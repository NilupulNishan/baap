# Writing patterns

Patterns for the words in an Android app. Each one has a rule and a before
and after.

## Contents

- Voice
- Names
- Buttons and menu items
- Titles
- Empty states
- Errors
- Confirmations
- Undo
- Settings
- Fields and hints
- Permissions
- Progress
- Tooltips and labels
- Counts and plurals
- Words to cut

## Voice

Pick three words for the app's voice and keep them in `docs/GLOSSARY.md`
under `## Voice`, for example "calm, direct, brief". Tone changes with the
moment: plain and serious for errors, a little warmer for a finished goal.

- Talk to people as "you". Never "the user".
- Say "tap" for a touch screen and "select" when it can be a tap or a key press.
- No idioms or jokes that do not translate. Explain a technical term the
  first time, or drop it.

## Names

A good name fits the app's other words, sets the right expectation, and
works in every language.

- Name a feature by what it does for the person, not by the technology:
  "Keep Working Offline", not "Local Room Cache".
- A switch gets a verb phrase that answers "what" and "for whom".
- Avoid clever names that raise questions. A label must not sound like a
  judgment when its value is low or zero ("Spending Power: $0").
- Where trust matters (money, health, privacy), use the industry word
  ("Balance", not "Current Funds").
- Reuse words already in the app. A new name for an old idea is a bug.
- Test each name in sentences: "Turn on Focus Mode", "Search for it in
  Recent". If the sentence sounds odd, the name is wrong.

To find a name: write who it is for, then three lists of words, what the
feature makes people think, feel, and do. Group them into themes, cut what
fails the rules above, and try the rest in sentences.

## Buttons and menu items

Start with a verb. Name the object when the verb alone is unclear.

| Before | After |
| --- | --- |
| OK | Save |
| Let's go! | Create Project |
| New | New Task |
| Export | Export... (it asks where or what first) |
| Click here to learn more | Learn About Smart Lists |

- Title case on buttons and menu items.
- An ellipsis means "asks for more before it acts". Leave it off commands
  that act at once.
- Multi-step flows start with "Get Started", move with "Continue", and end
  with "Done". Keep the same words in every flow.
- A link reads as its destination ("Privacy Policy"), never "here".

## Titles

The top bar title names the screen. Dialog titles name the task.

| Before | After |
| --- | --- |
| Tasks | Today |
| Edit | Rename Project |
| Settings Page | Settings |

## Empty states

Say what the screen is for and give the first action as a button.

| Before | After |
| --- | --- |
| Nothing here. | No Tasks. Tasks you add appear here. [Add Task] |
| Your inbox is empty! | All Done. New tasks land here first. |

Keep critical information out of empty states. They disappear.

## Errors

Say what happened and how to fix it, next to the problem, without blame.
Use a Snackbar for a failed action and inline text for a field. Never a Toast.

| Before | After |
| --- | --- |
| Oops! Something went wrong. | Unable to save the task. The device storage is full. |
| Invalid name. | Use letters and numbers only. |
| Password too short. | Choose a password with at least 8 characters. |
| Error 401 | Your session ended. Sign in again. |
| Failed to fetch | Unable to reach the server. Your changes are saved on this device. |

Say what to do, not what is forbidden: "Use only letters for your name",
not "Numbers are not allowed".

When the same error keeps happening to many people, change the design, not
the message.

## Confirmations

Ask only for rare actions that cannot be undone. The title is a question
with the verb. The button repeats the verb.

| Before | After |
| --- | --- |
| Are you sure? [Yes] [No] | Delete 3 Tasks? [Delete] [Cancel] |
| Warning! This cannot be undone. [OK] | Delete All Data? You can't undo this. [Delete All Data] [Cancel] |

## Undo

Name every undoable action in its Snackbar: "Task deleted" with an Undo
action, not "Done". Use `SnackbarDuration.Long` when the Snackbar has an
action, so people have time to tap it.

## Settings

Label the setting by what it does. Describe the on state only; people
infer the off state.

| Before | After |
| --- | --- |
| Enable notifications for tasks that are due while the app is closed | Notify me when a task is due |
| Persistence | Keep my data on this device |

## Fields and hints

Every field has a visible label (`label` in `OutlinedTextField`). The
placeholder shows an example or the format, never the only label: it
disappears when people type.

| Before | After |
| --- | --- |
| (placeholder only) Email | Label "Email", placeholder "name@example.com" |
| Enter date | Label "Due Date", placeholder "Tomorrow, Friday, Oct 30" |

## Permissions

Ask at the moment of first use, after a tap, never at launch. Show one
line first that says how and why, then the system dialog.

| Before | After |
| --- | --- |
| Allow Daily Log to send you notifications? (system dialog only) | Notifications tell you when a task is due. [Turn On Notifications] |
| Allow storage access? | (Use the document picker. It needs no permission.) |

When people said no, say how to turn it on in system settings once, with a
button that opens them (`Settings.ACTION_APP_NOTIFICATION_SETTINGS` for
notifications), and keep the app working without it.

## Progress

Say what is happening, with a count when there is one.

| Before | After |
| --- | --- |
| Loading... | Importing 12 notes... |
| Processing | Summarizing your notes... |
| Please wait | Exporting "Q3 Report"... |

## Tooltips and labels

Touch has no hover, so the rule covers two things.

**Content descriptions** (read by TalkBack) on every icon-only control. Say
what the control does, starting with a verb: "Add a task", "Remove star".
Do not include the word "button": the role says it. Do not repeat a
visible label. Decorative icons next to text use `contentDescription = null`.

| Before | After |
| --- | --- |
| (none) | Add a task |
| Plus button | Add a task |
| Star button | Remove star (when starred), Star (when not) |

**Tooltips** appear on long press of an icon button (`TooltipBox` with a
`PlainTooltip`). They carry the same text as the description, in sentence case with no period, at
most 60 to 75 characters. An information tooltip (why a control is disabled)
just shows that information.

## Counts and plurals

Use plural resources, so counts read right in every language:

```xml
<plurals name="tasks_left">
    <item quantity="one">%d task left</item>
    <item quantity="other">%d tasks left</item>
</plurals>
```

Read them with `pluralStringResource(R.plurals.tasks_left, count, count)`.

## Words to cut

- "we", "our": unclear who that is. "Unable to sync", not "We couldn't sync".
- "Oops", "Uh-oh", "Whoops": sound insincere in an error.
- "Simply", "just", "easily": they blame people who find it hard.
- "Please": once in a while is fine, every message is noise.
- "Your", "my": "Favorites" already means yours.
- Code words: entity, record, payload, null, exception, 404, SQLite.
