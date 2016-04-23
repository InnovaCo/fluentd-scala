fluentd-scala
=============

[![Circle CI](https://circleci.com/gh/kulikov/fluentd-scala.svg?style=svg)](https://circleci.com/gh/kulikov/fluentd-scala)

[Logback](http://logback.qos.ch) appender for fluentd http://fluentd.org/ based on akka IO


```xml
<configuration>
  <appender name="FLUENT" class="eu.inn.fluentd.FluentdAppender">
    <tag>my.project</tag>
    <remoteHost>localhost</remoteHost>
    <port>24224</port>
  </appender>

  <root level="DEBUG">
    <appender-ref ref="FLUENT"/>
  </root>
</configuration>
```

Available on Maven Central. Settings for SBT:

```scala
libraryDependencies += "eu.inn" %% "fluentd-scala" % "0.1.20"
```


#### Release new version

```
sbt publishSigned
sbt sonatypeRelease
```
