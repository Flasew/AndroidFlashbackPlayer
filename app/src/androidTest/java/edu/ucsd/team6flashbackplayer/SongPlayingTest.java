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
public class SongPlayingTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void songPlayingTest() {
        ViewInteraction button = onView(
                allOf(withId(R.id.main_songs),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                0)),
                                1),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.main_songs), withText("Songs"),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.song_name), withText("Can't Find Love"),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Can't Find Love")));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.fb_button),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                        0),
                                0),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

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

        ViewInteraction viewGroup = onView(
                allOf(withId(R.id.song_entry),
                        childAtPosition(
                                allOf(withId(R.id.song_list),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                1)),
                                0),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.curr_playing), withText("Currently Playing"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("Currently Playing")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Song"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                2),
                        isDisplayed()));
        textView3.check(matches(withText("Song")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Artist"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                4),
                        isDisplayed()));
        textView4.check(matches(withText("Artist")));

        DataInteraction constraintLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.song_list),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(0);
        constraintLayout.perform(click());

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Can't Find Love"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                2),
                        isDisplayed()));
        textView5.check(matches(withText("Can't Find Love")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Stacy Jones"),
                        childAtPosition(
                                allOf(withId(R.id.current_song),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                2)),
                                4),
                        isDisplayed()));
        textView6.check(matches(withText("Stacy Jones")));

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
