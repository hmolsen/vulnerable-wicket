# WicketAds Design System — MASTER

> Pattern: **Marketplace / Directory** · Style: **Swiss Minimalism** · Stack: HTML/CSS + Bootstrap Icons

---

## 1. Color Tokens

### Raw Palette
| Alias | Hex | Usage |
|-------|-----|-------|
| `--palette-navy-950` | `#0f172a` | Navbar background |
| `--palette-navy-900` | `#1e293b` | Body text, table headers |
| `--palette-navy-700` | `#334155` | Secondary text |
| `--palette-navy-400` | `#64748b` | Muted text, labels |
| `--palette-navy-200` | `#94a3b8` | Placeholders, nav muted |
| `--palette-navy-100` | `#cbd5e1` | Nav text |
| `--palette-navy-50`  | `#e2e8f0` | Borders, dividers |
| `--palette-navy-25`  | `#f0f4f8` | Page background |
| `--palette-blue-700` | `#1d4ed8` | Primary hover |
| `--palette-blue-600` | `#2563eb` | Primary interactive |
| `--palette-blue-100` | `#dbeafe` | Primary subtle bg |
| `--palette-orange-500` | `#f97316` | Accent |
| `--palette-orange-600` | `#ea580c` | Accent hover |
| `--palette-green-600` | `#22c55e` | Success, prices |
| `--palette-red-500`  | `#ef4444` | Danger / errors |
| `--palette-violet-600` | `#7c3aed` | Admin / special |

### Semantic Aliases
| Token | Maps To | Meaning |
|-------|---------|---------|
| `--color-bg` | navy-25 | Page canvas |
| `--color-surface` | white | Cards, panels |
| `--color-nav-bg` | navy-950 | Sticky navbar |
| `--color-primary` | blue-600 | Main CTA, links |
| `--color-primary-hover` | blue-700 | Hover state |
| `--color-primary-subtle` | blue-100 | Badge / chip bg |
| `--color-accent` | orange-500 | Secondary CTA |
| `--color-success` | green-600 | Prices, confirmations |
| `--color-danger` | red-500 | Errors, delete |
| `--color-admin` | violet-600 | Admin UI only |
| `--color-text` | navy-900 | Primary body |
| `--color-text-secondary` | navy-700 | Secondary body |
| `--color-text-muted` | navy-400 | Labels, captions |
| `--color-border` | navy-50 | Card/divider borders |
| `--color-border-strong` | navy-100 | Input borders |
| `--color-focus-ring` | blue-600 | Keyboard focus |

### Contrast compliance (WCAG AA 4.5:1)
- `--color-text` on `--color-bg` → **15.3:1** ✓
- `--color-primary` on white → **4.7:1** ✓
- `--color-success` on white → **3.0:1** (large text / UI only)
- `--color-danger` on `--color-danger-subtle` → **4.9:1** ✓
- White on `--color-nav-bg` → **18.1:1** ✓

---

## 2. Typography Scale

| Token | rem | px | Use |
|-------|-----|----|-----|
| `--text-xs`   | 0.75  | 12 | Timestamps, uppercase labels, badges |
| `--text-sm`   | 0.875 | 14 | Secondary body, table cells, captions |
| `--text-base` | 1.0   | 16 | Primary body copy |
| `--text-md`   | 1.125 | 18 | Lead paragraphs, card titles |
| `--text-lg`   | 1.25  | 20 | Section headings (`h3`) |
| `--text-xl`   | 1.5   | 24 | Page headings (`h2`) |
| `--text-2xl`  | 1.875 | 30 | Hero sub-heading |
| `--text-3xl`  | 2.25  | 36 | Hero heading (`h1`) |
| `--text-4xl`  | 3.0   | 48 | Display / marketing |

**Font family:** `Open Sans` (400 body · 500 label · 600 subheading · 700 heading)  
**Line-height:** `1.5` body · `1.35` cards · `1.2` display  
**Letter-spacing:** normal body · `--tracking-wider (.08em)` uppercase labels · `--tracking-tight (-.02em)` display headings

---

## 3. Spacing Scale (8px base)

| Token | rem | px |
|-------|-----|----|
| `--space-1` | 0.25 | 4 |
| `--space-2` | 0.5  | 8 |
| `--space-3` | 0.75 | 12 |
| `--space-4` | 1.0  | 16 |
| `--space-5` | 1.25 | 20 |
| `--space-6` | 1.5  | 24 |
| `--space-8` | 2.0  | 32 |
| `--space-10` | 2.5 | 40 |
| `--space-12` | 3.0 | 48 |
| `--space-16` | 4.0 | 64 |
| `--space-20` | 5.0 | 80 |
| `--space-24` | 6.0 | 96 |

**Rule:** always use a space token — never a raw pixel value in component CSS.

---

## 4. Shadow Scale

