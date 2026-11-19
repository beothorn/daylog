---
name: release
description: Release daylog: choose the next semver version, update RELEASE_NOTES.md and pom.xml, build, tag, and upload the jar to GitHub with release notes. Use when the user asks to release, publish or ship a version.
---

# Release

Versions follow semver (`MAJOR.MINOR.PATCH`). The development version is the `-SNAPSHOT` in `pom.xml` (single source of truth; the app shows it
in the dialog through the filtered `version.properties`). Release notes live in `RELEASE_NOTES.md`, newest first, with an `## Unreleased` section on top.

## Steps

1. **Check readiness.** On `main`, clean tree, `mvn -q verify` passes. `## Unreleased` in `RELEASE_NOTES.md` must describe what changed
   (write it from `git log <last tag>..HEAD --oneline`; users should understand it, not just commit titles). Keep it updated while developing, not only at release time.
2. **Choose the version.** Last released version = latest `vX.Y.Z` tag (`git describe --tags --abbrev=0`), or none for the first release (then use the
   snapshot's own number, e.g. `0.0.1-SNAPSHOT` -> `0.0.1`). Then:
   - breaking change for users (file format, settings location, removed feature) -> **major** (while < 1.0.0, use minor);
   - new feature -> **minor**; only fixes/internal -> **patch**.
   Tell the user the chosen version and why.
3. **Dry run** (nothing changes): `scripts/release.sh X.Y.Z --dry-run`.
4. **Release locally**: `scripts/release.sh X.Y.Z`. It sets the version in the pom, moves Unreleased notes under `## X.Y.Z - date`, runs `mvn clean verify`,
   commits `Release X.Y.Z`, tags `vX.Y.Z`, then sets the next snapshot (`X.Y.(Z+1)-SNAPSHOT`) and commits it.
5. **Publish: ask the user first**, this is outward-facing (pushes `main` and the tag, creates a public GitHub release with `target/daylog.jar`).
   On a yes run the printed commands, or rerun with `--publish` on a fresh checkout state. Requires the GitHub CLI `gh`, logged in (`gh auth status`).
   Never publish without the user's yes in this conversation.
6. **After publishing**: move the kanban cards that shipped to `kanban/deployed/X.Y.Z/`, update AGENTS.md "Versioning" if wording needs it,
   and check the release page lists `daylog.jar` and the notes.

## If something fails
- Before the tag: `git reset --hard origin/main` is NOT needed, just `git checkout -- . ` for uncommitted version edits and rerun.
- After the local commits but before pushing: `git tag -d vX.Y.Z` and `git reset --hard HEAD~2`, then fix and rerun.
- Never force-push or delete a published tag/release without the user asking.
