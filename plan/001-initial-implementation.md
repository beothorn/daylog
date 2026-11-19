# 001 Initial implementation
Goal: working tray app that appends entries to a configured markdown file.

1. [ ] Create Maven project (Java 21, JUnit 5, slf4j-simple, shade plugin) producing `target/daylog.jar`.
2. [ ] Create `AppDirectory` resolving `~/.daylog` or `%APPDATA%\daylog`, with tests.
3. [ ] Create `Settings` storing the log file path, with tests.
4. [ ] Create `TaskName`, `TaskHistory` and `TaskCompletion`, with tests.
5. [ ] Create `LogEntry`, `EntryFormatter` (dynamic fence) and `LogFileAppender`, with tests.
6. [ ] Create `EntryDialog`, `LogFileChooser` and `TrayApp` in `ui`.
7. [ ] Wire everything in `Main`.
8. [ ] Run `mvn verify`, start the jar and check it starts without errors.
9. [ ] Ask the user for a manual tray test on their desktop.