| Token | When to use |
|-------|-------------|
| `--shadow-xs` | Resting cards in a dense list |
| `--shadow-sm` | Default card, input focus ring base |
| `--shadow-md` | Raised card on hover |
| `--shadow-lg` | Ad card lift on hover |
| `--shadow-xl` | Auth modal, dropdowns |
| `--shadow-inner` | Inset inputs, pressed states |

---

## 5. Border-Radius Scale

| Token | px | Use |
|-------|----|-----|
| `--radius-none` | 0 | Tables, full-bleed banners |
| `--radius-sm`   | 4 | Buttons (sm), pills |
| `--radius-md`   | 8 | Buttons (default), inputs |
| `--radius-lg`   | 12 | Cards, panels |
| `--radius-xl`   | 16 | Auth card, modal |
| `--radius-2xl`  | 24 | Hero card callouts |
| `--radius-full` | 9999 | Badges, avatars, toggles |

---

## 6. Component States

Every interactive element must implement all five states:

| State | Visual rule |
|-------|-------------|
| **Default** | Base token values |
| **Hover** | Darken bg by one step OR apply `--state-hover-overlay`; `transition: 150ms ease-out` |
| **Active/Pressed** | `transform: scale(.97)`; darken further |
| **Focus** | `outline: 2px solid --color-focus-ring; outline-offset: 2px` (never remove outline) |
| **Disabled** | `opacity: 0.42; cursor: not-allowed; pointer-events: none` |

---

## 7. Icon Guidelines (Bootstrap Icons)

### Size tokens
| Class | Size | Context |
|-------|------|---------|
| `.icon-xs`  | 14px | Timestamps, inline decorations |
| `.icon-sm`  | 16px | Inline text, table cells |
| `.icon-md`  | 20px | Nav links, button labels **(default)** |
| `.icon-lg`  | 24px | Section headers, empty-state icons |
| `.icon-xl`  | 32px | Auth card header |
| `.icon-2xl` | 48px | Hero / illustration |

### Rules
1. **Always pair icon + visible text** for nav items and buttons — never icon-only without `aria-label`
2. Use **filled variant** (`bi-check-circle-fill`) for confirmed/active states; **outline** for idle
3. Semantic choices:
   - Error: `bi-exclamation-triangle-fill` (red)
   - Success: `bi-check-circle` (green)
   - Info: `bi-info-circle` (blue)
   - Warning: `bi-exclamation-circle` (orange)
4. Align with text: `vertical-align: middle; margin-top: -2px`
5. Color: inherit from parent unless semantic role requires override

---

## 8. Z-Index Scale

| Token | Value | Layer |
|-------|-------|-------|
| `--z-base`    | 0   | Normal flow |
| `--z-raised`  | 10  | Hovering card, tooltip anchor |
| `--z-dropdown`| 20  | Dropdown menus |
| `--z-sticky`  | 40  | Sticky navbar |
| `--z-modal`   | 100 | Modals, drawers, scrim |
| `--z-toast`   | 200 | Toasts, snackbars |

---

## 9. Animation

| Token | Value | Use |
|-------|-------|-----|
| `--duration-fast`   | 100ms | Micro (opacity, scale) |
| `--duration-base`   | 150ms | Colors, borders, shadows |
| `--duration-slow`   | 250ms | Card hover lift |
| `--duration-slower` | 350ms | Panel slides |
| `--ease-out` | `cubic-bezier(.2,0,0,1)` | Elements entering screen |
| `--ease-in`  | `cubic-bezier(.4,0,1,1)` | Elements leaving |
| `--ease-inout` | `cubic-bezier(.4,0,.2,1)` | State transitions |

**Rules:**
- Only animate `transform` and `opacity` (never `width/height/top/left`)
- Respect `prefers-reduced-motion` — all transitions collapse to `.01ms`
- Exit animations: 60–70% of enter duration

---

## 10. Accessibility Checklist

- [ ] All interactive elements have `:focus-visible` ring (2px `--color-focus-ring`)
- [ ] Color contrast ≥ 4.5:1 for body text; ≥ 3:1 for large UI elements
- [ ] Touch targets ≥ 44px height (`min-height: 44px` on inputs and buttons)
- [ ] Icon-only buttons have `aria-label`
- [ ] Errors announced via `aria-live` or Wicket FeedbackPanel
- [ ] `prefers-reduced-motion` media query in base CSS
- [ ] No information conveyed by color alone (add icon or text)
- [ ] Form labels always visible (never placeholder-only)

---

## 11. Responsive Breakpoints

| Breakpoint | Width | Target |
|------------|-------|--------|
| `sm` | 640px | Small phones |
| `md` | 768px | Tablets portrait |
| `lg` | 1024px | Tablets landscape / small laptop |
| `xl` | 1280px | Desktop |
| `2xl` | 1440px | Wide desktop |

**Rule:** Mobile-first. Containers: `max-width: 1200px` (`.container`), `680px` (`.container-sm`).
