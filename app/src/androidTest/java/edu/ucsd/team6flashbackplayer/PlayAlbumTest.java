package edu.ucsd.team6flashbackplayer;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.CoreMatchers;
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
public class PlayAlbumTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void playAlbumTest() {
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

        ViewInteraction textView = onView(
                allOf(withId(R.id.entry_name), withText("Town of Windmill"),
                        childAtPosition(
                                allOf(withId(R.id.album_entry),
                                        childAtPosition(
                                                withId(R.id.album_list),
                                                1)),
                                0),
                        isDisplayed()));

        onData(CoreMatchers.anything())
                .inAdapterView(withId(R.id.album_list))
                .atPosition(1)
                .onChildView(withId(R.id.entry_name))
                .check(matches(withText("Town of Windmill")));



        DataInteraction constraintLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.album_list),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(1);
        constraintLayout.perform(click());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.song_name), withText("Town of Windmill")));
        textView3.check(matches(withText("Town of Windmill")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Town of Windmill")));
        textView4.check(matches(withText("Town of Windmill")));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView5.check(matches(withText("a_hisa")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView6.check(matches(withText("a_hisa")));


        ViewInteraction imageButton = onView(
                allOf(withId(R.id.like_button)));
        imageButton.check(matches(isDisplayed()));

        ViewInteraction imageButton2 = onView(
                allOf(withId(R.id.dislike_button)));
        imageButton2.check(matches(isDisplayed()));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Town of Windmill")));
        textView7.check(matches(withText("Town of Windmill")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView8.check(matches(withText("a_hisa")));

        pressBack();

        ViewInteraction textView9 = onView(
                allOf(withText("Albums"),
                        childAtPosition(
                                allOf(withId(R.id.action_bar),
                                        childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                0),
                        isDisplayed()));
        textView9.check(matches(withText("Albums")));

        ViewInteraction textView10 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Town of Windmill")));
        textView10.check(matches(withText("Town of Windmill")));

        ViewInteraction textView11 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView11.check(matches(withText("a_hisa")));

        ViewInteraction textView12 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView12.check(matches(withText("a_hisa")));

        pressBack();

        ViewInteraction textView13 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Town of Windmill")));
        textView13.check(matches(withText("Town of Windmill")));

        ViewInteraction textView14 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView14.check(matches(withText("a_hisa")));

        ViewInteraction textView15 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("a_hisa")));
        textView15.check(matches(withText("a_hisa")));

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
