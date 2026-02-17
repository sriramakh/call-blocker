# BlockNos — UI/UX Style Guide

Based on the reference design (lake monitoring app with teal/green palette, glassmorphism cards, and clean data presentation).

---

## Color Palette

| Token              | Hex       | Usage                                      |
|---------------------|-----------|---------------------------------------------|
| `primary`          | `#0D7C66` | Headers, primary buttons, active states     |
| `primary-dark`     | `#065A4A` | Status bar, pressed states, dark accents    |
| `primary-light`    | `#B2DFDB` | Tinted backgrounds, selected card fills     |
| `surface`          | `#FFFFFF` | Card backgrounds                            |
| `background`       | `#E8F5F1` | Screen/page background (soft mint wash)     |
| `on-primary`       | `#FFFFFF` | Text/icons on primary-colored surfaces      |
| `on-surface`       | `#1B1B1F` | Primary body text on white/light surfaces   |
| `on-surface-muted` | `#5F6368` | Secondary/description text, timestamps      |
| `divider`          | `#D6E5E1` | Card borders, section separators            |
| `card-stroke`      | `#C8DDD8` | Subtle card outlines                        |
| `input-bg`         | `#EFF5F3` | Text field backgrounds                      |
| `accent`           | `#E94560` | Destructive actions (block/delete icons)    |
| `success`          | `#0D7C66` | Confirmation badges, checkmarks, toggles on |
| `badge-bg`         | `#D4EDDA` | Light badge/chip background                 |

---

## Typography

| Style            | Size   | Weight   | Tracking | Usage                                  |
|-------------------|--------|----------|----------|----------------------------------------|
| **Display**      | 28 sp  | Bold     | 0.02     | Screen titles ("BlockNos" header)      |
| **Headline**     | 18 sp  | SemiBold | 0        | Card titles, section names             |
| **Section Label**| 11–13 sp | Bold   | 0.08     | Uppercase section headers              |
| **Body**         | 14–15 sp | Regular | 0        | Primary content, list item titles      |
| **Caption**      | 11–12 sp | Regular | 0        | Timestamps, descriptions, helper text  |
| **Button**       | 14 sp  | Medium   | 0.02     | Button labels (sentence case, no ALLCAPS) |

**Font family:** System default (Roboto on Android). Clean sans-serif throughout.

---

## Layout & Spacing

- **Screen padding:** 16 dp horizontal
- **Card margin:** 16 dp horizontal, 10–12 dp vertical between cards
- **Card internal padding:** 16 dp all sides
- **Section gap:** 20 dp before a new logical section (e.g., lists after inputs)
- **Element spacing inside cards:** 8–12 dp between elements
- **Header area:** Full-width colored banner, 48 dp top padding (accounts for status bar), 24 dp bottom padding

---

## Cards

- **Corner radius:** 12–16 dp (soft, rounded)
- **Elevation:** 0 dp (flat, no drop shadows)
- **Border:** 1 dp stroke using `card-stroke` color
- **Background:** Pure white (`surface`)
- **Selected/active card:** Light tinted fill (`primary-light`) with `primary` stroke, subtle checkmark badge

### Card Variants
1. **Input card** — Section label (uppercase, muted) + input row (text field + action button)
2. **List card** — Section label + scrollable list of items, empty state text centered
3. **Info card** — Rich data display with metrics, badges, toggles (reference: "Active Generators" panel)
4. **Selection card** — Icon + title + description + radio/check indicator (reference: left panel list)

---

## Buttons

### Primary (Filled)
- Background: `primary` (#0D7C66)
- Text: white, 14 sp, sentence case
- Corner radius: 8–10 dp
- Height: 44 dp
- Full-width for prominent actions ("Next"), inline for field actions ("Block")

### Outlined
- Border: 1 dp `primary`
- Text: `primary` color
- Background: transparent
- Corner radius: 8–10 dp
- Height: 44 dp

### Text Button
- No background or border
- Text: `primary` or `accent` color, 12 sp
- Used for secondary actions ("Refresh", "Clear")
- Minimal padding, compact

---

## Inputs

- **Height:** 44 dp
- **Background:** `input-bg` (soft mint/gray)
- **Corner radius:** 8 dp (or matching card radius)
- **Text color:** `on-surface` (dark)
- **Hint color:** `on-surface-muted`
- **Font size:** 15 sp
- **Padding:** 12 dp horizontal
- **No visible border** — relies on fill contrast against white card

---

## Icons & Imagery

- **Style:** Outlined, minimal line icons (not filled/heavy)
- **Size:** 20–24 dp in lists, 36 dp for action buttons
- **Tint:** `primary` for informational, `accent` (red) for destructive actions
- **Badge:** Small circular badge with count, positioned top-right on icon (reference: notification bell)
- **Decorative icons:** Circular colored background with a small icon inside for list items (reference: left panel categories)

---

## Toggles & Switches

- **Style:** Material Switch
- **Active track:** `primary`
- **Active thumb:** white
- **Inactive track:** muted gray
- **Placement:** Right-aligned within a row, label to the left

---

## Lists

- **Item height:** ~48–56 dp (auto based on content)
- **Padding:** 16 dp horizontal, 10 dp vertical per item
- **Title:** 14–15 sp, `on-surface`, bold for emphasis
- **Subtitle:** 12 sp, `on-surface-muted`
- **Trailing action:** Icon button (block/delete), 36 dp touch target
- **Dividers:** None between items (card boundary is sufficient), optional thin divider within a card for grouped sections
- **Empty state:** Centered text, 13 sp, muted color, 20 dp vertical padding

---

## Header / App Bar

- **Style:** Full-bleed colored banner (not a toolbar)
- **Background:** `primary` solid fill
- **Title:** 28 sp, bold, white
- **Subtitle/summary:** 13 sp, semi-transparent white or light muted tint
- **No elevation/shadow** — seamless transition to content via colored edge

---

## Motion & Interaction

- **Feedback:** Snackbar from bottom for confirmations ("xxx blocked successfully")
- **Ripple:** Standard `selectableItemBackground` on tappable rows
- **Transitions:** Default Activity transitions, no custom animations needed
- **List updates:** DiffUtil-based smooth item insertion/removal

---

## Accessibility

- **Touch targets:** Minimum 44×44 dp for all interactive elements
- **Contrast:** All text meets WCAG AA (4.5:1 for body, 3:1 for large text)
- **Content descriptions:** All icon buttons have `contentDescription`
- **Scrolling:** `nestedScrollingEnabled=false` on RecyclerViews inside ScrollView for smooth UX

---

## Design Principles (from Reference)

1. **Clean data hierarchy** — Large prominent metrics, smaller supporting details
2. **Card-based grouping** — Each logical section lives in its own rounded card
3. **Flat & borderless** — No shadows, rely on subtle strokes and background contrast
4. **Teal/green palette** — Calm, professional, nature-inspired
5. **Generous whitespace** — Content breathes, nothing feels cramped
6. **Progressive disclosure** — Show summary first, details on interaction
7. **Consistent radius** — Same 12 dp corner radius on every card and container
