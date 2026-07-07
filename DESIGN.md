---
name: SYNC
description: >
  Design system for SYNC — a forum-shaped team knowledge base built to fight
  knowledge rot. Clean, professional, dark-first, with a deep emerald brand.
  The visual language exists to make knowledge feel findable and trustworthy.
tokens_spec: design.md (Google) — YAML tokens are normative; prose explains intent.

colors:
  # Brand — Evergreen (deep, blue-leaning emerald; primary interaction color)
  evergreen-50:  "#E9F7F1"
  evergreen-100: "#C6ECDD"
  evergreen-200: "#97DCC1"
  evergreen-300: "#5FC6A0"
  evergreen-400: "#2DAA80"
  evergreen-500: "#12906A"
  evergreen-600: "#0B7A5B"   # primary — filled buttons, active nav (white text passes AA)
  evergreen-700: "#0A6350"   # hover / pressed
  evergreen-800: "#0A4D3F"
  evergreen-900: "#073B31"
  evergreen-950: "#04231D"

  # Semantic roles (default = dark theme; see [data-theme=light] in the CSS block)
  brand:         "{colors.evergreen-600}"
  brand-hover:   "{colors.evergreen-700}"
  on-brand:      "#FFFFFF"
  surface-canvas:"#0B0C0E"
  surface-raised:"#16181C"
  surface-overlay:"#26292E"
  text-primary:  "#ECEDEE"
  text-secondary:"#A4A9AF"
  text-muted:    "#7E848B"
  border:        "rgba(255,255,255,0.08)"
  border-strong: "rgba(255,255,255,0.14)"

  # Functional — success reuses the brand green on purpose (freshness = the thesis)
  success:       "{colors.evergreen-500}"
  warning:       "#E08D0B"    # aging / stale
  danger:        "#E14B48"    # unanswered / destructive

  # Categorical — post types (kept clear of green so they never read as "success")
  type-short:    "#8A9099"    # slate — low-ceremony microblog
  type-question: "#4C8FD6"    # blue — Stack-Overflow-style Q&A
  type-long:     "#8B72E0"    # purple — durable guides / decisions

typography:
  family-sans: "Inter, ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, sans-serif"
  family-mono: "JetBrains Mono, ui-monospace, SFMono-Regular, Menlo, monospace"
  display:  { fontFamily: sans, fontSize: 30px, fontWeight: 600, lineHeight: 1.2,  letterSpacing: -0.02em }
  h1:       { fontFamily: sans, fontSize: 24px, fontWeight: 600, lineHeight: 1.25, letterSpacing: -0.01em }
  h2:       { fontFamily: sans, fontSize: 20px, fontWeight: 600, lineHeight: 1.3 }
  h3:       { fontFamily: sans, fontSize: 16px, fontWeight: 600, lineHeight: 1.4 }
  body-lg:  { fontFamily: sans, fontSize: 16px, fontWeight: 400, lineHeight: 1.6 }
  body:     { fontFamily: sans, fontSize: 14px, fontWeight: 400, lineHeight: 1.55 }  # default UI text
  body-sm:  { fontFamily: sans, fontSize: 13px, fontWeight: 400, lineHeight: 1.5 }
  label:    { fontFamily: sans, fontSize: 12px, fontWeight: 500, lineHeight: 1.3, letterSpacing: 0.04em }  # section headers
  caption:  { fontFamily: sans, fontSize: 12px, fontWeight: 400, lineHeight: 1.4 }   # metadata
  code:     { fontFamily: mono, fontSize: 13px, fontWeight: 400, lineHeight: 1.6 }

spacing:   { 1: 4px, 2: 8px, 3: 12px, 4: 16px, 5: 20px, 6: 24px, 8: 32px, 10: 40px, 12: 48px, 16: 64px }
rounded:   { sm: 6px, md: 8px, lg: 12px, xl: 16px, full: 999px }
elevation: { sm: "0 1px 2px rgba(0,0,0,0.30)", md: "0 4px 12px rgba(0,0,0,0.35)", popover: "0 8px 24px rgba(0,0,0,0.45)", focus: "0 0 0 3px rgba(11,122,91,0.45)" }

layout:
  sidebar-width: 240px
  content-max:   1200px
  density:       comfortable   # dense enough for a knowledge tool, calm enough to read
---

# SYNC — Design System

This is the source of truth for how SYNC looks and feels. The YAML above is
normative (exact token values); the prose below explains **how to apply them and
when not to**. Point your coding agent (Claude Code, Cursor) at this file, and
paste the CSS block in *Color* into your global stylesheet.

