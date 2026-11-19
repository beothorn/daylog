# Glossary (technical)

- **Entry**: one logged item: timestamp, task, optional text. Rendered as markdown and appended to the log file.
- **Task**: short single-line label of what the user is working on ("Offhour work"). Free text.
- **Text**: the free multi-line description of what the user is doing.
- **Log file**: the user-chosen text/markdown file where entries are appended (e.g. a daily diary `19.md`).
- **App directory**: folder with the app's own data: `~/.daylog` (Windows `%APPDATA%\daylog`).
- **Task history**: remembered previous tasks, used for autocomplete and for prefilling the last one.
- **Autocomplete**: a drop-down under the task field listing earlier tasks that start with what was typed, most recent first.
- **Version**: the pom version (semver, `-SNAPSHOT` while in development), shown in the dialog. See AGENTS.md, "Versioning".
- **Tray**: the system notification area where the icon lives.
- **Microtype**: a small type wrapping a primitive that can only be created with a valid value (e.g. `TaskName`).
