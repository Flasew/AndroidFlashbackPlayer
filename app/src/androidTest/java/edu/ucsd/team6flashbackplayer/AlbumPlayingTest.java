package edu.ucsd.team6flashbackplayer;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AlbumPlayingTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void albumPlayingTest() {
        ViewInteraction button = onView(
                allOf(withId(R.id.main_albums),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                0)),
                                2),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.main_albums), withText("Albums"),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.main_albums), withText("Albums"),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                0)),
                                4),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.entry_name), withText("Love Is Everywhere"),
                        childAtPosition(
                                allOf(withId(R.id.album_entry),
                                        childAtPosition(
                                                withId(R.id.album_list),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Love Is Everywhere")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Song"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                2),
                        isDisplayed()));
        textView2.check(matches(withText("Song")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Artist"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                4),
                        isDisplayed()));
        textView3.check(matches(withText("Artist")));

        ViewInteraction listView = onView(
                allOf(withId(R.id.album_list),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                        0),
                                1),
                        isDisplayed()));
        listView.check(matches(isDisplayed()));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.curr_playing), withText("Currently Playing"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                1),
                        isDisplayed()));
        textView4.check(matches(withText("Currently Playing")));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.fb_button),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                        0),
                                0),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        DataInteraction constraintLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.album_list),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(0);
        constraintLayout.perform(click());

        ViewInteraction viewGroup = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(android.R.id.content),
                                0),
                        0),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.fb_button),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                        0),
                                0),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.song_name), withText("Can't Find Love"),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                0),
                        isDisplayed()));
        textView5.check(matches(withText("Can't Find Love")));

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.like_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                1),
                        isDisplayed()));
        imageButton.check(matches(isDisplayed()));

        ViewInteraction imageButton2 = onView(
                allOf(withId(R.id.dislike_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                2),
                        isDisplayed()));
        imageButton2.check(matches(isDisplayed()));

        ViewInteraction viewGroup2 = onView(
                allOf(withId(R.id.song_entry),
                        childAtPosition(
                                allOf(withId(R.id.song_list),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                1)),
                                0),
                        isDisplayed()));
        viewGroup2.check(matches(isDisplayed()));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.curr_playing), withText("Currently Playing"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                1),
                        isDisplayed()));
        textView6.check(matches(withText("Currently Playing")));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Can't Find Love"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                2),
                        isDisplayed()));
        textView7.check(matches(withText("Can't Find Love")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Stacy Jones"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                4),
                        isDisplayed()));
        textView8.check(matches(withText("Stacy Jones")));

        ViewInteraction textView9 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Stacy Jones"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                4),
                        isDisplayed()));
        textView9.check(matches(withText("Stacy Jones")));

        DataInteraction constraintLayout2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.song_list),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(0);
        constraintLayout2.perform(click());

        ViewInteraction textView10 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Can't Find Love"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                2),
                        isDisplayed()));
        textView10.check(matches(withText("Can't Find Love")));

        ViewInteraction textView11 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Stacy Jones"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                4),
                        isDisplayed()));
        textView11.check(matches(withText("Stacy Jones")));

        ViewInteraction textView12 = onView(
                allOf(withId(R.id.curr_playing), withText("Currently Playing"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                1),
                        isDisplayed()));
        textView12.check(matches(withText("Currently Playing")));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
