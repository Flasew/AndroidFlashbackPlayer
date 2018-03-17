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
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoSongPageTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void espressoSongPageTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.main_songs), withText("Songs"),
                        childAtPosition(
                                allOf(withId(R.id.main_layout),
                                        childAtPosition(
                                                withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                                1)),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView = onView(
                allOf(withText("Songs"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Songs")));


        ViewInteraction textView2 = onView(
                allOf(withId(R.id.song_name), withText("Universal Fanfare"),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("Universal Fanfare")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.song_attrs), withText("The Minions • Minions (Original Motion Picture Soundtrack)"),
                        childAtPosition(
                                allOf(withId(R.id.song_entry),
                                        childAtPosition(
                                                withId(R.id.song_list),
                                                0)),
                                1),
                        isDisplayed()));
        textView3.check(matches(withText("The Minions • Minions (Original Motion Picture Soundtrack)")));

        ViewInteraction textView4 = onView(
                allOf(withText("Currently Playing")));
        textView4.check(matches(withText("Currently Playing")));

        ViewInteraction imageView = onView(
                allOf(withId(R.id.pause_play)));
        imageView.check(matches(isDisplayed()));

        ViewInteraction imageView2 = onView(
                allOf(withId(R.id.skip)));
        imageView2.check(matches(isDisplayed()));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.curr_playing_name), withText("---")));
        textView5.check(matches(withText("---")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("---")));
        textView6.check(matches(withText("---")));


        ViewInteraction textView8 = onView(
                allOf(withId(R.id.add_song)));
        textView8.check(matches(withText("")));

        ViewInteraction imageView3 = onView(
                allOf(withContentDescription("More options"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                1),
                        isDisplayed()));
        imageView3.check(matches(isDisplayed()));


        DataInteraction constraintLayout = onData(anything())
                .inAdapterView(allOf(withId(R.id.song_list),
                        childAtPosition(
                                withClassName(is("android.support.constraint.ConstraintLayout")),
                                3)))
                .atPosition(0);
        constraintLayout.perform(click());

        ViewInteraction imageView5 = onView(
                allOf(withId(R.id.pause_play)));
        imageView5.check(matches(isDisplayed()));

        ViewInteraction imageView6 = onView(
                allOf(withId(R.id.skip)));
        imageView6.check(matches(isDisplayed()));

        ViewInteraction textView9 = onView(
                allOf(withId(R.id.curr_playing_name), withText("Universal Fanfare")));
        textView9.check(matches(withText("Universal Fanfare")));

        ViewInteraction textView10 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("The Minions")));
        textView10.check(matches(withText("The Minions")));

        ViewInteraction textView11 = onView(
                allOf(withId(R.id.curr_playing_artist), withText("The Minions")));
        textView11.check(matches(withText("The Minions")));

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.pause_play)));
        appCompatImageView.perform(click());

        ViewInteraction constraintLayout2 = onView(
                allOf(withId(R.id.current_song)));
        constraintLayout2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction textView12 = onView(
                allOf(withId(R.id.song_name), withText("Universal Fanfare")));
        textView12.check(matches(withText("Universal Fanfare")));

        ViewInteraction textView13 = onView(
                allOf(withId(R.id.song_artist), withText("The Minions")));
        textView13.check(matches(withText("The Minions")));

        ViewInteraction textView14 = onView(
                allOf(withId(R.id.song_album), withText("Minions (Original Motion Picture Soundtrack)")));
        textView14.check(matches(withText("Minions (Original Motion Picture Soundtrack)")));

        ViewInteraction imageView7 = onView(
                allOf(withId(R.id.pause_play)));
        imageView7.check(matches(isDisplayed()));

        ViewInteraction imageView8 = onView(
                allOf(withId(R.id.skip)));
        imageView8.check(matches(isDisplayed()));


        pressBack();

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pressBack();

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
