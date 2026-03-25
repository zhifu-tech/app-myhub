# Preview Download Plan

## Goal
- Add `download` action for preview image.
- Keep existing share behavior.
- Support Android + iOS first.
- Other platforms return no-op.

## UI Placement
- Narrow layout (actions below content): show `Download`, `Share`.
- Wide layout (actions on right side): show `Download`, `Share`.
- Order: `Download` first, then `Share`.

## Image Source
- Reuse `rememberPreviewShareImage(payload, width)` output image path.
- The image is already generated as PNG.

## Platform Implementation

### Android
- Save PNG to system gallery via `MediaStore.Images.Media`.
- Permission policy:
  - Android <= 28: request `WRITE_EXTERNAL_STORAGE` at runtime.
  - Android >= 29: save through MediaStore without legacy write permission.
- If permission denied: show message and stop.
- On successful save: open system gallery.

### iOS
- Save PNG to Photos via `PHPhotoLibrary.performChanges`.
- Permission policy:
  - If not determined: request authorization.
  - If denied/restricted: show failure result.
- Save the generated file image to Photos.

### Other Platforms (web/jvm/wasm)
- No-op implementation, return `Unsupported`.

## API Design
- Introduce downloader abstraction in preview module:
  - `rememberPreviewDownloader()`
  - `PreviewDownloader.download(imagePath: String)`
  - `PreviewDownloadResult`
- Keep image generation API as suspend function to avoid blocking UI.

## Execution Scope (this round)
1. Android download implementation.
2. iOS download implementation.
3. UI integration for two placements.

## Out of Scope
- Save-as location picker enhancement.
- Advanced download history or custom naming UI.
