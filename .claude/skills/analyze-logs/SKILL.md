---
name: analyze-logs
description: Capture and read daylog's log output to follow a flow, find warnings/errors, and filter by class or package.
---

# Analyze logs

1. Capture at trace level to a file (see `change-log-level`):

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=trace -Dorg.slf4j.simpleLogger.logFile=/tmp/daylog.log -jar target/daylog.jar
```

2. Filter. Lines look like `12:00:01.123 [AWT-EventQueue-0] TRACE LogFileAppender - append(file, entry): ...`.

```bash
grep -E "WARN|ERROR" /tmp/daylog.log                 # problems only
grep "LogFileAppender" /tmp/daylog.log               # one class
grep -n "TaskHistory" /tmp/daylog.log | head -50     # follow a component
awk '/Exception/{f=1} f' /tmp/daylog.log | head -40  # first stack trace onwards
```

3. Read the trace lines in order: they narrate the whole flow (user clicked -> dialog shown -> entry built -> appended).
   The last trace before an error shows where it broke. Debug lines carry the values.
4. Match the failing frame to code with `git blame` (see `debug-java`), then write a test reproducing it.

Data to look at besides logs: `~/.daylog/settings.properties`, `~/.daylog/tasks.txt` (Windows: `%APPDATA%\daylog`), and the configured log file.
