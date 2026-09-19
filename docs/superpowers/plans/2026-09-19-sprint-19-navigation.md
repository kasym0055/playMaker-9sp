# Sprint 19 Navigation Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert Playlist Maker to a single-activity, fragment-based application with Navigation Component, a state-preserving `BottomNavigationView`, and an audio-player destination above the selected top-level section.

**Architecture:** `MainActivity` owns one `NavHostFragment` and connects one `BottomNavigationView` to a root graph. The root graph starts at `library_graph`, has `search_graph` and `settings_graph` as sibling top-level graphs, and contains `AudioPlayerFragment` outside those graphs. Existing ViewModels and domain/data flows remain intact; only small UI parameters use `SavedStateHandle`.

**Tech Stack:** Kotlin, Android Views, View Binding, AndroidX Navigation 2.9.8, Material Components, ViewPager2, Koin 3.3.0, LiveData/ViewModel.

**Spec:** `docs/superpowers/specs/2026-09-19-sprint-19-navigation-design.md`

## Global Constraints

- Keep one `MainActivity`, one `NavHostFragment`, and one root navigation graph.
- Use nested `library_graph`, `search_graph`, and `settings_graph`; graph IDs must match bottom-menu item IDs.
- Use `setupWithNavController()` and Navigation Component only; do not perform manual `FragmentManager` transactions.
- Keep `AudioPlayerFragment` outside the three top-level graphs and hide bottom navigation on it.
- Preserve top-level state using Navigation Component multiple back stacks and the existing ViewModels.
- Store only small restoration parameters in `SavedStateHandle`; never store result lists or the complete UI state.
- Do not change repositories, interactors, domain behavior, network/data code, or unrelated UI.
- Keep `windowSoftInputMode="adjustPan"` on `MainActivity`.
- Observe `LiveData` with `viewLifecycleOwner` and clear every Fragment View Binding in `onDestroyView()`.
- Do not commit, push, create a PR, amend, rebase, reset, or otherwise change Git history. Leave all implementation changes uncommitted.

## File Map

- `gradle/libs.versions.toml`: declare AndroidX Navigation 2.10.1 libraries.
- `app/build.gradle.kts`: enable View Binding and add Navigation dependencies.
- `app/src/main/res/navigation/main_nav_graph.xml`: root graph, three nested top-level graphs, audio-player destination, and track argument.
- `app/src/main/res/menu/bottom_navigation_menu.xml`: Library/Search/Settings items whose IDs match graph IDs.
- `app/src/main/res/color/bottom_navigation_item_color.xml`: selected/unselected icon and label colors.
- `app/src/main/res/layout/activity_main.xml`: root `NavHostFragment` plus `BottomNavigationView`.
- `app/src/main/java/com/example/playlist_maker2/ui/MainActivity.kt`: thin NavigationUI host and destination visibility listener.
- `app/src/main/res/layout/fragment_media_library.xml`: former media Activity UI without Back control.
- `app/src/main/java/com/example/playlist_maker2/ui/media/MediaLibraryFragment.kt`: TabLayout/ViewPager host.
- `app/src/main/java/com/example/playlist_maker2/ui/media/MediaPagerAdapter.kt`: child fragments scoped to parent `Fragment`.
- `app/src/main/java/com/example/playlist_maker2/ui/media/view_model/MediaViewModel.kt`: selected page as the sole explicit `SavedStateHandle` tab value.
- `app/src/main/res/layout/fragment_settings.xml`: former settings Activity UI without Back control.
- `app/src/main/java/com/example/playlist_maker2/ui/settings/SettingsFragment.kt`: settings UI bound to the existing ViewModel.
- `app/src/main/res/layout/fragment_search.xml`: former search Activity UI without Back control.
- `app/src/main/java/com/example/playlist_maker2/ui/search/SearchFragment.kt`: existing search UI behavior plus `NavController` player navigation.
- `app/src/main/java/com/example/playlist_maker2/ui/search/view_model/SearchViewModel.kt`: query restoration through `SavedStateHandle` while keeping result state in memory.
- `app/src/main/res/layout/fragment_audio_player.xml`: former audio-player Activity UI.
- `app/src/main/java/com/example/playlist_maker2/ui/player/AudioPlayerFragment.kt`: track argument, player rendering, and Navigation back handling.
- `app/src/main/java/com/example/playlist_maker2/ui/player/view_model/PlayerViewModel.kt`: idempotent preparation guard for Fragment View recreation.
- `app/src/main/java/com/example/playlist_maker2/di/ViewModelModule.kt`: inject `SavedStateHandle` into Search and Media ViewModels.
- `app/src/main/AndroidManifest.xml`: remove four obsolete Activity declarations and set `adjustPan` on `MainActivity`.
- Remove obsolete Activity source/layout files after their Fragment replacements compile.

