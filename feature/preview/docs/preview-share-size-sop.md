# Preview Share Size SOP

## Objective

Define a consistent preview card size for sharing. The goal is a visually balanced card that preserves content hierarchy and reads well across common share targets.

## Constraints

- Must look good as a standalone image (no surrounding app UI).
- Should accommodate title + image + metadata + short body.
- Must not be too tall to avoid extreme vertical scroll in previews.

## Observed Targets (practical baseline)

Most social previews and chat shares show content in:

- Square / near-square (1:1) thumbnails
- Portrait (4:5 or 3:4) for feeds
- Story (9:16) for full-screen shares

Given our content density (title + cover + tags + body), portrait is the best balance.

## Recommended Baseline (SOP)

**Primary target: width-limited layout with adaptive height.**

We keep 4:5 as an ideal reference, but real content (text length, image aspect, resolution) makes a strict ratio hard to enforce. The pragmatic solution is:

### Size Rules

1. **Fix width, let height adapt to content.**
    - Width: `min(screenWidth * 0.68, 360.dp)`
2. **Enforce a minimum height equal to 4:5.**
    - `minHeight = width * 1.25`
3. **Allow height to grow if content needs it, within the screen cap.**

### Example Sizing

- `width = min(screenWidth * 0.68, 360.dp)`
- `minHeight = width * 1.25`
- `height = max(minHeight, contentHeight)` (then clamp to screen cap if needed)

## Visual Hierarchy Budget

- Image/cover: 35–45% of height (auto-adjust when text grows)
- Title + meta: 20–25%
- Body snippet: 25–35%
- Actions: outside the card (not in share image)

## Implementation Guidance

For the preview dialog:

- Card container uses fixed width + adaptive height with minHeight = 4:5.
- If content exceeds the vertical budget, truncate body text and clamp to 4–6 lines.
- Keep the share button outside the card so the exported image is clean.

## Alternatives

If 4:5 still feels tall:

- Use **3:4** ratio (height = width * 1.33) only when body is short.
- Use **1:1** ratio for minimal preview (title + image only).

## Recommendation

Adopt **4:5** as the default SOP for all preview shares and stick to max width 360 dp.