SYNC is a search-and-retrieval + real-time collaboration system that wears a
forum. The interface has one job: make knowledge feel **findable** and
**trustworthy**. Every visual decision serves that, or it gets cut.

---

## Design principles

1. **Clean over decorated.** Flat surfaces, hairline borders, generous
   whitespace, no gradients or ornament. Depth comes from tonal surface steps
   and borders, not heavy shadows — especially in dark mode.
2. **Calm density.** This is a working tool people read for a long time. Pack
   information without noise: one weight of emphasis, quiet neutrals, and color
   reserved for meaning.
3. **Green means action; green also means trust.** Evergreen is the single
   brand color. It drives interaction (primary buttons, active nav) *and*
   doubles as the "verified / fresh" signal — because freshness is SYNC's whole
   thesis. Everything else stays gray. Warnings and errors use amber and red so
   they never blur with the brand.
4. **Spend boldness in one place — the signature is trust.** SYNC's memorable
   element is not a font or a hero; it's the **freshness/trust visual language**
   (verified badges, staleness flags, the freshness meter). Make that
   expressive and consistent; keep everything around it disciplined.
5. **Never rely on hue alone.** Status is always color **plus** an icon or a
   word. A stale flag is amber *and* a clock icon *and* "needs review." This is
   both accessibility and clarity.
6. **Sentence case, plain verbs, user's vocabulary.** "Ask a question,"
   "Verify," "Follow" — not "Submit" or "Configure."

---

## Foundations

### Color

SYNC ships **dark-first** (the primary theme). The `:root` block below *is* the
dark theme; `[data-theme="light"]` overrides it. All components reference the
semantic tokens (`--surface-*`, `--text-*`, `--brand`), never raw hex — so
theming is a token swap, not a find-and-replace.

```css
:root {
  /* ── Brand · Evergreen ─────────────────────────────── */
  --evergreen-50:#E9F7F1; --evergreen-100:#C6ECDD; --evergreen-200:#97DCC1;
  --evergreen-300:#5FC6A0; --evergreen-400:#2DAA80; --evergreen-500:#12906A;
  --evergreen-600:#0B7A5B; --evergreen-700:#0A6350; --evergreen-800:#0A4D3F;
  --evergreen-900:#073B31; --evergreen-950:#04231D;

  --brand:            var(--evergreen-600);   /* filled actions, active nav   */
  --brand-hover:      var(--evergreen-700);
  --brand-active:     var(--evergreen-800);
  --brand-accent:     var(--evergreen-400);   /* links / accents on dark      */
  --on-brand:         #FFFFFF;
  --brand-tint:       #10261F;                /* subtle green fill on dark     */
  --brand-tint-text:  var(--evergreen-300);

  /* ── Surfaces (dark) ───────────────────────────────── */
  --surface-canvas:   #0B0C0E;                /* page background              */
  --surface-raised:   #16181C;                /* cards                        */
  --surface-overlay:  #26292E;                /* menus, popovers, dialogs     */
  --surface-inset:    #101215;                /* wells, code-block background  */

  /* ── Text (dark) ───────────────────────────────────── */
  --text-primary:     #ECEDEE;                /* off-white, not pure white     */
  --text-secondary:   #A4A9AF;
  --text-muted:       #7E848B;                /* metadata, placeholders        */

  /* ── Borders (dark) — carry separation in place of shadow */
  --border:           rgba(255,255,255,0.08);
  --border-strong:    rgba(255,255,255,0.14);

  /* ── Functional — success reuses the brand ─────────── */
  --success:          var(--evergreen-500);
  --success-tint:     #10261F;  --success-text:  var(--evergreen-300);
  --warning:          #E08D0B;
  --warning-tint:     #33260B;  --warning-text:  #F0A83C;
  --danger:           #E14B48;  --danger-hover:  #C93B39;
  --danger-tint:      #351614;  --danger-text:   #EE726F;

  /* ── Categorical — post types ──────────────────────── */
  --type-short:    #8A9099;  --type-short-tint:    #1D2024;
  --type-question: #4C8FD6;  --type-question-tint: #122234;
  --type-long:     #8B72E0;  --type-long-tint:     #1F1A33;

  /* ── Shape · spacing · elevation · type ────────────── */
  --radius-sm:6px; --radius-md:8px; --radius-lg:12px; --radius-xl:16px; --radius-full:999px;
  --space-1:4px; --space-2:8px; --space-3:12px; --space-4:16px; --space-5:20px;
  --space-6:24px; --space-8:32px; --space-10:40px; --space-12:48px; --space-16:64px;
  --shadow-sm:0 1px 2px rgba(0,0,0,.30);
  --shadow-md:0 4px 12px rgba(0,0,0,.35);
  --shadow-popover:0 8px 24px rgba(0,0,0,.45);
  --focus-ring:0 0 0 3px rgba(11,122,91,.45);
  --font-sans:'Inter',ui-sans-serif,system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;
  --font-mono:'JetBrains Mono',ui-monospace,'SFMono-Regular',Menlo,monospace;
}

[data-theme="light"] {
  --surface-canvas:#F7F8F8; --surface-raised:#FFFFFF;
  --surface-overlay:#FFFFFF; --surface-inset:#F1F3F3;
  --text-primary:#16181C; --text-secondary:#565B61; --text-muted:#9A9EA4;
  --border:rgba(16,18,20,.10); --border-strong:rgba(16,18,20,.18);
  --brand-accent:var(--evergreen-600);
  --brand-tint:var(--evergreen-50); --brand-tint-text:var(--evergreen-800);
  --success-tint:var(--evergreen-50); --success-text:var(--evergreen-700);
  --warning-tint:#FBF1DD; --warning-text:#8A560A;
  --danger-tint:#FBEAEA;  --danger-text:#A32E2C;
  --type-short-tint:#EEF0F2; --type-question-tint:#E7F0FA; --type-long-tint:#EFEBFB;
  --shadow-sm:0 1px 2px rgba(16,18,20,.06);
  --shadow-md:0 4px 12px rgba(16,18,20,.08);
  --shadow-popover:0 8px 24px rgba(16,18,20,.12);
}
```

