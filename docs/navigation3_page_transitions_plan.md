# Navigation3 Page Transitions Plan (Slide In/Out)

## Status
Draft – pending confirmation

## Goal
Introduce a standard, framework-level page transition for Navigation3 screens:
- Forward navigation: new page enters from right to left.
- Back navigation: current page exits from left to right.
- Use Jetpack Compose standard animations via `NavDisplay`.

## Current Architecture (as-is)
- Root host uses `NavDisplay` in [App.kt](/Users/zzf/Work/zhifu-tech-apps/app-myhub/composeApp/src/commonMain/kotlin/tech/zhifu/app/myhub/App.kt).
- Adaptive layout uses a custom `SceneStrategy` (`rememberListDetailSceneStrategy`).
- Navigation is driven by `NavDisplay(entries = …, sceneStrategy = …, onBack = …)`.

## Official Navigation3 Capabilities (source of truth)
- `NavDisplay` supports `transitionSpec`, `popTransitionSpec`, and `predictivePopTransitionSpec`.
- These apply globally to all destinations unless overridden at the entry level.
- Official recipe demonstrates `slideInHorizontally`/`slideOutHorizontally` for forward/back transitions.

References:
- Android Developers – Navigation3 Animations Recipe (NavDisplay `transitionSpec`, `popTransitionSpec`, `predictivePopTransitionSpec`).

## Desired Transition Behavior
- **Forward (push):** new screen slides in from right → left.
- **Backward (pop):** current screen slides out to the right; previous screen slides in from left → right (or stays depending on spec).
- **Predictive Back:** should match the pop animation direction to avoid visual mismatch.

## Proposed Global Transition Spec (NavDisplay-level)
Use Compose standard animation primitives:
- `slideInHorizontally { fullWidth -> fullWidth }` for enter
- `slideOutHorizontally { fullWidth -> -fullWidth }` for exit
- `popTransitionSpec` should reverse direction:
  - `slideInHorizontally { fullWidth -> -fullWidth }`
  - `slideOutHorizontally { fullWidth -> fullWidth }`
- `predictivePopTransitionSpec` should mirror `popTransitionSpec`.

This aligns with official Navigation3 guidance that `transitionSpec` is for forward navigation and `popTransitionSpec` is for backward navigation.

## Where This Change Lives
Framework-level change at the **root `NavDisplay` in `App.kt`**:
- Centralized transition config applies to all features, including `feature/mixed` and `feature/dashboard`.
- Keeps per-feature UI clean and consistent.
- If needed, an individual entry can override with a custom `transitionSpec` in the entry DSL.

## Open Questions / Decisions
1. **Predictive Back**
   - Use the same spec as pop to keep parity.
2. **SceneStrategy Compatibility**
   - No changes expected: `SceneStrategy` determines displayed scenes; `NavDisplay` handles animations.
3. **Platform Differences (KMP)**
   - `NavDisplay` transition API is in Navigation3 UI and should be consistent across platforms.
   - If any platform lacks predictive back support, the pop spec still applies.

## Implementation Steps (after approval)
1. Update `NavDisplay` call in `App.kt` to pass:
   - `transitionSpec`
   - `popTransitionSpec`
   - `predictivePopTransitionSpec`
2. Use standard Compose animation APIs from `androidx.compose.animation`.
3. Verify behavior on forward + back navigation.

## Risks
- If any screen uses custom entry-level transitions, global defaults might conflict.
- Predictive back might feel off on non-Android platforms (no gesture), but safe as a fallback.

## Acceptance Criteria
- Forward navigation: enter from right, exit to left.
- Back navigation: exit to right, enter from left.
- Applies across all features without per-screen changes.
