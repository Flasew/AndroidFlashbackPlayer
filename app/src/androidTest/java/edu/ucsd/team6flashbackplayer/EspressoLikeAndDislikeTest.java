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
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EspressoLikeAndDislikeTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void likeAndDislikeTest() {
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

        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.like_button))
                .check(matches(isDisplayed()));

        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.dislike_button))
                .check(matches(isDisplayed()));

        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.song_name))
                .check(matches(isDisplayed()));


        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.like_button))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.dislike_button))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.like_button))
                .check(matches(isDisplayed()));

        onData(anything())
                .inAdapterView(withId(R.id.song_list))
                .atPosition(0)
                .onChildView(withId(R.id.dislike_button))
                .check(matches(isDisplayed()));

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
