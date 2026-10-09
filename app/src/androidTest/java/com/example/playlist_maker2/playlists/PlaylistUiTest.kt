package com.example.playlist_maker2.playlists

import android.app.Activity
import android.app.Instrumentation
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.viewpager2.widget.ViewPager2
import com.example.playlist_maker2.R
import com.example.playlist_maker2.data.FavoriteTracksRepositoryImpl
import com.example.playlist_maker2.data.PlaylistsRepositoryImpl
import com.example.playlist_maker2.data.db.AppDatabase
import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.domain.favorite.FavoriteTracksRepository
import com.example.playlist_maker2.domain.favorite.impl.FavoriteTracksInteractorImpl
import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.domain.playlists.PlaylistsRepository
import com.example.playlist_maker2.domain.playlists.impl.PlaylistsInteractorImpl
import com.example.playlist_maker2.ui.MainActivity
import com.example.playlist_maker2.ui.search.TRACK_ARGUMENT_KEY
import com.google.gson.Gson
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class PlaylistUiTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: PlaylistsRepositoryImpl
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val testModule = module {
        single<AppDatabase> { database }
        single<PlaylistsRepository> { repository }
        single<PlaylistsInteractor> { PlaylistsInteractorImpl(get()) }
        single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get()) }
        single<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }
    }

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = PlaylistsRepositoryImpl(database, context, Gson())
        loadKoinModules(testModule)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        androidx.test.espresso.Espresso.setFailureHandler { error, matcher ->
            runCatching { screenshot("playlist-failure-${System.nanoTime()}") }
            DefaultFailureHandler(context).handle(error, matcher)
        }
    }

    @After fun tearDown() {
        androidx.test.espresso.Espresso.setFailureHandler(DefaultFailureHandler(context))
        scenario.close()
        runBlocking { repository.observePlaylists().first() }.forEach { playlist ->
            playlist.coverPath?.let { File(it).delete() }
        }
        unloadKoinModules(testModule)
        database.close()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    @Test fun emptyFormClosesAndDescriptionOnlyRequiresDiscardConfirmation() {
        openLibraryPlaylists()
        waitFor(R.id.playlistsPlaceholderText, isDisplayed())
        screenshot("playlists-empty")
        openCreation()
        onView(withId(R.id.bottom_navigation)).check(matches(not(isDisplayed())))
        onView(withId(R.id.createPlaylistButton)).check(matches(not(isEnabled())))
        onView(withId(R.id.playlistCoverPlaceholder)).check(matches(isDisplayed()))
        screenshot("playlist-create-empty")
        toolbarBack()
        waitFor(R.id.newPlaylistButton, isDisplayed())
        openCreation()
        enterText(R.id.playlistDescription, "Несохранённое описание")
        onView(withId(R.id.createPlaylistButton)).check(matches(not(isEnabled())))
        pressBack()
        onView(withText(R.string.discard_playlist_title)).check(matches(isDisplayed()))
        onView(withText(R.string.discard_playlist_message)).check(matches(isDisplayed()))
        screenshot("playlist-discard-dialog")
        onView(withId(android.R.id.button2)).perform(click())
        onView(withId(R.id.playlistDescription)).check(matches(withText("Несохранённое описание")))
        toolbarBack()
        onView(withId(android.R.id.button1)).perform(click())
        waitFor(R.id.playlistsPlaceholderText, isDisplayed())
        assertTrue(storedPlaylists().isEmpty())
    }

    @Test fun nameOnlyPlaylistIsStoredAndAppearsInLibrary() {
        openLibraryPlaylists()
        openCreation()
        enterText(R.id.playlistName, "Только название")
        onView(withId(R.id.createPlaylistButton)).check(matches(isEnabled())).perform(click())
        waitFor(R.id.playlistsList, isDisplayed())
        waitFor(R.id.playlistName, withText("Только название"))
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()))
        onView(withId(R.id.playlistsPlaceholderText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.playlistTrackCount)).check(matches(withText(trackCountText(0))))
        val saved = storedPlaylists().single()
        assertEquals("Только название", saved.name)
        assertEquals("", saved.description)
        assertNull(saved.coverPath)
        assertTrue(saved.trackIds.isEmpty())
        assertEquals(0, saved.trackCount)
        scenario.onActivity { activity ->
            val cover = activity.findViewById<ImageView>(R.id.playlistCover)
            assertTrue(cover.drawable.toBitmap(64, 64).sameAs(
                activity.getDrawable(R.drawable.audio_player_placeholder)!!.toBitmap(64, 64)
            ))
        }
        screenshot("playlists-name-only")
        scenario.recreate()
        waitFor(R.id.playlistName, withText("Только название"))
    }

    @Test fun draftSurvivesBackgroundRotationAndActivityRecreation() {
        openLibraryPlaylists()
        openCreation()
        enterText(R.id.playlistName, "Черновик")
        enterText(R.id.playlistDescription, "Описание черновика")
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.playlistName)).check(matches(withText("Черновик")))
        onView(withId(R.id.playlistDescription)).check(matches(withText("Описание черновика")))
        scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        waitFor(R.id.createPlaylistToolbar, hasOrientation(Configuration.ORIENTATION_LANDSCAPE))
        onView(withId(R.id.playlistName)).check(matches(withText("Черновик")))
        onView(withId(R.id.playlistDescription)).check(matches(withText("Описание черновика")))
        onView(withId(R.id.createPlaylistButton)).check(matches(isEnabled()))
        screenshot("playlist-draft-landscape")
        scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        waitFor(R.id.createPlaylistToolbar, hasOrientation(Configuration.ORIENTATION_PORTRAIT))
        scenario.recreate()
        onView(withId(R.id.playlistName)).check(matches(withText("Черновик")))
        onView(withId(R.id.playlistDescription)).check(matches(withText("Описание черновика")))
        screenshot("playlist-draft-restored")
        onView(withId(R.id.createPlaylistButton)).perform(click())
        waitFor(R.id.playlistName, withText("Черновик"))
        assertEquals("Описание черновика", storedPlaylists().single().description)
    }

    @Test fun photoPickerDisplaysCoverAndSavedCopySurvivesOriginalDeletion() {
        val source = createMediaStoreImage()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var monitor: Instrumentation.ActivityMonitor? = null
        var sourceDeleted = false
        try {
            openLibraryPlaylists()
            openCreation()
            val pickerIntent = ActivityResultContracts.PickVisualMedia().createIntent(
                context, PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
            monitor = instrumentation.addMonitor(
                IntentFilter(requireNotNull(pickerIntent.action)).apply {
                    pickerIntent.type?.let { addDataType(it) }
                    pickerIntent.categories?.forEach { addCategory(it) }
                },
                Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().setData(source)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)),
                true
            )
            onView(withId(R.id.playlistCoverContainer)).perform(scrollTo(), click())
            waitFor(R.id.playlistCover, hasCenterColor(Color.MAGENTA))
            assertEquals(1, requireNotNull(monitor).hits)
            onView(withId(R.id.playlistCoverPlaceholder)).check(matches(not(isDisplayed())))
            scenario.recreate()
            waitFor(R.id.playlistCover, hasCenterColor(Color.MAGENTA))
            enterText(R.id.playlistName, "С обложкой")
            screenshot("playlist-selected-cover")
            onView(withId(R.id.createPlaylistButton)).perform(click())
            waitFor(R.id.playlistsList, isDisplayed())
            val saved = storedPlaylists().single()
            val privateCover = File(requireNotNull(saved.coverPath))
            assertTrue(privateCover.canonicalPath.startsWith(context.filesDir.canonicalPath + File.separator))
            assertTrue(privateCover.isFile)
            assertTrue(privateCover.length() > 0)
            assertEquals(1, context.contentResolver.delete(source, null, null))
            sourceDeleted = true
            scenario.recreate()
            waitFor(R.id.playlistCover, hasCenterColor(Color.MAGENTA))
            assertTrue(privateCover.isFile)
            screenshot("playlist-cover-after-original-deleted")
        } finally {
            monitor?.let(instrumentation::removeMonitor)
            if (!sourceDeleted) runCatching { context.contentResolver.delete(source, null, null) }
        }
    }

    @Test fun playerReturnsFromCreationAddsOnceAndKeepsDuplicateSheetOpen() {
        openPlayer(track(505))
        waitFor(R.id.musicTitle, withText("Track 505"))
        openPlayerPlaylists()
        onView(withId(R.id.newPlaylistButton)).perform(click())
        waitFor(R.id.createPlaylistToolbar, isDisplayed())
        toolbarBack()
        waitFor(R.id.musicTitle, withText("Track 505"))
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_HIDDEN))
        openPlayerPlaylists()
        onView(withId(R.id.newPlaylistButton)).perform(click())
        waitFor(R.id.createPlaylistToolbar, isDisplayed())
        enterText(R.id.playlistName, "Из плеера")
        onView(withId(R.id.createPlaylistButton)).perform(click())
        waitFor(R.id.musicTitle, withText("Track 505"))
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_HIDDEN))
        openPlayerPlaylists()
        waitFor(R.id.playlistName, withText("Из плеера"))
        onView(withId(R.id.playlistTrackCount)).check(matches(withText(trackCountText(0))))
        screenshot("player-new-playlist")
        onView(withText("Из плеера")).perform(click())
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_HIDDEN))
        val saved = storedPlaylists().single()
        assertEquals(listOf(505L), saved.trackIds)
        assertEquals(1, saved.trackCount)
        assertEquals(505L, runBlocking { database.playlistTrackDao().getTracks(listOf(505L)).single().trackId })
        openPlayerPlaylists()
        waitFor(R.id.playlistTrackCount, withText(trackCountText(1)))
        onView(withText("Из плеера")).perform(click())
        waitFor(R.id.newPlaylistButton, isEnabled())
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_EXPANDED))
        onView(withId(R.id.playlistsBottomSheet)).check(matches(isDisplayed()))
        assertEquals(listOf(505L), storedPlaylists().single().trackIds)
        assertEquals(1, storedPlaylists().single().trackCount)
        screenshot("player-duplicate-playlist")
        onView(withId(R.id.playlistsBottomSheet)).perform(swipeDown())
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_HIDDEN))
        pressBack()
        openLibraryPlaylists()
        waitFor(R.id.playlistTrackCount, withText(trackCountText(1)))
    }

    @Test fun gridUsesTwoColumnsAndLongNamesRemainReadableInBothThemes() {
        runBlocking {
            repeat(4) { index ->
                repository.createPlaylist("Очень длинное название плейлиста номер $index для проверки переноса", "", null)
            }
        }
        openLibraryPlaylists()
        waitFor(R.id.playlistsList, hasGridItems(4))
        assertGridGeometry()
        screenshot("playlists-grid-long-names-light")
        scenario.onActivity { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
        waitFor(R.id.playlistsList, allOf(hasGridItems(4), hasNightMode()))
        assertGridGeometry()
        screenshot("playlists-grid-long-names-dark")
        openCreation()
        onView(withId(R.id.createPlaylistButton)).check(matches(not(isEnabled())))
        enterText(R.id.playlistName, "Тёмная тема")
        onView(withId(R.id.createPlaylistButton)).check(matches(isEnabled()))
        screenshot("playlist-create-dark")
        toolbarBack()
        onView(withText(R.string.discard_playlist_title)).check(matches(isDisplayed()))
        screenshot("playlist-discard-dark")
        onView(withId(android.R.id.button1)).perform(click())
        waitFor(R.id.playlistsList, hasGridItems(4))
    }

    private fun openLibraryPlaylists() {
        waitFor(R.id.mediaTabLayout, isDisplayed())
        waitFor(R.id.mediaViewPager, hasIdlePager())
        onView(allOf(withText(R.string.playlists), isDescendantOfA(withId(R.id.mediaTabLayout)))).perform(click())
        waitFor(R.id.mediaViewPager, hasIdlePager(1))
        waitFor(R.id.newPlaylistButton, isCompletelyDisplayed())
    }

    private fun openPlayerPlaylists() {
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_HIDDEN))
        onView(withId(R.id.playlistButton)).perform(scrollTo(), click())
        waitFor(R.id.playlistsBottomSheet, hasSheetState(BottomSheetBehavior.STATE_EXPANDED))
        waitFor(R.id.newPlaylistButton, isCompletelyDisplayed())
    }

    private fun openCreation() {
        onView(withId(R.id.newPlaylistButton)).perform(click())
        waitFor(R.id.createPlaylistToolbar, isDisplayed())
    }

    private fun toolbarBack() {
        onView(allOf(withContentDescription(R.string.back),
            isDescendantOfA(withId(R.id.createPlaylistToolbar)))).perform(click())
    }

    private fun enterText(id: Int, text: String) {
        onView(withId(id)).perform(scrollTo(), replaceText(text), closeSoftKeyboard())
    }

    private fun openPlayer(track: Track) {
        scenario.onActivity { activity ->
            val host = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            host.navController.navigate(R.id.audioPlayerFragment, bundleOf(TRACK_ARGUMENT_KEY to track))
        }
    }

    private fun storedPlaylists(): List<Playlist> = runBlocking { repository.observePlaylists().first() }

    private fun trackCountText(count: Int) = context.resources.getQuantityString(R.plurals.playlist_track_count, count, count)

    private fun createMediaStoreImage(): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "playlist-ui-${System.nanoTime()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PlaylistMakerTests")
        }
        val uri = requireNotNull(context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.MAGENTA) }
        context.contentResolver.openOutputStream(uri)!!.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return uri
    }

    private fun assertGridGeometry() {
        scenario.onActivity { activity ->
            val list = activity.findViewById<RecyclerView>(R.id.playlistsList)
            assertEquals(2, (list.layoutManager as GridLayoutManager).spanCount)
            val first = requireNotNull(list.findViewHolderForAdapterPosition(0)).itemView
            val second = requireNotNull(list.findViewHolderForAdapterPosition(1)).itemView
            val third = requireNotNull(list.findViewHolderForAdapterPosition(2)).itemView
            val side = (16 * list.resources.displayMetrics.density).roundToInt()
            val gap = (8 * list.resources.displayMetrics.density).roundToInt()
            assertEquals(side, first.left)
            assertEquals(side, list.width - second.right)
            assertTrue(abs(second.left - first.right - gap) <= 1)
            assertTrue(abs(third.top - first.bottom - gap) <= 1)
            listOf(first, second, third).forEach { item ->
                val cover = item.findViewById<ImageView>(R.id.playlistCover)
                val name = item.findViewById<TextView>(R.id.playlistName)
                val count = item.findViewById<TextView>(R.id.playlistTrackCount)
                assertEquals(cover.width, cover.height)
                assertEquals(1, name.maxLines)
                assertEquals(TextUtils.TruncateAt.END, name.ellipsize)
                assertTrue(name.width > 0)
                assertTrue(count.top >= name.bottom)
                assertTrue(count.bottom <= item.height)
            }
        }
    }

    private fun hasGridItems(count: Int) = viewMatcher("grid with $count laid-out items") { view ->
        view is RecyclerView && view.adapter?.itemCount == count &&
            view.findViewHolderForAdapterPosition(2)?.itemView?.height?.let { it > 0 } == true
    }

    private fun hasCenterColor(color: Int) = viewMatcher("image with center color $color") { view ->
        (view as? ImageView)?.drawable?.toBitmap(64, 64)?.getPixel(32, 32) == color
    }

    private fun hasOrientation(orientation: Int) = viewMatcher("orientation $orientation") {
        it.resources.configuration.orientation == orientation
    }

    private fun hasNightMode() = viewMatcher("night mode") {
        it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun hasSheetState(state: Int) = viewMatcher("bottom sheet state $state") {
        runCatching { BottomSheetBehavior.from(it).state == state }.getOrDefault(false)
    }

    private fun hasIdlePager(page: Int? = null) = viewMatcher("idle pager on page $page") {
        it is ViewPager2 && it.width > 0 && it.adapter?.itemCount == 2 &&
            it.scrollState == ViewPager2.SCROLL_STATE_IDLE && (page == null || it.currentItem == page)
    }

    private fun viewMatcher(description: String, predicate: (View) -> Boolean) = object : TypeSafeMatcher<View>() {
        override fun describeTo(descriptionText: Description) { descriptionText.appendText(description) }
        override fun matchesSafely(item: View): Boolean = predicate(item)
    }

    private fun waitFor(id: Int, matcher: Matcher<View>) {
        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "Wait for view $id to match $matcher"
            override fun perform(uiController: UiController, view: View) {
                val deadline = System.currentTimeMillis() + 10000
                var currentRoot = view
                do {
                    if (!currentRoot.isAttachedToWindow) {
                        currentRoot = ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED).firstOrNull()?.window?.decorView ?: currentRoot
                    }
                    if (descendants(currentRoot).any { it.id == id && matcher.matches(it) }) return
                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < deadline)
                runCatching { screenshot("playlist-wait-failure-$id") }
                val tabs = currentRoot.findViewById<TabLayout>(R.id.mediaTabLayout)
                val pager = currentRoot.findViewById<ViewPager2>(R.id.mediaViewPager)
                val targets = descendants(currentRoot).filter { it.id == id }.joinToString { target ->
                    "${target.javaClass.simpleName}(visible=${isDisplayed().matches(target)}, attached=${target.isAttachedToWindow}, size=${target.width}x${target.height})"
                }
                throw AssertionError("$description; selectedTab=${tabs?.selectedTabPosition}; currentPage=${pager?.currentItem}; targets=[$targets]")
            }
        })
    }

    private fun descendants(root: View): List<View> = buildList {
        add(root)
        if (root is ViewGroup) {
            repeat(root.childCount) { index -> addAll(descendants(root.getChildAt(index))) }
        }
    }

    private fun screenshot(name: String) {
        val directory = File(context.getExternalFilesDir(null), "test-screenshots").apply { mkdirs() }
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun track(id: Long) = Track(id, "Track $id", "Album", "Artist", 185000,
        "2020-01-01", "Rock", "USA", "", null)
}
