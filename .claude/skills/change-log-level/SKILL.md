---
name: change-log-level
description: Change daylog's log verbosity (trace/debug/info/warn/error), globally or for one package/class, using slf4j-simple system properties.
---

# Change log level

Logging is SLF4J + `slf4j-simple`. It is configured at JVM start with system properties (no config file needed).
Defaults: level `info`, short class names, time shown, output on stderr.

| Goal | Flag |
|---|---|
| Everything at trace | `-Dorg.slf4j.simpleLogger.defaultLogLevel=trace` |
| One package only | `-Dorg.slf4j.simpleLogger.log.daylog.task=trace` |
| One class only | `-Dorg.slf4j.simpleLogger.log.daylog.entry.LogFileAppender=debug` |
| Write to a file | `-Dorg.slf4j.simpleLogger.logFile=/tmp/daylog.log` |

Examples:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=warn -Dorg.slf4j.simpleLogger.log.daylog.entry=trace -jar target/daylog.jar
```

For tests: `mvn -q test -DargLine="-Dorg.slf4j.simpleLogger.defaultLogLevel=trace"`.

Levels cannot change while running; restart the app. Do not edit code to change levels. Defaults for the packaged jar are in
`src/main/resources/simplelogger.properties`; override with the flags above.
