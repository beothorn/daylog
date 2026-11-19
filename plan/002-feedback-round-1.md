# 002 Feedback round 1
Goal: apply the owner's feedback after the first manual test, and add versioning and releases.

1. [x] Make the appender leave exactly one blank line between the existing content and a new entry.
2. [x] Focus the text area (not the task) when the dialog opens; do not clear typed text if the tray is clicked while it is open.
3. [x] Replace inline autocomplete with a drop-down list under the task field (`TaskCompletion.suggestionsFor`).
4. [x] Draw a small book as the tray icon (`BookIcon`).
5. [x] Show the version as the last label of the dialog, read from the filtered `version.properties` (`AppVersion`).
6. [x] Set the pom version to 0.0.1-SNAPSHOT and document it as the current version in AGENTS.md.
7. [x] Create `RELEASE_NOTES.md` with an Unreleased section.
8. [x] Create `scripts/release.sh` and skill `release` (semver bump, notes, tag, GitHub release).
9. [x] Update user and technical docs, run `mvn verify`, commit.
