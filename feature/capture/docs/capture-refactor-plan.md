# Capture Refactor Plan

- **Target:** feature/capture
- **Goal:** Align Capture with UI Architecture Rulebook, unify UI state model, and refactor ViewModel and UI usage.
- **Scope:** Capture UI state definition, ViewModel state transitions, and UI bindings.
- **Out of scope:** Visual redesign and new features.

## 1. Current Issues
- UI state is a large data class with internal enum states, and logic is spread across helper flags.
- Input and review phases use duplicated state branches (ReadyIdle/ReadyFocused/AiFailed) and overlays that can be simplified.
- ViewModel uses many per-state factory helpers, which makes transitions harder to verify.

## 2. Target State Model
- Replace `CaptureState` enum with a sealed `CaptureUiState` using `tech.zhifu.app.myhub.ui.State`.
- Reduce to at most 7 states:
  - `Input`
  - `Processing`
  - `Review`
  - `Publishing`

## 3. Refactor Steps
1. Define the Capture UI design spec in `feature/capture/docs/ui-desigin-spec.md`.
2. Update `CaptureUiState.kt`:
   - Convert to sealed class.
   - Move per-state data into state-specific data classes.
   - Recreate helper properties (`isInputPhase`, `isReviewPhase`, etc.).
3. Update `CaptureViewModel.kt`:
   - Replace transitions to match new states.
   - Keep behavior parity for existing flows (AI analyze, review, publish, retry).
4. Update UI usage:
   - Update `CaptureScreen.kt`, `InputSection.kt`, `ReviewSection.kt`, `PreviewPanel.kt` to use new states.
   - Replace `CaptureState.*` checks with new state types/helpers.
5. Update docs:
   - Link the new capture design spec from `docs/design/myhub-design.md`.
6. Validate:
   - Compile `:feature:capture:compileKotlinJvm` and `:composeApp:compileKotlinJvm`.

## 4. Risk & Mitigation
- **Risk:** Missing state mapping in UI overlays.
  - Mitigation: Add explicit mapping for analyze/publish overlays with tests in UI preview.
- **Risk:** Transition mismatches in ViewModel.
  - Mitigation: Mirror existing flows and keep retry paths unchanged.

## 5. Acceptance Checklist
- UI spec exists and linked in `myhub-design.md`.
- `CaptureUiState` is sealed and uses <= 7 states.
- ViewModel compiles and UI overlays show the same behavior.
- Capture module compiles.
