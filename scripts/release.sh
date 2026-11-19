#!/usr/bin/env bash
# Releases daylog: sets the release version, moves the release notes, builds, commits, tags and starts the next snapshot.
# With --publish it also pushes and creates the GitHub release (needs `gh`). Without it, nothing leaves this machine.
# Usage: scripts/release.sh <X.Y.Z> [--publish] [--dry-run]
set -euo pipefail
cd "$(dirname "$0")/.."

release_version="${1:?usage: release.sh X.Y.Z [--publish] [--dry-run]}"
publish=false; dry_run=false
for flag in "${@:2}"; do
  case "$flag" in
    --publish) publish=true ;;
    --dry-run) dry_run=true ;;
    *) echo "unknown flag $flag" >&2; exit 2 ;;
  esac
done

[[ "$release_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "version must be X.Y.Z" >&2; exit 2; }

# Runs a command, or only prints it in dry-run mode.
run() { if $dry_run; then echo "[dry-run] $*"; else "$@"; fi; }

# Reads the project version: the first <version> in the pom is the project's own.
project_version() { sed -n 's:.*<version>\(.*\)</version>.*:\1:p' pom.xml | head -1; }

branch="$(git rev-parse --abbrev-ref HEAD)"
[[ "$branch" == "main" ]] || { echo "release from main, you are on $branch" >&2; exit 1; }
[[ -z "$(git status --porcelain)" ]] || { echo "working tree is not clean" >&2; exit 1; }
git rev-parse "v$release_version" >/dev/null 2>&1 && { echo "tag v$release_version already exists" >&2; exit 1; }
grep -qE '^## Unreleased' RELEASE_NOTES.md || { echo "RELEASE_NOTES.md has no '## Unreleased' section" >&2; exit 1; }
unreleased_lines="$(awk '/^## Unreleased/{f=1;next} /^## /{f=0} f && NF' RELEASE_NOTES.md | wc -l)"
[[ "$unreleased_lines" -gt 0 ]] || { echo "the Unreleased section is empty, write the release notes first" >&2; exit 1; }

echo "Current version $(project_version) -> releasing $release_version"
today="$(date +%Y-%m-%d)"

# Next development version: patch + 1, as a snapshot.
IFS=. read -r major minor patch <<< "$release_version"
next_snapshot="$major.$minor.$((patch + 1))-SNAPSHOT"

run mvn -q versions:set -DnewVersion="$release_version" -DgenerateBackupPoms=false

if $dry_run; then
  echo "[dry-run] move '## Unreleased' notes to '## $release_version - $today' in RELEASE_NOTES.md"
else
  python3 - "$release_version" "$today" <<'PY'
import sys
version, today = sys.argv[1], sys.argv[2]
text = open("RELEASE_NOTES.md").read()
text = text.replace("## Unreleased", "## Unreleased\n\n## %s - %s" % (version, today), 1)
open("RELEASE_NOTES.md", "w").write(text)
PY
fi

run mvn -q clean verify
notes_file="target/release-notes-$release_version.md"
if $dry_run; then
  echo "[dry-run] extract notes of $release_version to $notes_file"
else
  awk -v v="## $release_version " 'index($0, v)==1{f=1;next} /^## /{f=0} f' RELEASE_NOTES.md > "$notes_file"
  echo "Release notes:"; cat "$notes_file"
fi

run git commit -qam "Release $release_version"
run git tag -a "v$release_version" -m "daylog $release_version"
run mvn -q versions:set -DnewVersion="$next_snapshot" -DgenerateBackupPoms=false
run git commit -qam "Start development of $next_snapshot"

if ! $publish; then
  echo "Done locally. To publish: git push origin main v$release_version &&"
  echo "  gh release create v$release_version target/daylog.jar --title 'daylog $release_version' --notes-file $notes_file"
  exit 0
fi
command -v gh >/dev/null || { echo "gh (GitHub CLI) is not installed" >&2; exit 1; }
run git push origin main "v$release_version"
run gh release create "v$release_version" target/daylog.jar --title "daylog $release_version" --notes-file "$notes_file"
