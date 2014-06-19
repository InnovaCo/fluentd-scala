package eu.inn.fluentd

import java.net.{InetAddress, InetSocketAddress}
import akka.routing.RoundRobinRouter

import scala.collection.JavaConversions._
import scala.concurrent.duration._

import akka.actor._
import akka.io.{Tcp, IO}
import akka.util.ByteString
import ch.qos.logback.classic.pattern.CallerDataConverter
import ch.qos.logback.classic.spi.{ThrowableProxyUtil, ILoggingEvent}
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.typesafe.config.ConfigFactory
import org.msgpack.ScalaMessagePack


class FluentdAppender extends UnsynchronizedAppenderBase[ILoggingEvent] {
  import FluentdAppender._

  private var appender: ActorRef = null

  private var tag        = "default"
  private var remoteHost = "127.0.0.1"
  private var port       = 24224

  override def start() {
    super.start()
    appender = actorSystem.actorOf(Props(classOf[FluentdLoggerActor], tag, remoteHost, port).withRouter(RoundRobinRouter(nrOfInstances = 10)))
  }

  override def append(eventObject: ILoggingEvent) {
    if (isStarted && appender != null) {
      appender ! eventObject
    }
  }

  override def stop() {
    try {
      super.stop()
    } finally {
      actorSystem.stop(appender)
    }
  }

  def setTag(tag: String) {
    this.tag = tag
  }

  def setRemoteHost(remoteHost: String) {
    this.remoteHost = remoteHost
  }

  def setPort(port: Int) {
    this.port = port
  }
}


object FluentdAppender {
  lazy val actorSystem = ActorSystem(
    "fluentd-logger",
    ConfigFactory.parseString("""
      akka.loggers = []
      akka.log-dead-letters = off
    """).withFallback(ConfigFactory.load())
  )
}


class FluentdLoggerActor(tag: String, remoteHost: String, port: Int) extends Actor with Stash with ActorLogging {

  private val messagePack = new ScalaMessagePack

  override def preStart() {
    connect()
  }

  private def connect(delay: FiniteDuration = 0.second) {
    import context.dispatcher
    log.info("Try connect to fluentd after {}", delay)

    context.system.scheduler.scheduleOnce(delay, IO(Tcp)(context.system), Tcp.Connect(new InetSocketAddress(remoteHost, port)))
  }

  def receive = {
    case _: Tcp.Connected ⇒
      log.info("Connected to fluentd")

      sender ! Tcp.Register(self)
      context.become(connected(sender))
      unstashAll()

    case e @ Tcp.CommandFailed(_: Tcp.Connect) ⇒
      log.warning("Error connect to fluentd agent: {}", e)
      connect(delay = 5 seconds)

    case e: Tcp.Event ⇒
      log.warning("Unexpected TCP Event {}", e.getClass)

    case _ ⇒ stash()
  }

  var writeErrors = 0

  def connected(conn: ActorRef): Receive = {

    /**
     * todo:
     *  - add buffering
     *  - add nrOfInstances to logback xml settings
     */
    case event: ILoggingEvent ⇒
      val data = event.getMDCPropertyMap ++ Map(
        "message"   → event.getFormattedMessage,
        "level"     → event.getLevel.toString,
        "logger"    → event.getLoggerName,
        "thread"    → event.getThreadName,
        "timestamp" → event.getTimeStamp,
        "hostname"  → InetAddress.getLocalHost.getHostName
      )

      if (event.getMarker != null) {
        data("marker") = event.getMarker.getName
      }

      if (event.hasCallerData) {
        data("caller") = new CallerDataConverter().convert(event)
      }

      if (event.getThrowableProxy != null) {
        data("throwable") = ThrowableProxyUtil.asString(event.getThrowableProxy)
      }

      conn ! Tcp.Write(ByteString(messagePack.write(List(tag, event.getTimeStamp / 1000, data))))

    case Tcp.CommandFailed(write: Tcp.Write) ⇒
      writeErrors += 1
      if (writeErrors > 1000) {
        log.warning("Too many errors, close current connection")
        writeErrors = 0
        conn ! Tcp.Close
      }

    case e: Tcp.ConnectionClosed ⇒
      log.warning("Error write to fluentd: {}", e)
      context.unbecome()
      connect(delay = 5 seconds)

    case other ⇒
      log.warning("Unexpected message {}", other)
  }
}
