package com.purdue.comedia

import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matcher

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
    /*
    @Test
    fun testFollowGenre() {
        signIn()

        ActivityScenario.launch(MainActivity::class.java)
        FragmentTransaction.TRANSIT_ENTER_MASK
        onView(withId(R.id.btnBrowseGenre)).perform(click())
        Thread.sleep(1000)
        onView(withHint("Genre: Eg. Pun")).perform(typeText("TestPun"))
        onView(withText("CONFIRM")).perform(click())
        Thread.sleep(5000)

        signOut()
    }

    private fun signIn() {
        ActivityScenario.launch(SignUp::class.java)
        onView(withId(R.id.btnToggleRegister)).perform(click())
        onView(withId(R.id.registerUsername)).perform(typeText("pallav"))
        onView(withId(R.id.registerPassword)).perform(typeText("123456"))
        onView(withId(R.id.btnRegister)).perform(click())

        Thread.sleep(3000)
    }

    private fun signOut() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnToLoginPage)).check(matches(withText("Sign Out")))
        FirebaseAuth.getInstance().signOut()
        Thread.sleep(2000)
    }

    fun clickItemWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController, view: View) {
                val v = view.findViewById(id) as View
                v.performClick()
            }
        }
    }

    @Test
    fun testViewUserProfile() {
        signIn()

        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(5000)

        signOut()
    }
    */
}