**Brand ramp usage**

| Stop | Hex | Use |
|------|-----|-----|
| 400 | `#2DAA80` | Links / accents **on dark** surfaces |
| 500 | `#12906A` | Success / verified accent |
| 600 | `#0B7A5B` | **Primary** — filled buttons, active nav (white text passes AA ~5.3:1) |
| 700 | `#0A6350` | Hover / pressed |
| 800 | `#0A4D3F` | Tint text **on light**, deep fills |
| 50–100 | `#E9F7F1`/`#C6ECDD` | Light-mode tint backgrounds (chips, selected rows) |

**Rules**

- **One accent per view.** At most one `--brand`-filled control in a given
  region; secondary actions are ghost/outline, everything else neutral. A
  screen full of green buttons destroys the meaning of green.
- **Brand vs. success.** They share the green intentionally. Use `--brand` for
  *interaction* (a button, an active tab) and `--success`/`--success-text` for
  *state* (verified, fresh, saved). Contexts differ, so the overlap reads as
  coherence, not confusion.
- **Status is never hue-only.** Pair every colored status with an icon and a
  label (see *Freshness badge*, *Post-type badge*).
- **On dark, borders do the work of shadows.** Prefer a `--border` hairline and
  a surface step over drop shadows; reserve shadows for true floating layers
  (menus, dialogs).
- **Verify pairings before shipping.** Target WCAG AA — 4.5:1 for text, 3:1 for
  non-text (borders, icons, focus rings). Re-check after any brand tweak.

### Typography

`Inter` for all UI and body — it's neutral, dense-legible, and professional,
which is exactly right for a reading-heavy tool; the personality lives in the
brand and the trust UI, not an exotic face. `JetBrains Mono` for code, because
fenced code blocks with syntax highlighting are a first-class post feature and
deserve a real developer mono.

| Token | Size / Weight / LH | Use |
|-------|--------------------|-----|
| `display` | 30 / 600 / 1.2 | Empty states, marketing/landing only |
| `h1` | 24 / 600 / 1.25 | Page title (post title, project name) |
| `h2` | 20 / 600 / 1.3 | Section heading |
| `h3` | 16 / 600 / 1.4 | Card / subsection heading |
| `body-lg` | 16 / 400 / 1.6 | Long-form post reading |
| `body` | 14 / 400 / 1.55 | **Default UI text** |
| `body-sm` | 13 / 400 / 1.5 | Dense lists, table cells |
| `label` | 12 / 500 / 1.3, +0.04em | Sidebar section headers (e.g. "YOUR PROJECTS") |
| `caption` | 12 / 400 / 1.4 | Metadata (timestamps, counts, "@handle") |
| `code` | 13 / 400 / 1.6 mono | Inline code and code blocks |

