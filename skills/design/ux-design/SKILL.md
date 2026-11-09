---
name: ux-design
description: UX pass for one feature of an Android app, covering names, flow, where it opens (inline, bottom sheet, dialog, full screen, Snackbar, menu), states, words, tooltips, accessibility, and AI features. Use when a story adds a screen, an action, or text, when user-stories or improve-loop call it, or when the user asks for a UX review, clearer wording, better names, or whether something should be a dialog.
argument-hint: "[story ID or screen]"
---

# UX design

A feature that works can still confuse people. This pass decides what
things are called, how a person moves through the feature, where it opens,
and what it says, before any code exists.

Run it once per story that adds a screen, an action, or words. The output
is a `UX:` block in the story in `docs/STORIES.md`, plus new terms in
`docs/GLOSSARY.md`. To review a built screen, run the same steps against
its snapshot in `shots/` and list what fails, most serious first.

## 0. Read

Read `docs/BRIEF.md`, `docs/GLOSSARY.md` (create it if missing), the story,
and its reference screenshots.

## 1. Name it

Every thing in the app has exactly one name, and every name lives in the
glossary.

```markdown
| Term | Means | Not |
| --- | --- | --- |
| Task | One thing to do, with an optional due date | to-do, item, entry |
| Complete | Mark a task done; it leaves the list | check, finish, close |
```

- Name the things first (the nouns in the brief), then the actions on them.
- Use the user's words, not the code's: "Task", not "TaskEntity" or "record".
- One word per idea across buttons, menus, titles, descriptions, and errors.
- Name a feature for what it does for the person, not the technology.
  Method: [writing.md](writing.md#names).

Check: every noun and verb the story puts on screen is in the glossary,
and no glossary term has a second name anywhere.

## 2. Map the flow

Write the flow as one line, from every way in to a clear end.

```text
FAB, widget, share sheet "Add Task" -> title field has focus -> Done on the keyboard saves, row appears -> focus stays for the next task
```

- List every entry point: FAB, menu, widget, shortcut, share target, a deep link.
- Cut steps: fill in good defaults, ask only for what you cannot guess.
- Every step has a way back: Back cancels, Undo reverses, and system back
  or the predictive back gesture goes to the previous screen.
- The end is visible: the new row, a check, the sheet closing.

Check: the flow fits on one line, and each step names its way back.

## 3. Pick the surface

Go down this list and use the first one that fits. Lighter is better.

| The feature | Use |
| --- | --- |
| Changes one value on something already on screen | Inline (field, switch, chip, menu in the row) |
| A short list of actions on one item | Menu (`DropdownMenu`) |
| A few related choices tied to one control | Bottom sheet (`ModalBottomSheet`) |
| One short decision that must finish or cancel before going on | Dialog (`AlertDialog`) |
| A long or self-contained task, or one worth a deep link | Its own screen, a `Route` on the back stack |
| A place people come back to, like a list or a collection | Navigation destination (bar, rail, drawer) |
| Feedback after an action, with a chance to reverse it | Snackbar with Undo |
| A rare action that destroys data and cannot be undone | Confirmation dialog |
| A problem people must act on right now | Inline banner at the top of the screen |

- Show one modal at a time. Never open a dialog from a dialog.
- A dialog's title names its task: "Rename Project", not "Edit".
- The confirm button names the action, and Back cancels.
- Focus moves into a dialog when it opens and back to the control that
  opened it when it closes. Material dialogs and sheets do both.
- Never use a Toast for an error or for anything people must read: it
  cannot be acted on and TalkBack may miss it. Use a Snackbar.
- Information that needs no action goes inline, never in a dialog.
- Destructive actions offer Undo (a Snackbar) instead of a confirmation.
  Keep the confirmation for what Undo cannot restore.

Check: the `UX:` block names the surface and the table row that picked it.

## 4. Cover the states

Every screen the story touches has words and a look for each state:

- **Empty**: what it is for, plus the first action as a button.
- **Loading**: only when it can take longer than half a second. Show
  cached content first. A skeleton for the layout, words for what is
  happening ("Importing 12 notes"), and Cancel when it is safe.
- **Error**: inline, next to the problem, with the fix.
- **Offline**: what still works, and what waits for the network.
- **Too much**: long names truncate to one line with an ellipsis and show in full on the detail screen; a
  thousand rows still scroll smoothly.
- **Narrow and wide**: the screen works at 360 dp wide and on a tablet.
- **Large text**: it works at 1.3 times font scale and does not clip.
- **Done**: visible feedback in the same frame, and Undo where it applies.

Check: each state has a snapshot page or seed data that shows it.

## 5. Write the words

- Buttons and menu items start with a verb: "Add Task", "Export...".
- Add an ellipsis when the command asks for more input before it acts.
- Title case for buttons, menu items, and screen titles. Sentence case for
  labels, descriptions, and messages.
- Errors say what happened and how to fix it, without blame. Write
  "Unable to load tasks", never "Oops, something went wrong".
- Confirmations repeat the action: "Delete 3 Tasks?" with buttons "Delete"
  and "Cancel", never "Yes" and "No".

Patterns and before and after examples: [writing.md](writing.md).

A feature that uses AI (a summary, generated text, a model's suggestion)
also follows [ai-features.md](ai-features.md).

Check: read every string aloud. Each one is short, uses glossary terms
only, and a new user could say what each button does.

## 6. Add tooltips and labels

- Every icon-only button gets a `contentDescription` with what it does,
  starting with a verb: "Add a task". TalkBack reads it. Rules:
  [writing.md](writing.md#tooltips-and-labels).
- Touch has no hover. Secondary actions also go in the overflow menu or a
  long press menu, and long press shows a `TooltipBox` for icon buttons.
- Information people need is always visible. A tooltip only adds detail.

Check: every icon-only control in the snapshot has a content description,
and the self-test finds it by that description.

## 7. Check accessibility

Target WCAG 2.2 AA and the Android accessibility guidance.

- Touch targets are at least 48 dp by 48 dp.
- TalkBack reads every control with the same words as the screen, in reading
  order. Group a row with `Modifier.semantics(mergeDescendants = true)` and
  give it a role (`Role.Button`, `Role.Checkbox`, `Role.Switch`).
- Text contrast is 4.5:1 (3:1 for large text and control borders) in light and dark.
- Meaning never rests on color alone: add an icon or a word.
- Text scales: the screen works at 1.3 times font scale, and does not clip at 2 times.
  Use `sp` for text and let containers grow.
- Motion respects the system "remove animations" setting.
- Hardware keyboards and switch access reach every control: focus order
  follows reading order and focus is visible.
- Back works everywhere: system back and predictive back pop one screen.

Check: you ran the flow with TalkBack on a device or emulator once, or the
self-test finds every control by its description, and the snapshots show no clipped text at 1.3 times font scale.

## 8. Write the UX block

Add the result under the story's acceptance lines:

```markdown
UX:
- Flow: FAB -> title field -> Done saves -> focus stays
- Surface: inline row at the top of the list (changes one value on screen)
- States: empty "No Tasks" with "Add Task"; error "Give the task a title." under the field
- Words: "Add Task", content description "Add a task", Snackbar "Task added" with Undo
- Access: Done on the keyboard saves, Back cancels; label "New task title"
```

Done when the story has a `UX:` block, every term it uses is in
`docs/GLOSSARY.md`, and steps 1 to 7 pass on its snapshots.
