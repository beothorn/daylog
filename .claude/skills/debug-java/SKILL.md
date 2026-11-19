---
name: debug-java
description: Investigate a bug in daylog by reproducing it in a single unit test, running that test under jdb, reading stack traces and using git blame/log. Use whenever behavior is wrong or a test fails.
---

# Debug Java (daylog)

## 1. Reproduce with one test
Write the smallest failing test (use `@TempDir` for files, inject a fixed `Clock`). Run only it:

```bash
mvn -q test -Dtest=EntryFormatterTest#formatsMultilineText
```

Run a whole class: `-Dtest=EntryFormatterTest`. Add `-Dsurefire.failIfNoSpecifiedTests=false` if filtering matches nothing.
Reports with stack traces: `target/surefire-reports/*.txt`.

## 2. Read the stack trace
Read bottom-up to the first `daylog.` frame, that is where our code is wrong or was called wrongly. `Caused by:` is the real reason.

## 3. Attach jdb to the single test
Terminal A (suspends and waits on port 5005):

```bash
mvn -q test -Dtest=EntryFormatterTest#formatsMultilineText -Dmaven.surefire.debug
```

Terminal B (or `scripts/debug-test.sh Class#method` which does both):

```bash
jdb -attach 5005 -sourcepath src/main/java:src/test/java
```

Useful jdb commands: `stop in daylog.entry.EntryFormatter.format`, `stop at daylog.entry.EntryFormatter:42`, `cont`, `where`,
`locals`, `print var`, `dump obj`, `step`, `next`, `step up`, `eval expr`, `clear`, `quit`.
Tip: break on exceptions with `catch java.lang.IllegalArgumentException` (or `catch all`).

## 4. Why is the code like this?
```bash
git blame -w -C -C src/main/java/daylog/entry/EntryFormatter.java
git log -L :format:src/main/java/daylog/entry/EntryFormatter.java
git log -S'some text' --oneline
git log --name-only --oneline -- src/main/java/daylog/entry
```

## 5. Callers and hierarchy
`grep -rn "methodName(" src/`, `grep -rn "implements X\|extends X" src/`. For compiled facts: `javap -c -p -cp target/classes daylog.entry.EntryFormatter`.

## 6. Running app (UI) problems
Run with trace logs (see `change-log-level`), read output (see `analyze-logs`). Hang: `jcmd <pid> Thread.print` or `jstack <pid>`.
Attach to the running jar: start with `java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/daylog.jar`, then `jdb -attach 5005`.

## Finish
Keep the reproducing test as a regression test. Commit the fix and test together.
