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
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FBPageTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void fBPageTest() {
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.fb_button), withText("FLASHBACK"),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction button = onView(
                allOf(withId(R.id.fb_button)
                        ));
        button.check(matches(isDisplayed()));

        ViewInteraction textView = onView(allOf(withId(R.id.song_name)));
        textView.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(allOf(withId(R.id.song_artist)));
        textView2.check(matches(isDisplayed()));

        ViewInteraction imageButton = onView(allOf(withId(R.id.like_button)));
        imageButton.check(matches(isDisplayed()));

        ViewInteraction imageButton2 = onView(allOf(withId(R.id.dislike_button)));
        imageButton2.check(matches(isDisplayed()));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.location_lab), withText("Location:")));
        textView3.check(matches(withText("Location:")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.time_lab), withText("Time:")));
        textView4.check(matches(withText("Time:")));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.show_playlist)));
        button2.check(matches(isDisplayed()));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.time_date_txt)));
        textView5.check(matches(isDisplayed()));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.time_clock_txt), withText("---")));
        textView6.check(matches(isDisplayed()));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.time_clock_txt), withText("---")));
        textView7.check(matches(isDisplayed()));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.fb_button), withText("FLASHBACK")));
        appCompatButton2.perform(click());

        ViewInteraction textView8 = onView(
                allOf(withText("Currently Playing")));
        textView8.check(matches(withText("Currently Playing")));

        ViewInteraction textView9 = onView(
                allOf(withText("Currently Playing")));
        textView9.check(matches(withText("Currently Playing")));

        pressBack();

        ViewInteraction button3 = onView(
                allOf(withId(R.id.main_songs)));
        button3.check(matches(isDisplayed()));

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
