# Preview Feature Technical Plan

## Goal

Provide a reusable “content preview” overlay used by multiple screens (Dashboard content list, future AI Chat capture, etc.). The preview is a floating layer (modal) with content on top and a bottom action area (Share now, more actions later).

## Non-Goals (for this phase)

- No code changes to existing screens.
- No actual share integration or extra actions.
- No deep content rendering (markdown, media players, etc.) yet.

## Constraints

- Reusable across feature modules.
- Must not block existing navigation patterns.
- Should support mobile and desktop with Compose Multiplatform.

---

## Architecture

### Modules

- **feature/preview-api**
    - Public API: navigation entry points, state model, parameters.
    - Exposed types only; no UI or heavy dependencies.
- **feature/preview**
    - UI implementation of the overlay.
    - Host composable that renders preview content + actions.

### Entry Points

- `PreviewHost(...)` in `feature/preview`
    - Renders the dialog overlay when `state.visible == true`.
- `PreviewController` (API layer)
    - `show(PreviewPayload)`
    - `hide()`
    - Backed by `MutableStateFlow` (or navigation overlay entry).

### Suggested API (preview-api)

```kotlin
data class PreviewPayload(
    val contentId: String,
    val title: String?,
    val summary: String?,
    val coverUrl: String?,
    val isVideo: Boolean,
)

interface PreviewController {
    val state: StateFlow<PreviewState>
    fun show(payload: PreviewPayload)
    fun hide()
}

data class PreviewState(
    val visible: Boolean,
    val payload: PreviewPayload? = null
)
```

---

## UI Composition (feature/preview)

### Overlay Choice

**Decision: use `Dialog` with full-width surface.**

**Reasons**

- Reusable across screens without wiring a new navigation route.
- Works on all KMP targets; no dependency on system bottom sheets.
- Fits “floating overlay” requirement and supports custom layout.

**Why not ModalBottomSheet**

- Bottom sheet suggests partial-height content and swipe-to-dismiss.
- Preview needs a full content focus area + action bar, closer to a dialog.

### Layout Structure

```
Dialog (scrim)
  Surface (rounded, max width, full height on phone)
    Column
      Content area (image/video/title/summary/body)
      Divider
      Action bar (primary: Share)
```

### Interaction

- Tap scrim or close icon: dismiss preview.
- Back button: dismiss preview.
- Action bar: primary Share; secondary actions later.

---

## Data Flow

1. User taps content item.
2. Caller builds `PreviewPayload` from `ContentItem` (or card metadata later).
3. `PreviewController.show(payload)`
4. `PreviewHost` renders overlay based on state.

---

## Integration Plan

### Dashboard

- On click: call `previewController.show(...)`.
- `Content` screen includes `PreviewHost` at top-level.

### AI Chat Capture (future)

- Reuse the same controller and host.
- Payload can reuse the same fields or extend with `PreviewPayload.extras`.

---

## Extensibility

- Add `PreviewAction` list for multi-action bottom bar.
- Add optional `contentType` to support richer preview renderers.
- Support external share by delegating to platform (core/platform).

---

## Open Questions

- Should preview be full-screen on tablets or centered with max width?
- Confirm action bar layout (single primary vs primary + secondary).
- Whether to keep preview state in shared ViewModel or a dedicated controller.
