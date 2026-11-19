---
name: plan-task
description: Create a plan in plan/ for a task and move its kanban card (backlog, todo, doing, done, deployed) with a RICE score. Use before writing any code for a new task.
---

# Plan a task

1. Next number `NNN`: look at `plan/` and `kanban/*/`.
2. Create `plan/NNN-short-name.md`: a numbered list of short actionable sentences.

```markdown
# NNN Short name
Goal: one sentence.

1. [ ] Create record `TaskName` in `src/main/java/daylog/task`.
2. [ ] Add `TaskNameTest` for blank and multi-line input.
3. [ ] ...
```

3. Create card `kanban/backlog/NNN-short-name.md`:

```markdown
# NNN Short name
Plan: ../../plan/NNN-short-name.md
RICE: Reach x Impact x Confidence / Effort = score
Notes: ...
```

4. Prioritize with the user using the RICE score, then `git mv` the card: `backlog -> todo -> doing -> done`.
   On release, put it under `kanban/deployed/<version>/` with release notes.
5. Tick plan items as they are done. Cycle: plan, score, prioritize, develop, test (ask the user for a manual UI test), release.
