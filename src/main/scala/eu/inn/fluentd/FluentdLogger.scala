package eu.inn.fluentd

import java.net.{InetAddress, InetSocketAddress}
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._
import scala.util.Try

import akka.actor._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import ch.qos.logback.classic.pattern.CallerDataConverter
import ch.qos.logback.classic.spi.{ILoggingEvent, ThrowableProxyUtil}
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.typesafe.config.ConfigFactory
import org.msgpack.ScalaMessagePack


class FluentdAppender extends UnsynchronizedAppenderBase[ILoggingEvent] {
  import eu.inn.fluentd.FluentdAppender._

  private var appender: ActorRef = null

  private var tag        = "default"
  private var remoteHost = "127.0.0.1"
  private var port       = 24224

  override def start() {
    super.start()
    appender = actorSystem.actorOf(Props(classOf[FluentdLoggerActor], tag, remoteHost, port))
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
      akka.daemonic = on
    """)
  )
}


class FluentdLoggerActor(tag: String, remoteHost: String, port: Int) extends Actor with Stash with ActorLogging {
  import context.dispatcher

  case object FlushBuffer

  private val messagePack = new ScalaMessagePack

  var writeErrors = 0

  val BufferSize    = 100
  val MaxWriteError = 100

  val LocalHostName = Try(InetAddress.getLocalHost.getHostName).getOrElse("unknown-host")

  val buffer = ListBuffer.empty[List[Any]]
  buffer.sizeHint(BufferSize)

  context.system.scheduler.schedule(10 seconds, 10 seconds, self, FlushBuffer)

  override def preStart() {
    connect()
  }

  private def connect(delay: FiniteDuration = 0.second) {
    log.info("Try connect to fluentd after {}", delay)
    context.system.scheduler.scheduleOnce(delay, IO(Tcp)(context.system), Tcp.Connect(new InetSocketAddress(remoteHost, port)))
  }

  def receive = {
    case _: Tcp.Connected ⇒
      log.info("Connected to fluentd")

      sender ! Tcp.Register(self)
      context.become(connected(sender()))
      unstashAll()

    case e @ Tcp.CommandFailed(_: Tcp.Connect) ⇒
      log.warning("Error connect to fluentd agent: {}", e)
      connect(delay = 5 seconds)

    case e: Tcp.Event ⇒
      log.warning("Unexpected TCP Event {}", e.getClass)

    case _: ILoggingEvent ⇒ stash()
  }

  def connected(conn: ActorRef): Receive = {
    case event: ILoggingEvent ⇒
      val data = event.getMDCPropertyMap ++ Map(
        "message"    → event.getFormattedMessage,
        "level"      → event.getLevel.toString,
        "logger"     → event.getLoggerName,
        "thread"     → event.getThreadName,
        "timemillis" → event.getTimeStamp.toString,
        "host"       → LocalHostName
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

      buffer += List(event.getTimeStamp / 1000, data)

      if (buffer.size >= BufferSize) {
        flushBuffer(conn)
      }

    case FlushBuffer ⇒
      if (buffer.nonEmpty) {
        flushBuffer(conn)
      }

    case Tcp.CommandFailed(write: Tcp.Write) ⇒
      log.info("Error write message to fluentd, retry")

      writeErrors += 1
      if (writeErrors > MaxWriteError) {
        log.warning("Too many writer errors, close current connection and reconnect")
        writeErrors = 0
        conn ! Tcp.Close
      } else {
        conn ! write // retry
      }

    case e: Tcp.ConnectionClosed ⇒
      log.warning("Error write to fluentd: {}", e)
      context.unbecome()
      connect(delay = 5 seconds)
  }

  private def flushBuffer(conn: ActorRef) {
    conn ! Tcp.Write(ByteString(messagePack.write(List(tag, buffer.toList))))
    buffer.clear()
  }
}
