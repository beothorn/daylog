#!/usr/bin/env bash
# Runs one test (Class or Class#method) suspended on port 5005 and attaches jdb to it.
# Usage: scripts/debug-test.sh EntryFormatterTest#formatsMultilineText
set -euo pipefail
cd "$(dirname "$0")/.."
test_selector="${1:?usage: debug-test.sh Class[#method]}"
mvn -q test -Dtest="$test_selector" -Dmaven.surefire.debug &
maven_pid=$!
trap 'kill $maven_pid 2>/dev/null || true' EXIT
until (exec 3<>/dev/tcp/127.0.0.1/5005) 2>/dev/null; do sleep 0.5; done
jdb -attach 5005 -sourcepath src/main/java:src/test/java
