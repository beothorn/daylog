# Release notes

Newest first. Everything not yet released goes under **Unreleased**; the `release` skill moves it under the new version.
The version being developed is the one in `pom.xml` (see AGENTS.md, "Versioning").

## Unreleased

- Tray icon (a small book on a blue tile, drawn at the exact size of the tray slot) with a left-click dialog: task (drop-down autocomplete from earlier tasks, left aligned, last task prefilled) and a 4-line text area.
- Enter saves; Shift+Enter inserts a line break. The text area is focused when the dialog opens.
- Entries are appended to a configurable markdown/text file as `yyyy_MM_dd_HH_mm `task`` plus a fenced block, separated by a blank line.
- Right-click menu: Log now, Choose log file, the version, Quit.
- Uses the system look and feel.
