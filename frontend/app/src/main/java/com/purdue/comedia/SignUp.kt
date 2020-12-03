package com.purdue.comedia

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var signingUp = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()

        supportActionBar?.title = "Sign Up"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        signInLoader.isVisible = false

        registerEmail.isClickable = true
        registerEmail.isFocusable = true

        // Button to switch between logging in and signing up
        btnToggleRegister.setOnClickListener {
            toggleSignInAndSignUp()
        }

        btnRegister.setOnClickListener {
            if (checkInputFields(signingUp)) {
                signInLoader.isVisible = true
                if (signingUp) {
                    signUpUser() // Sign up user
                } else {
                    loginUser() // Login user
                }
            }
        }

    }

    private fun loginUser() {
        val email: String

        if (!Patterns.EMAIL_ADDRESS.matcher(registerUsername.text).matches()) {
            FirestoreUtility.queryForEmailByName(registerUsername.text.toString())
                .addOnSuccessListener { performFirebaseLogin(it) }
                .addOnFailureListener {
                    signInLoader.isVisible = false
                    snack("Username does not exist. Please sign up for an account.")
                }

        } else {
            // Else was email entered
            email = registerUsername.text.toString()
            performFirebaseLogin(email)
        }

    }

    private fun performFirebaseLogin(email: String) {
        FirestoreUtility.isInternetWorking { isWorking ->
            runOnUiThread {
                if (isWorking) {
                    AuthUtility.signIn(email, registerPassword.text.toString())
                        .addOnSuccessListener {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            signInLoader.isVisible = false
                            if (user!!.isEmailVerified) {
                                updateUI(true, user)
                            } else {
                                AuthUtility.signOut()
                                snack("Please verify your email address")
                            }
                        }
                        .addOnFailureListener {
                            // If sign in fails, display why to the user.
                            Log.w("*Fail", "createUserWithEmail:failure", it)
                            signInLoader.isVisible = false
                            snack(it.message.toString())
                        }
                } else {
                    signInLoader.isVisible = false
                    snack("Please check internet connection and try again.")
                }
            }
        }
    }

    private fun signUpUser() {
        val username = registerUsername.text.toString()
        FirestoreUtility.queryForUserByName(username)
            .addOnSuccessListener {
                signInLoader.isVisible = false
                snack("Username already exists. Please select a new username.")
            }
            .addOnFailureListener {
                createFirebaseUserAccount()
            }
    }

    private fun createFirebaseUserAccount() {
        // Checks completed. Continue with sign up. Create new account.

        AuthUtility.createAccount(
            registerEmail.text.toString(),
            registerPassword.text.toString()
        )
            .addOnSuccessListener {
                val userID = auth.uid!!
                val username = registerUsername.text.toString()
                val email = registerEmail.text.toString()

                AuthUtility.addNewUser(userID, username, email)
                    .continueWithTask {
                        auth.currentUser!!.sendEmailVerification()
                    }
                    .addOnSuccessListener {
                        AuthUtility.signOut()
                        signInLoader.isVisible = false
                        snack("Email Sent. Verify email and login.")
                        toggleSignInAndSignUp()
                    }
                    .addOnFailureListener {
                        signInLoader.isVisible = false
                        snack("Unable to create account. Err: $it")
                    }
            }
            .addOnFailureListener {
                // If sign in fails, display why to the user.
                signInLoader.isVisible = false
                snack(it.message.toString())
            }
    }

    private fun checkInputFields(signingUp: Boolean): Boolean {

        // Check unique username
        if (registerUsername.text.isEmpty()) {
            registerUsername.error = "Please Enter Username"
            registerUsername.requestFocus()
            return false
        } else if (signingUp && registerUsername.text.contains(regex = Regex("[^a-z1-9]"))) {
            registerUsername.error = "Username can only contain lowercase letters from a-z"
            registerUsername.requestFocus()
            return false
        }

        // Check password. Needs to have 6 characters
        if (registerPassword.text.isEmpty()) {
            registerPassword.error = "Please enter a password of length 6+"
            registerPassword.requestFocus()
            return false
        }

        // Check email address
        if (signingUp && !Patterns.EMAIL_ADDRESS.matcher(registerEmail.text).matches()) {
            registerEmail.error = "Please Enter a valid email"
            registerEmail.requestFocus()
            return false
        }

        return true
    }

    private fun toggleSignInAndSignUp() {
        if (btnToggleRegister.text.contains("create")) {
            signingUp = true
            signUpTextView.text = "Sign Up"
            supportActionBar?.title = "Sign Up"
            registerUsername.hint = "Username"
            registerEmail.alpha = 1F
            registerEmail.isClickable = true
            registerEmail.isFocusable = true
            registerEmail.isFocusableInTouchMode = true
            btnRegister.text = "Register"
            btnToggleRegister.text = "Already have an account? Login here"
        } else {
            signingUp = false
            signUpTextView.text = "Login"
            supportActionBar?.title = "Login"
            registerUsername.hint = "Username/Email"
            registerEmail.alpha = 0F
            registerEmail.isClickable = false
            registerEmail.isFocusable = false
            btnRegister.text = "Login"
            btnToggleRegister.text = "Click here to create a new account"
        }
    }

    // Auto-runs and checks if the user is signed in
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(false, currentUser)
    }

    // Called once login flow has been completed
    private fun updateUI(isCustomCheck: Boolean, currentUser: FirebaseUser?) {
        // To Move to different screen: startActivity(Intent(this, loginActivity::class.java)); finish()
        if (isCustomCheck) {
            if (currentUser == null) {
                // Sign up failed.
                snack("Sign Up Failed.")
            } else if (currentUser.isEmailVerified) {
                // Sign up successful.
                toast("Sign In Successful!")

                // Go back to previous page
                finish()
            }
        }

    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

    // Hide Keyboard on a non-text field screen tap
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun snack(str: String) {
        Snackbar.make(findViewById(android.R.id.content), str, Snackbar.LENGTH_LONG).show()
    }

    private fun toast(str: String) {
        Toast.makeText(baseContext, str, Toast.LENGTH_LONG).show()
    }

}
