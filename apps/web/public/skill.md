# SYNC for Agents

Create draft posts on [SYNC](https://sync.skkil.org) on behalf of the person you
are working for.

## Connect

Add SYNC as a remote MCP server:

```
https://sync.skkil.org/mcp
```

That is the whole setup. The server advertises its own authorization server, so
your client runs the standard OAuth flow the first time it connects: a SYNC page
opens in the browser, the human logs in and approves, and your client receives a
token. There is no API key to paste and no code to copy.

If your client asks for a command instead of a URL, it does not support remote
MCP servers over Streamable HTTP, and SYNC will not work with it.

## What you can do

One tool: `create_post`.

| Argument        | Required                | Notes                           |
| --------------- | ----------------------- | ------------------------------- |
| `type`          | yes                     | `SHORT`, `LONG`, or `QUESTION`  |
| `title`         | for `LONG` / `QUESTION` | omit for `SHORT`                |
| `bodyMarkdown`  | yes                     | GitHub-flavored Markdown        |
| `tags`          | no                      | tag names                       |
| `projectHandle` | no                      | omit for a personal post        |
| `projectTags`   | no                      | ignored without `projectHandle` |

**Every post is created as a draft.** Nothing you create is ever published
automatically — the human reviews it and publishes it themselves on SYNC. The
tool returns a `reviewUrl`; show it to them.

Posting into a project requires that the human already be a member of it. If
they are not, the call fails; do not try to join on their behalf.

## Writing the body

`bodyMarkdown` supports headings, lists (including task lists), tables, fenced
code blocks with a language, bold/italic/strikethrough, links, and blockquotes.

**Images and embeds are not supported.** Do not include image syntax — it will
not render.

## What to write

SYNC is for knowledge worth keeping, not status updates. A good post explains
something a reader can act on: what the problem was, what you tried, what
actually worked, and why. Prefer one specific, verified example over a general
survey.

Draft in the human's voice, not yours. You are writing something they will put
their name on — if you are unsure about a claim, leave it out or mark it as
uncertain rather than stating it confidently.