Two weights only: **400** and **600**. Sentence case everywhere except `@handles`
and the letter-spaced section labels. Never Title Case, never ALL CAPS body.

### Spacing & layout

4px base with an 8px rhythm: `--space-1..16` (4, 8, 12, 16, 20, 24, 32, 40, 48,
64). Use the scale for every margin, padding, and gap — no arbitrary pixel
values. Sidebar is **240px**; content column caps at **1200px** and centers.
Card internal padding is `--space-4` (16px) for dense rows, `--space-5/6` for
roomy cards.

### Shape

`--radius-sm` (6px) for small controls, `--radius-md` (8px) for buttons /
inputs / chips, `--radius-lg` (12px) for cards, `--radius-xl` (16px) for
dialogs, `--radius-full` for pills, avatars, and vote buttons. **Never round a
single-sided border** — an accent that uses only `border-left` must have
`border-radius: 0`.

### Elevation

Flat by default. Depth ladder: `--surface-canvas` → `--surface-raised` →
`--surface-overlay`, with a `--border` hairline for separation. Shadows only for
things that truly float: `--shadow-md` on hover-lifted cards, `--shadow-popover`
on menus and dialogs. On dark, keep shadows subtle and let the surface step +
border carry the hierarchy. Focus is always visible: `box-shadow: var(--focus-ring)`.

### Iconography

Outline icon set (Tabler or Lucide), stroke ~1.5px. 16px inline, 18–20px for
nav and buttons, 24px max decorative. Icons inherit `currentColor`. Every
status icon travels with its label. Post types and status each have a fixed icon
(below) — keep them consistent across the app so they become learnable shorthand.

### Motion

Quick and functional: 120–160ms ease-out for hovers, presses, and popovers;
200–240ms for entrances. Real-time arrivals (new post/answer, presence) fade in
gently — never slide or bounce. Respect `prefers-reduced-motion`: drop
transforms, keep opacity.

---

## Components

Every component references semantic tokens. Intent first, then the states that
matter.

### Buttons

- **Primary** — `background: var(--brand)`, `color: var(--on-brand)`, hover
  `--brand-hover`, `--radius-md`, `active: scale(.98)`. One per region.
- **Secondary** — transparent fill, `1px solid var(--border-strong)`,
  `color: var(--text-primary)`, hover `background: var(--surface-raised)`.
- **Ghost** — no border, hover `background: var(--surface-raised)`. For toolbar
  and inline actions.
- **Danger** — `background: var(--danger)` (destructive confirms only), or a
  ghost with `color: var(--danger-text)`.
- Height 36px default, 32px compact. Disabled is rare — prefer keeping enabled
  and responding on use.

### Sidebar (two modes)

The sidebar **swaps contents** by context; the top block (logo + Ask/Write) and
the bottom (Settings) stay identical across both so the app feels like one
surface going deeper.

- **Global home** (no project): logo → home · `Ask / Write` · nav: Home, Feed
  (merged), Explore, Saved · **Your projects** list · **Following** list ·
  Settings. No project-scoped items, no presence.
- **Inside a project**: project switcher (`Duobam ▾`) · `Ask / Write` · nav:
  Overview, Feed, Questions, Guides, Tags · **Maintain**: Needs review, My
  contributions · presence footer ("3 online").
- Section headers use the `label` token. Nav width 240px.

The `sync` wordmark (top-left) is always **home** — clicking it deselects any
project. The switcher moves sideways; "Overview" goes down into a project.

### Nav item

- Default: `color: var(--text-secondary)`, transparent, `--radius-md`, 15px icon.
- Hover: `background: var(--surface-raised)`, `color: var(--text-primary)`.
- **Active**: `background: var(--brand)`, `color: var(--on-brand)`.
- Count badge (unanswered, needs-review): pill, `--radius-full`, `caption`
  size — danger tint for unanswered, warning tint for stale.

### Project switcher

