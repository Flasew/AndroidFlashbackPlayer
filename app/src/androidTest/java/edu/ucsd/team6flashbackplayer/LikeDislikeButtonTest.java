package edu.ucsd.team6flashbackplayer;


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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LikeDislikeButtonTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void likeDislikeButtonTest() {
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

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.like_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        withParent(withId(R.id.song_list))),
                                2),
                        isDisplayed()));
        appCompatImageButton.perform(click());

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
                allOf(withId(R.id.like_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                1),
                        isDisplayed()));
        imageButton2.check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.like_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        withParent(withId(R.id.song_list))),
                                2),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        ViewInteraction imageButton3 = onView(
                allOf(withId(R.id.like_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                1),
                        isDisplayed()));
        imageButton3.check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.dislike_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        withParent(withId(R.id.song_list))),
                                1),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        ViewInteraction imageButton4 = onView(
                allOf(withId(R.id.dislike_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                2),
                        isDisplayed()));
        imageButton4.check(matches(isDisplayed()));

        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.dislike_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        withParent(withId(R.id.song_list))),
                                1),
                        isDisplayed()));
        appCompatImageButton4.perform(click());

        ViewInteraction imageButton5 = onView(
                allOf(withId(R.id.dislike_button),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                2),
                        isDisplayed()));
        imageButton5.check(matches(isDisplayed()));

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
