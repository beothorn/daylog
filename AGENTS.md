# AGENTS.md

Instructions for AI agents working on **daylog**: a tiny tray application (Java, Windows + Linux) where the user
logs what they are doing, so they can remember it later. Entries are appended to a markdown diary file.

`CLAUDE.md` simply points here. Keep this file the single source of truth.

# Audience

## Dev

The person talking to you is a **Java developer**. Be technical and direct, skip beginner explanations.
They want things done in a single try: make reasonable decisions, state them briefly, do not ask unnecessary questions.
User decisions always win over defaults written here.

## Userbase

Today only the author. The project is open source (Apache 2.0, on GitHub), so anyone may use it.
Final users are people who keep a plain-text/markdown diary and want a zero-friction way to append entries.
They are comfortable running `java -jar`. User docs (`Docs_user`) must not mention code.

# Project summary

- Tray icon. Left click opens a dialog: a one-line **task** field (autocomplete from history, prefilled with the last task)
  and a 4-line **text** area (scrolls when longer). Saving appends an entry to the configured log file.
- Right click menu: Log now, Choose log file, Quit.
- Runs locally, stores everything on local disk. No network. Distributed as one jar, no installer.
- Must be **very fast**: instant startup, instant dialog. No heavy dependencies, no classpath scanning, dialog is built once and reused.

Entry format appended to the log file (one entry, followed by a line break):

````
2026_11_19_23_55 `Offhour work`
```
Now I am writing the setup for the seed
with line breaks and so on
```
````

Details and rules: `Docs_technical/architecture.md`, `Docs_technical/glossary.md`, `Docs_technical/use-cases.md`.

# Tools

- JDK 21, Maven 3.9 (`mvn`). No wrapper installed; plain `mvn` is fine.
- `aibin/` is git-ignored. Put downloaded executables there. Do not put scripts there, scripts go to `scripts/`.
- Common commands:

| Goal | Command |
|---|---|
| Build jar + run tests | `mvn -q verify` (jar: `target/daylog.jar`) |
| Run tests only | `mvn -q test` |
| One test class / method | `mvn -q test -Dtest=EntryFormatterTest#formatsMultilineText` |
| Run app | `java -jar target/daylog.jar` |
| Run app with trace logs | `java -Dorg.slf4j.simpleLogger.defaultLogLevel=trace -jar target/daylog.jar` |

# Skills

Skills live in `.claude/skills/<name>/SKILL.md` (the Claude Code convention; other agents can read them as plain markdown).

| Skill | Use it for |
|---|---|
| `debug-java` | Reproducing a bug with a unit test, attaching `jdb`, stack traces, git blame/log investigation |
| `change-log-level` | Raising/lowering log level globally or per package, at runtime start |
| `analyze-logs` | Reading and filtering daylog's log output to follow a flow |
| `plan-task` | Writing a plan in `plan/` and moving the card through the kanban |

Scripts used by skills live in `scripts/`.

# Architecture

Package root: `daylog`. Folders are **domain first, then layer** (see Code section). Source: `src/main/java/daylog`.

```
daylog/
  Main.java                  wires everything together, nothing else
  appdir/AppDirectory        resolves ~/.daylog (Windows: %APPDATA%\daylog)
  settings/Settings          persisted settings (log file path) in settings.properties
  task/                      TaskName (validated microtype), TaskHistory (tasks.txt), TaskCompletion (autocomplete logic)
  entry/                     LogEntry, EntryFormatter (markdown text), LogFileAppender
  ui/                        TrayApp (tray + menu), EntryDialog (Swing), LogFileChooser
```

- Persistence is plain files in the app directory: `settings.properties`, `tasks.txt` (one task per line, last = most recent).
- UI is AWT `SystemTray` + Swing. No third-party UI libs. Pure logic (formatting, completion, history, appending) has no UI imports and is unit tested.
- Logging: SLF4J API + `slf4j-simple`. Config by `-Dorg.slf4j.simpleLogger.*` system properties (see skill `change-log-level`).
- Linux note: GNOME needs the AppIndicator/KStatusNotifierItem extension for tray icons to show. Not a bug in this app.
- Build: Maven, `maven-shade-plugin` produces `target/daylog.jar` with `Main-Class` set. Tests: JUnit 5 + AssertJ-free plain assertions.
- Headless environments (CI) cannot show the tray: UI classes are not covered by unit tests; test the logic around them.

# Debugging (very important, you will debug the system yourself)

1. Reproduce with the smallest thing: a unit test (`mvn -q test -Dtest=Class#method`) or a tiny scenario in a test using `@TempDir`.
2. Turn on logs: trace/debug (skill `change-log-level`). Every function logs a trace on entry, so the flow is visible.
3. Need a debugger: `mvn -q test -Dtest=Class#method -Dmaven.surefire.debug` waits on port 5005, then `jdb -attach 5005`
   (`stop in daylog.entry.EntryFormatter.format`, `cont`, `where`, `locals`, `print x`, `step`, `next`). Full recipe in skill `debug-java`.
