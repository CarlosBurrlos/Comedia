package com.purdue.comedia

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.purdue.comedia", appContext.packageName)
    }

    @Before
    fun initValidString() {
        // Ensure logged out
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun testChangeSignUpText() {
        ActivityScenario.launch(SignUp::class.java)
        onView(withId(R.id.signUpTextView)).check(matches(withText("Sign Up")))
        onView(withId(R.id.btnToggleRegister)).perform(click())
        onView(withId(R.id.signUpTextView)).check(matches(withText("Login")))
    }

    @Test
    fun testChangeSignUpTextToggle() {
        ActivityScenario.launch(SignUp::class.java)
        onView(withId(R.id.signUpTextView)).check(matches(withText("Sign Up")))
        onView(withId(R.id.btnToggleRegister)).perform(click())
        onView(withId(R.id.signUpTextView)).check(matches(withText("Login")))
        onView(withId(R.id.btnToggleRegister)).perform(click())
        onView(withId(R.id.signUpTextView)).check(matches(withText("Sign Up")))
    }

    @Test
    fun testInitiallySignedOut() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnToLoginPage)).check(matches(withText("Sign In")))
    }

    @Test
    fun testUserLoginWithUsername() {
        ActivityScenario.launch(SignUp::class.java)
        onView(withId(R.id.btnToggleRegister)).perform(click())
        onView(withId(R.id.registerUsername)).perform(typeText("pallav"))
        onView(withId(R.id.registerPassword)).perform(typeText("123456"))
        onView(withId(R.id.btnRegister)).perform(click())

        Thread.sleep(3000)

        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnToLoginPage)).check(matches(withText("Sign Out")))
        FirebaseAuth.getInstance().signOut()
        Thread.sleep(2000)
        //onView(withId(R.id.btnToLoginPage)).perform(click()) // Sign back out
    }

    @Test
    fun testUserLoginWithEmail() {
        ActivityScenario.launch(SignUp::class.java)
        onView(withId(R.id.btnToggleRegister)).perform(click())
        onView(withId(R.id.registerUsername)).perform(typeText("agpallav@gmail.com"))
        onView(withId(R.id.registerPassword)).perform(typeText("123456"))
        onView(withId(R.id.btnRegister)).perform(click())

        Thread.sleep(3000)

        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnToLoginPage)).check(matches(withText("Sign Out")))
        FirebaseAuth.getInstance().signOut()
        Thread.sleep(2000)
        //onView(withId(R.id.btnToLoginPage)).perform(click()) // Sign back out
    }

    @Test
    fun testTextPostUI() {
        ActivityScenario.launch(CreatePostPage::class.java)
        onView(withId(R.id.radioBtnText)).perform(click())
        onView(withId(R.id.postBodyField)).check(matches(withHint("Joke Body")))
    }

    @Test
    fun testImagePostUI() {
        ActivityScenario.launch(CreatePostPage::class.java)
        onView(withId(R.id.radioBtnImage)).perform(click())
        onView(withId(R.id.postBodyField)).check(matches(withHint("Enter a joke image URL")))
    }

    @Test
    fun testURLPostUI() {
        ActivityScenario.launch(CreatePostPage::class.java)
        onView(withId(R.id.radioBtnUrl)).perform(click())
        onView(withId(R.id.postBodyField)).check(matches(withHint("Enter a joke website URL")))
    }
}
