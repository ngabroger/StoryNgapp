package com.ngabroger.storyngapp

import android.content.Intent
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.ngabroger.storyngapp.activity.LandingActivity
import com.ngabroger.storyngapp.activity.LoginActivity
import com.ngabroger.storyngapp.activity.MainActivity
import com.ngabroger.storyngapp.util.EspressoIdlingResource
import com.ngabroger.storyngapp.util.WaitActivityIsResumedIdlingResource
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @get:Rule
    val activity= ActivityScenarioRule(LoginActivity::class.java)

    private val resource = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private lateinit var homeActivityClassName: String
    private lateinit var waitActivityHome : WaitActivityIsResumedIdlingResource

    @Before
    fun setUp(){
        homeActivityClassName = MainActivity::class.java.name
        waitActivityHome = WaitActivityIsResumedIdlingResource(homeActivityClassName)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }
    @Test
    fun loginWithInvalidData(){
        val invalidEmail ="ngabroger"
        val invalidPassword = "gans"

        onView(withId(R.id.edEmailText))
            .perform(click())
            .perform(typeText(invalidEmail))
        onView(withId(R.id.textInputLayoutEmailLogin)).check(
            matches(
                hasDescendant(withText(resource.getString(R.string.email_is_not_valid)))

            )
        )
        onView(withId((R.id.edEmailText))).perform(clearText())
        onView(withId(R.id.textInputLayoutEmailLogin)).check(
            matches(
                hasDescendant(withText(resource.getString(R.string.email_cannot_be_empty)))
            )
        )

        onView(withId(R.id.edPasswordText))
            .perform(click())
            .perform(
                typeText(invalidPassword),
                closeSoftKeyboard()
                )
        onView(withId(R.id.textInputLayoutPasswordLogin)).check(
            matches(
                hasDescendant(withText(resource.getString(R.string.password_must_be_at_least_8_characters)))
            )
        )
        onView(withId(R.id.btnLogin)).perform(click())
        onView(withId(R.id.main)).check(doesNotExist())

    }
    @Test
    fun loginValidData(){
        val validEmail = "ngabroger@gmail.com"
        val validPassword = "testing123"

        onView(withId(R.id.edEmailText)).check(matches(isDisplayed()))
        onView(withId(R.id.edEmailText)).perform(click()).perform(typeText(validEmail),closeSoftKeyboard())
        onView(withId(R.id.textInputLayoutEmailLogin)).check(
            matches(
                allOf(
                    not(hasDescendant(withText(resource.getString(R.string.email_cannot_be_empty)))),
                    not(hasDescendant(withText(resource.getString(R.string.email_is_not_valid))))
                )
            )
        )
        onView(withId(R.id.edPasswordText)).check(matches(isDisplayed()))
        onView(withId(R.id.edPasswordText)).perform(click()).perform(typeText(validPassword),closeSoftKeyboard())
        onView(withId(R.id.textInputLayoutPasswordLogin)).check(
            matches(
                not(hasDescendant(withText(resource.getString(R.string.password_must_be_at_least_8_characters)))
                )
        )
        )
        onView(withId(R.id.btnLogin)).perform(click())

        IdlingRegistry.getInstance().register(waitActivityHome)
        try {
            intended(hasComponent(homeActivityClassName))
        }finally {
            IdlingRegistry.getInstance().unregister(waitActivityHome)
        }
        onView(withId(R.id.main)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogout)).perform(click())

        EspressoIdlingResource.increment()
        Thread.sleep(1000)

        EspressoIdlingResource.decrement()

        intended(hasComponent(LandingActivity::class.java.name))
    }
}