## Review Focus

- A restored non-empty Search query must repopulate the input and re-run search only when result state is absent, not restart on every tab switch.
- Recreating `AudioPlayerFragment`'s View must not call player preparation a second time while its ViewModel is alive.
- Missing or malformed `Track` arguments must pop the audio-player destination without crashing.
- Selecting a top-level menu item after visiting another section must restore its prior navigation/UI state.
- Destination changes must hide the bottom bar only for Audio Player and restore it for every top-level graph destination.

---

### Task 1: Navigation Shell and Resource Graph

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Replace: `app/src/main/res/layout/activity_main.xml`
- Create: `app/src/main/res/navigation/main_nav_graph.xml`
- Create: `app/src/main/res/menu/bottom_navigation_menu.xml`
- Create: `app/src/main/res/color/bottom_navigation_item_color.xml`
- Modify: `app/src/main/java/com/example/playlist_maker2/ui/MainActivity.kt`

**Interfaces:**
- Consumes: fragment class names defined by Tasks 2–4.
- Produces: `R.id.nav_host_fragment`, `R.id.bottom_navigation`, graph IDs `library_graph`, `search_graph`, `settings_graph`, and `R.id.audioPlayerFragment`.

- [ ] **Step 1: Record the current build baseline with the Android Studio JBR**

Run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest assembleDebug
```

Expected before source changes: PASS. If it fails, record the pre-existing failure before continuing.

- [ ] **Step 2: Add Navigation and View Binding build configuration**

Add `navigation = "2.9.8"`, `androidx-navigation-fragment-ktx`, and
`androidx-navigation-ui-ktx` aliases to the version catalog. Enable:

```kotlin
buildFeatures {
    viewBinding = true
}
```

and add both Navigation aliases to `dependencies`.

- [ ] **Step 3: Define menu, selected colors, and graph structure**

Create a three-item menu in Library/Search/Settings order. Use IDs
`library_graph`, `search_graph`, and `settings_graph`. Create a root graph whose
`app:startDestination` is `@id/library_graph`; give each nested graph its
corresponding Fragment start destination and define `audioPlayerFragment` at the
root with a nullable `serializable` argument named `track`.

- [ ] **Step 4: Replace the launcher layout and Activity logic**

Use a constrained `FragmentContainerView` with
`android:name="androidx.navigation.fragment.NavHostFragment"`,
`app:defaultNavHost="true"`, and `app:navGraph="@navigation/main_nav_graph"`.
Place `BottomNavigationView` below it. In `MainActivity`, obtain the
`NavHostFragment` through `supportFragmentManager` only to configure navigation,
call `bottomNavigation.setupWithNavController(navController)`, and hide the bar
when `destination.id == R.id.audioPlayerFragment`.

- [ ] **Step 5: Compile the navigation shell**

Run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:processDebugResources :app:compileDebugKotlin
```

Expected at this intermediate point: resource processing passes; Kotlin may
report the not-yet-created Fragment class names referenced by the graph. Those
classes are supplied by Tasks 2–4.

### Task 2: Library and Settings Fragments

**Files:**
- Create: `app/src/main/res/layout/fragment_media_library.xml`
- Create: `app/src/main/java/com/example/playlist_maker2/ui/media/MediaLibraryFragment.kt`
- Modify: `app/src/main/java/com/example/playlist_maker2/ui/media/MediaPagerAdapter.kt`
- Modify: `app/src/main/java/com/example/playlist_maker2/ui/media/view_model/MediaViewModel.kt`
- Create: `app/src/main/res/layout/fragment_settings.xml`
- Create: `app/src/main/java/com/example/playlist_maker2/ui/settings/SettingsFragment.kt`
- Modify: `app/src/main/java/com/example/playlist_maker2/di/ViewModelModule.kt`
- Delete after replacement: `app/src/main/java/com/example/playlist_maker2/ui/media/MediaActivity.kt`
- Delete after replacement: `app/src/main/java/com/example/playlist_maker2/ui/settings/SettingsActivity.kt`
- Delete after replacement: `app/src/main/res/layout/activity_media.xml`
- Delete after replacement: `app/src/main/res/layout/activity_settings.xml`

