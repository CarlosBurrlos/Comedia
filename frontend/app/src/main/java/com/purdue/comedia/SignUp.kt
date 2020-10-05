package com.purdue.comedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    var signingUp = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()

        supportActionBar?.title = "Sign Up"

        // Button to switch between logging in and signing up
        btnToggleRegister.setOnClickListener {
            toggleSignInAndSignUp()
        }

        btnRegister.setOnClickListener {
            if (checkInputFields(signingUp)) {
                if (signingUp) {
                    signUpUser() // Sign up user
                } else {
                    loginUser() // Login user
                }
            }
        }

    }

    private fun loginUser() {
        // Todo: Login User. Check username and password through firebase.
        auth.signInWithEmailAndPassword(
            registerUsername.text.toString(),
            registerPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    if (user!!.isEmailVerified) {
                        updateUI(true, user)
                    } else {
                        auth.signOut()
                        toast("Please verify your email address")
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("*Fail", "createUserWithEmail:failure", task.exception)
                    toast(task.exception?.message.toString())
                }

            }
    }

    private fun signUpUser() {
        // Checks completed. Continue with sign up. Create new account.
        auth.createUserWithEmailAndPassword(
            registerEmail.text.toString(),
            registerPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Send email verification
                    auth.currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.signOut()
                                toast("Email Sent. Verify email and login.")
                                toggleSignInAndSignUp()
                                // Sign in and verification successful
                                //updateUI(true, auth.currentUser)
                            } else {
                                toast(task.exception?.message.toString())
                            }
                        }
                } else {
                    // Sign in fails, display a message to the user.
                    Log.w("*Fail", "createUserWithEmail:failure", task.exception)
                    toast(task.exception?.message.toString())
                }
            }

    }

    private fun checkInputFields(signingUp: Boolean): Boolean {

        // Check unique username
        if (registerUsername.text.isEmpty()) {
            registerUsername.error = "Please Enter Username"
            registerUsername.requestFocus()
            return false
        } else if (registerUsername.text.contains("[^a-z]")) {
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
            registerEmail.alpha = 1F
            registerEmail.isClickable = true
            btnRegister.text = "Register"
            btnToggleRegister.text = "Already have an account? Login here"
        } else {
            signingUp = false
            signUpTextView.text = "Login"
            supportActionBar?.title = "Login"
            registerEmail.alpha = 0F
            registerEmail.isClickable = false
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
                toast("Sign Up Failed.")
            } else {
                // Sign up successful.
                toast("Sign In Successful!")

                // Go back to previous page
                finish()
            }
        }

    }

    private fun toast(str: String) {
        if (str.length < 60) Toast.makeText(baseContext, str, Toast.LENGTH_SHORT).show()
        else Toast.makeText(baseContext, str, Toast.LENGTH_LONG).show()
    }

}