Dropdown grouped **Your projects** (member, avatar tile) / **Following**
(public, globe icon) with a footer row `Explore public projects →` in
`--brand-accent`. Current project shows a check. It switches among things you
care about — it is **not** the discovery mechanism (that's Explore).

### Search

First-class and prominent — findability is the product. Persistent field in the
top bar, `36px`, `--surface-inset` fill, leading search icon, `⌘K` hint. On the
Explore page and empty states, promote it to a hero-width field. Placeholder is
a real example ("rotate staging DB credentials"), not "Search…".

### Cards

- **Health / metric card** — `--surface-raised` (or `--surface-inset` for a
  quieter tile), `--radius-lg`, `--space-4` padding. `caption` label on top,
  24/600 number below, a colored sub-stat beneath (e.g. `--danger-text` "4
  unanswered 7+ days", `--warning-text` "9 need review", `--success-text` "+5
  canonical"). Use for **knowledge-health** metrics, never follower vanity.
- **Raised card** — `--surface-raised`, `1px solid var(--border)`,
  `--radius-lg`. Wraps bounded objects (project card, post preview).

### Attention-queue row

A bordered row (not a floating card) with a leading **status badge**, the item
title (truncated), muted metadata, and a trailing brand-text action
("Answer →", "Verify →"). Badges: `Unanswered` = danger tint, `Stale · yours` =
warning tint, `Mention` = neutral/type-question tint. This panel is the app's
signal of aliveness and trust — give it prominence on both homes.

### Post-type badge (categorical)

Small pill, tint background + matching text, fixed icon per type:

| Type | Token | Icon | Feel |
|------|-------|------|------|
| Short | `--type-short` | `ti-message-circle` | Low-ceremony update |
| Question | `--type-question` | `ti-help-circle` | Q&A |
| Long | `--type-long` | `ti-book` | Durable guide / decision |

Kept clear of green so a post type never reads as a success state.

### Freshness badge — **the signature**

The one place to be expressive. A pill that states verification state as
color **+ icon + words**, driven by `last_verified_at` vs. the staleness
threshold:

| State | Tokens | Icon | Label |
|-------|--------|------|-------|
| Fresh / verified | `--success-tint` / `--success-text` | `ti-shield-check` | "Verified 3d ago" |
| Aging | `--warning-tint` / `--warning-text` | `ti-clock` | "Verify soon" |
| Stale | `--danger-tint` / `--danger-text` | `ti-alert-triangle` | "Needs review" |

Pair it with a one-click **Verify** action and, on directory/Explore cards, a
**freshness %** stat — the cue that lets someone trust a knowledge base at a
glance. Treat this family as SYNC's brand moment; keep it perfectly consistent.

### Tag chip

Neutral by default — `--surface-raised` fill, `--border`, `--radius-full`,
`caption` size — so tags don't compete with brand actions. The **selected**
filter chip flips to `--brand` fill / `--on-brand`. Tags are the primary
organizing axis; make them easy to scan, not loud.

### Vote control

Compact, stacked up/down with a count. Idle `--text-muted`; active up-vote
`--brand-accent`; the accepted/canonical answer is marked with a
`--success`-toned check. Voting feeds ranking — it's the social signal in SYNC,
so keep it quiet and ever-present rather than flashy.

---

## Content & voice

Intelligent, warm, and plain — the smartest teammate explaining something
clearly. Sentence case, active voice, verb-first.

- **Buttons** name the action and keep the name through the flow: "Publish" →
  toast "Published." Not "Submit," not "OK."
- **Empty states are invitations**, not apologies: "Ask your team's first
  question" with a CTA — never "Nothing here yet."
- **Errors** say what happened and what to do, in the product's voice, no first
  person: "Couldn't reach the workspace. Retry." No raw exceptions.
- **Name things by what people control**: "Notifications," not "webhook config";
  "Following," not "subscription entity." Avoid "please," "simply,"
  "successfully," and corporate filler.
- **Pronouns**: the user's things are "your projects" (never "my"); confirmations
  are past tense ("Saved"); "I" is reserved for chat/AI surfaces, not system UI.

---

## Accessibility

- **Contrast**: AA minimum — 4.5:1 text, 3:1 non-text (borders, icons, focus
  rings, chart marks). Re-verify every pairing after brand changes.
- **Never hue-only**: status = color + icon + label, always (critical for the
  freshness system and for color-blind users).
- **Focus is visible**: `--focus-ring` on every interactive element; never
  remove outlines without an equivalent.
- **Dark-mode care**: off-white text (`--text-primary`), near-black canvas — no
  pure `#FFF` paragraphs on pure `#000` (halation). Low-opacity borders, not
  opacity overlays, for surface separation.
- **Motion**: honor `prefers-reduced-motion`. **Targets**: ≥ 40px hit area on
  touch. **Keyboard**: full nav, `⌘K` search, roving focus in lists.

---

## Dark mode

Dark is the **primary** theme (the `:root` block is dark). Light is a supported
override via `[data-theme="light"]`. Both map to the same semantic token names,
so components never branch on theme — only the token values change. When adding
a color, add it to **both** themes and check contrast in each; a token that only
works in one mode is a bug.
