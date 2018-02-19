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
import static android.support.test.espresso.Espresso.pressBack;
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
public class PlaySongTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void playSongTest() {
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

        DataInteraction constraintLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.song_list),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(0);
        constraintLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.curr_playing_name), withText("Ana (full ver.)")));
        textView.check(matches(withText("Ana (full ver.)")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Lia")));
        textView2.check(matches(withText("Lia")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Lia")));
        textView3.check(matches(withText("Lia")));

        ViewInteraction constraintLayout2 = onView(
                allOf(withId(R.id.current_song),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.constraint.ConstraintLayout")),
                                        0),
                                4),
                        isDisplayed()));
        constraintLayout2.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.song_name), withText("Ana (full ver.)")));
        textView4.check(matches(withText("Ana (full ver.)")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.song_artist), withText("Lia")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.song_artist), withText("Lia")));
        textView6.check(matches(withText("Lia")));

        pressBack();

        pressBack();

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.curr_playing_name)));
        textView7.check(matches(withText("Ana (full ver.)")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Lia")));
        textView8.check(matches(withText("Lia")));

        ViewInteraction textView9 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("Lia")));
        textView9.check(matches(withText("Lia")));

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
