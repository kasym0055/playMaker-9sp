package com.example.playlist_maker2.favorite

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toBitmap
import com.example.playlist_maker2.ui.search.SearchViewHolder
import com.bumptech.glide.Glide
import org.junit.Assert.*
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.playlist_maker2.R
import com.example.playlist_maker2.data.FavoriteTracksRepositoryImpl
import com.example.playlist_maker2.data.db.AppDatabase
import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.domain.favorite.FavoriteTracksRepository
import com.example.playlist_maker2.domain.favorite.impl.FavoriteTracksInteractorImpl
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.MainActivity
import com.example.playlist_maker2.ui.search.TRACK_ARGUMENT_KEY
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import java.io.File

@RunWith(AndroidJUnit4::class)
class FavoritesUiTest {
    private lateinit var database: AppDatabase
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val testModule = module {
        single<AppDatabase> { database }
        single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get()) }
        single<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }
    }

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        loadKoinModules(testModule)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun tearDown() {
        scenario.close()
        unloadKoinModules(testModule)
        database.close()
    }

    @Test fun emptyAddOpenRemoveAndRecreate() {
        waitFor(R.id.favoriteTracksPlaceholderText, isDisplayed())
        screenshot("favorites-empty")
        openPlayer(track(101))
        waitFor(R.id.favouriteButton, isEnabled())
        onView(withId(R.id.favouriteButton)).check(matches(not(isSelected())))
        onView(withId(R.id.favouriteButton)).perform(scrollTo(), click())
        waitFor(R.id.favouriteButton, isSelected())
        scenario.recreate()
        waitFor(R.id.favouriteButton, isEnabled())
        onView(withId(R.id.favouriteButton)).check(matches(isSelected()))
        screenshot("player-favorite")
        pressBack()
        waitFor(R.id.favoriteTracksList, isDisplayed())
        waitFor(R.id.name_music, withText("Track 101"))
        onView(withId(R.id.author_music)).check(matches(withText("Artist")))
        onView(withId(R.id.time_music)).check(matches(withText("03:05")))
        screenshot("favorites-list")
        onView(withText("Track 101")).perform(click())
        waitFor(R.id.favouriteButton, isEnabled())
        onView(withId(R.id.musicTitle)).check(matches(withText("Track 101")))
        onView(withId(R.id.favouriteButton)).check(matches(isSelected())).perform(scrollTo(), click())
        waitFor(R.id.favouriteButton, not(isSelected()))
        screenshot("player-not-favorite")
        pressBack()
        waitFor(R.id.favoriteTracksPlaceholderText, isDisplayed())
        onView(withId(R.id.favoriteTracksList)).check(matches(not(isDisplayed())))
    }

    @Test fun longTitlesKeepDurationVisibleAndListUpdatesWhilePlayerIsOpen() {
        val repository = FavoriteTracksRepositoryImpl(database)
        val longTrack = Track(202, "A very long track title that should be truncated after one line", null,
            "A very long artist name that should leave room for duration", 65000, null, "Rock", "USA", "", null)
        runBlocking { repository.addTrack(longTrack) }
        waitFor(R.id.favoriteTracksList, isDisplayed())
        waitFor(R.id.time_music, withText("01:05"))
        onView(withId(R.id.time_music)).check(matches(isCompletelyDisplayed()))
        screenshot("favorites-long-title")
        onView(withId(R.id.name_music)).perform(click())
        waitFor(R.id.favouriteButton, isEnabled())
        runBlocking { repository.removeTrack(longTrack) }
        waitFor(R.id.favouriteButton, not(isSelected()))
        pressBack()
        waitFor(R.id.favoriteTracksPlaceholderText, isDisplayed())
    }

    @Test fun artworkShowsPlaceholderWhenMissingOrLoadingThenDisplaysLoadedImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "test-cover-${System.nanoTime()}.png")
        val cover = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.MAGENTA) }
        file.outputStream().use { cover.compress(Bitmap.CompressFormat.PNG, 100, it) }
        cover.recycle()
        lateinit var picture: ImageView
        lateinit var holder: SearchViewHolder
        lateinit var placeholder: Bitmap
        try {
            scenario.onActivity { activity ->
                val row = LayoutInflater.from(activity).inflate(R.layout.item_track, FrameLayout(activity), false)
                holder = SearchViewHolder(row)
                picture = row.findViewById(R.id.music_picture)
                holder.bind(track(1))
                placeholder = picture.drawable.toBitmap(45, 45)
                assertTrue(placeholder.sameAs(activity.getDrawable(R.drawable.placeholder)!!.toBitmap(45, 45)))
                holder.bind(Track(2, "Cover", null, "Artist", 1000, null, "", "", file.toURI().toString(), null))
                assertTrue(placeholder.sameAs(picture.drawable.toBitmap(45, 45)))
            }
            onView(isRoot()).perform(object : ViewAction {
                override fun getConstraints(): Matcher<View> = isRoot()
                override fun getDescription() = "Wait for local cover to finish loading"
                override fun perform(uiController: UiController, view: View) {
                    val deadline = System.currentTimeMillis() + 5000
                    while (System.currentTimeMillis() < deadline) {
                        val bitmap = picture.drawable.toBitmap(45, 45)
                        if (bitmap.getPixel(22, 22) == Color.MAGENTA) return
                        uiController.loopMainThreadForAtLeast(50)
                    }
                    throw AssertionError(description)
                }
            })
            scenario.onActivity {
                holder.bind(track(3))
                assertTrue(placeholder.sameAs(picture.drawable.toBitmap(45, 45)))
                Glide.with(picture).clear(picture)
            }
        } finally { file.delete() }
    }

    @Test fun favoriteSurvivesSwitchingBetweenLightAndDarkThemes() {
        openPlayer(track(303))
        waitFor(R.id.favouriteButton, isEnabled())
        onView(withId(R.id.favouriteButton)).perform(scrollTo(), click())
        waitFor(R.id.favouriteButton, isSelected())
        screenshot("player-favorite-light")
        scenario.onActivity { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
        waitFor(R.id.favouriteButton, isEnabled())
        onView(withId(R.id.favouriteButton)).check(matches(isSelected()))
        screenshot("player-favorite-dark")
        pressBack()
        waitFor(R.id.favoriteTracksList, isDisplayed())
        screenshot("favorites-list-dark")
    }

    private fun openPlayer(track: Track) {
        scenario.onActivity { activity ->
            val host = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            host.navController.navigate(R.id.audioPlayerFragment, bundleOf(TRACK_ARGUMENT_KEY to track))
        }
    }

    private fun waitFor(id: Int, matcher: Matcher<View>) {
        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "Wait for view $id to match $matcher"
            override fun perform(uiController: UiController, view: View) {
                val deadline = System.currentTimeMillis() + 10000
                do {
                    val target = view.findViewById<View>(id)
                    if (target != null && matcher.matches(target)) return
                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < deadline)
                throw AssertionError(description)
            }
        })
    }

    private fun screenshot(name: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.getExternalFilesDir(null), "test-screenshots").apply { mkdirs() }
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun track(id: Long) = Track(id, "Track $id", "Album", "Artist", 185000,
        "2020-01-01", "Rock", "USA", "", null)
}
