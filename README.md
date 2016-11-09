fluentd-scala
=============

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
libraryDependencies += "eu.inn" %% "fluentd-scala" % "0.1.22"
```


#### Release new version

```
sbt publishSigned
sbt sonatypeRelease
```