**Interfaces:**
- Consumes: `FragmentMediaLibraryBinding`, `FragmentSettingsBinding`, existing Settings actions and child media fragments.
- Produces: `MediaLibraryFragment`, `SettingsFragment`, and `MediaViewModel.selectedTab: LiveData<Int>` with `selectTab(index: Int)`.

- [ ] **Step 1: Make the media ViewModel accept saved state**

Use `SavedStateHandle` key `selected_media_tab`, defaulting to `0`. Expose it as
`LiveData<Int>` and update it only when the selected page changes. Register the
ViewModel in Koin with the handle supplied through `get()`.

- [ ] **Step 2: Convert Media Activity UI to a Fragment layout**

Copy the existing toolbar title, `TabLayout`, and `ViewPager2` into
`fragment_media_library.xml`, remove the Back `ImageButton`, and constrain the
title directly in the toolbar.

- [ ] **Step 3: Implement MediaLibraryFragment lifecycle and tab synchronization**

Inflate View Binding in `onCreateView`, create `MediaPagerAdapter(this)`, attach
`TabLayoutMediator`, observe `selectedTab` with `viewLifecycleOwner`, and register
a `ViewPager2.OnPageChangeCallback` that calls `selectTab(position)`. Detach the
mediator, unregister the callback, clear the adapter, and null the binding in
`onDestroyView()`.

- [ ] **Step 4: Scope MediaPagerAdapter to the parent Fragment**

Change the constructor from `FragmentActivity` to `Fragment` and call
`FragmentStateAdapter(fragment)` so Favorite Tracks and Playlists remain child
fragments of `MediaLibraryFragment`.

- [ ] **Step 5: Convert Settings Activity UI and logic to a Fragment**

Create `fragment_settings.xml` without the Back control. Move theme observation
and share/support/terms listeners into `SettingsFragment`, observe with
`viewLifecycleOwner`, and clear binding in `onDestroyView()`. Keep
`SettingsViewModel` unchanged.

- [ ] **Step 6: Remove replaced media/settings Activity artifacts and compile**

Delete the two Activity classes and their Activity layouts, then run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugKotlin
```

Expected: no media/settings Activity or Fragment lifecycle compilation errors.

### Task 3: Search Fragment and Query Restoration

**Files:**
- Create: `app/src/main/res/layout/fragment_search.xml`
- Create: `app/src/main/java/com/example/playlist_maker2/ui/search/SearchFragment.kt`
- Modify: `app/src/main/java/com/example/playlist_maker2/ui/search/view_model/SearchViewModel.kt`
- Modify: `app/src/main/java/com/example/playlist_maker2/di/ViewModelModule.kt`
- Delete after replacement: `app/src/main/java/com/example/playlist_maker2/ui/search/SearchActivity.kt`
- Delete after replacement: `app/src/main/res/layout/search_page.xml`

**Interfaces:**
- Consumes: existing `SearchState`, `TrackAdapter`, search/history interactors, and `R.id.audioPlayerFragment`.
- Produces: `SearchFragment` and `SearchViewModel.searchQuery: String` backed by `SavedStateHandle` key `search_query`.

- [ ] **Step 1: Add minimal query saved state to SearchViewModel**

Add `SavedStateHandle` to the constructor. Read `search_query` as the initial
query, update it whenever text changes, and keep lists/`SearchState` only in the
existing `LiveData`. On first observation after process restoration, re-run a
non-empty query only if no in-memory state exists.

- [ ] **Step 2: Convert Search layout without a Back control**

Create `fragment_search.xml` from `search_page.xml`, remove `arr_back`, retain
the title and all existing search/history/error/loading controls, and keep IDs
stable so the behavior maps directly to generated View Binding properties.

- [ ] **Step 3: Move Search UI behavior into SearchFragment**

Inflate and clear binding across `onCreateView`/`onDestroyView`. Set up both
adapters, RecyclerViews, query listeners, history, retry, and rendering with
`viewLifecycleOwner`. Restore the input from `viewModel.searchQuery` without
clearing the current ViewModel state.

For each track click, debounce, add result tracks to history as before, and call:

```kotlin
findNavController().navigate(
    R.id.audioPlayerFragment,
    bundleOf(TRACK_ARGUMENT_KEY to track)
)
```

Cancel the click-debounce reset callback and detach adapters in
`onDestroyView()` so they cannot retain a destroyed View.

- [ ] **Step 4: Remove SearchActivity artifacts and compile**

Delete `SearchActivity.kt` and `search_page.xml`, then run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugKotlin
```