4. Understand why code is as it is: `git blame -w -C -C <file>`, `git log -L :method:file`, `git log -S'text' --oneline`.
5. Find callers/usages: `grep -rn "methodName(" src`, or `jdeps`/`javap -c` when needed. Read stack traces bottom-up to the first `daylog.` frame.
6. UI problems: run the jar with trace logs and read the output; `jstack <pid>` for hangs, `jcmd <pid> Thread.print`.

# Security

Never commit passwords, keys or anything sensitive. Prefer env vars. The `secrets/` folder is git-ignored; if a secret is truly
needed, ask the dev to create/edit the file there. Avoid at all costs reading secrets. If the user insists on pushing a secret
(tests, for example), advise against it once, warn, and do it. (This app currently has no secrets and no network access.)

# Planning

Planning is done with the `plan/` folder, no OpenSpec. **Every task gets a plan** before code: `plan/NNN-short-name.md`,
a numbered sequence of short, actionable sentences ("Create class X in folder Y", "Add test Z"). Tick items as done (`- [x]`).
Always remind yourself to plan first. Skill `plan-task` has the template.

# Planning and managing iteration

`kanban/` has folders `backlog/ todo/ doing/ done/ deployed/`. A card is a markdown file (`NNN-name.md`) with a short description,
the RICE score and a link to its plan. Move the file with `git mv` as it progresses. `deployed/` holds a folder or file per version with release notes.

Cycle: **plan -> score (RICE) -> prioritize with the user -> develop -> test (run it yourself; ask the user for a manual test of tray/UI behavior, which you cannot fully see) -> release**.

RICE = (Reach x Impact x Confidence) / Effort. Reach: users per period; Impact: 0.25/0.5/1/2/3; Confidence: %; Effort: person-days.

# Code

Language is Java 21. Style rules:

Commit often, in small self-contained steps. Descriptive title, and a body saying what was done, why, who asked for it and how it was asked.
Use `git blame` and `git log` to understand why code is like it is. Revert, cherry-pick or branch when needed.

Keep everything simple. Less code is better. Good code is many small programs you can reason about separately.

**Folders: domain first, then layer.** `./payment/database`, `./payment/screen`, not `./database/payment`.
Common code **bubbles up** to the lowest common parent folder, only as high as needed. If `A` and `B` both depend on `C`,
keep `C` in the parent in its own folder: `./A`, `./B`, `./C`. Folders hold code as independent as possible: reading a leaf
folder should be enough; avoid navigating sideways. Avoid generic names (`utils`, `common`, `helpers`).

**Readability**
- Prefer immutability (`final`, records), small self-contained functions, long meaningful names.
- Keep declaration and usage close. Prefer composition over inheritance. One responsibility per function/file.
- Count what a reader must remember (state, which condition was met). Reduce it.
- Conditionals as early as possible; prefer strategy/choosing a different function over passing a boolean flag.
- Early returns instead of `else`. Keep width small (about 100 columns). Example:

```java
if (task == null) {
    log.warn("Task is missing");
    return;
}
if (!task.isNew()) {
    log.debug("Task {} already known", task);
    return;
}
history.add(task);
```

- Extract variables with good names: `int currentUsersCount = 5; store(currentUsersCount);` not `store(5);`.
- Avoid magic: no reflection, no behavior depending on naming conventions, nothing whose origin you cannot trace.
- Put important code to the left, boilerplate on one line:

```java
Dialog.builder().owner(frame).modal(false)
    .title("Daylog")
    .size(400, 300)
.build();
```

**Compile-time safety.** Static typing and microtypes validated at creation (`TaskName` instead of `String`). Validate as early as possible so later code does not recheck.

**Comments.** Before every function add a human-readable comment describing it, even if redundant, written like explaining to a junior dev
("In this function we...", "This class is responsible for...", "This is here to isolate...", "...and pay attention because..."). Anyone should be able to recreate the function from the comment.

**Logging.** Always add logs; always a `trace` at the start of a function, narrative style: `"append(file, entry): writing entry to {}"`.
Use SLF4J, one `Logger` per class (`LoggerFactory.getLogger(Foo.class)`) so filtering by class/package works.
- trace: as frequent as comments, narrates the flow. debug: sentences with values ("Mapping x to y"). 
- info: sparingly, only important events (app started, log file changed, entry saved).
- warn: unwanted state the process can continue from, and why. error: the app/process cannot continue, with consequences.
- Decision chain: for devs? needs variables -> debug, else trace. For operators? unwanted state? no -> info; can continue -> warn; else error.

**Tests.** Always add tests: happy path and errors. Ask "what could go wrong here?" and test it. JUnit 5, `@TempDir` for files, inject `Clock`/paths so tests are deterministic.

**Refactoring.** Extract/rename/move/inline when a business concept emerges or files always change together (confirm with `git log --name-only`).

# Documentation

Two folders:
- `Docs_technical/`: very technical, business logic, decisions, gotchas, warnings, sequence diagrams (mermaid). For you and devs.
- `Docs_user/`: user perspective only (the tray, the dialog, the files). No code or internals. Direct, has a glossary, applies the Descartes
  method: never use a term before it was explained (link to it).

`README.md` is small and direct: what it is, why use it, link to user docs and the download; last section "About" says who created and maintains it.