Expected: Search Fragment, SavedStateHandle injection, and navigation call all
compile.

### Task 4: Audio Player Fragment and Back Navigation

**Files:**
- Create: `app/src/main/res/layout/fragment_audio_player.xml`
- Create: `app/src/main/java/com/example/playlist_maker2/ui/player/AudioPlayerFragment.kt`
- Modify: `app/src/main/java/com/example/playlist_maker2/ui/player/view_model/PlayerViewModel.kt`
- Delete after replacement: `app/src/main/java/com/example/playlist_maker2/ui/player/AudioPlayerActivity.kt`
- Delete after replacement: `app/src/main/res/layout/activity_audioplayer.xml`

**Interfaces:**
- Consumes: nullable serialized Navigation argument `track`, existing `PlayerViewModel`, and root `NavController`.
- Produces: `AudioPlayerFragment`; `PlayerViewModel.prepareUrl(url)` remains public but becomes idempotent while already initialized.

- [ ] **Step 1: Guard player preparation across View recreation**

Make `prepareUrl(url)` return without preparing again unless the current player
state is `PlayerState.Default`. Do not otherwise change playback behavior.

- [ ] **Step 2: Convert Audio Player layout to Fragment View Binding**

Create `fragment_audio_player.xml` from the current layout and retain the Back,
artwork, metadata, playback, playlist, and favorite controls with existing IDs.

- [ ] **Step 3: Move player UI logic into AudioPlayerFragment**

Read `track` using the API-compatible serializable Bundle call, render track
metadata, observe player state with `viewLifecycleOwner`, and prepare the preview
only through the guarded ViewModel method. Use
`findNavController().navigateUp()` for the toolbar Back button.

When `track` is absent or has unusable required data, show the existing short
error feedback and call `findNavController().popBackStack()` without touching a
top-level graph.

- [ ] **Step 4: Remove AudioPlayerActivity artifacts and compile**

Delete the Activity class and layout, then run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugKotlin
```

Expected: player Fragment and both Navigation back paths compile.

### Task 5: Manifest Cleanup and Full Verification

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify only if required by direct migration breakage: existing test files.

**Interfaces:**
- Consumes: completed fragment/navigation implementation.
- Produces: single-Activity application package satisfying Sprint 19.

- [ ] **Step 1: Reduce the manifest to MainActivity**

Remove the four obsolete Activity declarations, remove the malformed unused
`tools` namespace, and set `android:windowSoftInputMode="adjustPan"` on
`MainActivity`.

- [ ] **Step 2: Run static navigation and lifecycle checks**

Run:

```powershell
rg -n "SearchActivity|MediaActivity|SettingsActivity|AudioPlayerActivity" app/src/main
rg -n "commit\(|beginTransaction|replace\(|add\(" app/src/main/java/com/example/playlist_maker2/ui
rg -n "observe\(this\)" app/src/main/java/com/example/playlist_maker2/ui
rg -n "setupWithNavController|library_graph|search_graph|settings_graph|audioPlayerFragment|adjustPan" app/src/main
```

Expected: the first three searches return no obsolete declarations, manual
navigation transactions, or Activity-scoped Fragment observers; the final
search finds the required wiring.

- [ ] **Step 3: Run automated verification**

Run:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

Expected: BUILD SUCCESSFUL. Fix only migration-related failures within approved
scope and rerun the failing command followed by the full command.

- [ ] **Step 4: Inspect the built package inputs and working-tree diff**

Run:

```powershell
git diff --check
git status --short
git diff --stat
```

Expected: no whitespace errors; all implementation and plan changes remain
uncommitted.

- [ ] **Step 5: Perform or hand off device-only checks**

On an emulator/device, verify all 13 manual scenarios from the specification,
including keyboard coverage under `adjustPan`, top-level state retention,
player Back parity, and both themes. If no emulator/device is available, report
these items explicitly as remaining manual checks rather than claiming them.
