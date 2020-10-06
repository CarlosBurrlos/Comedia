package com.purdue.comedia

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var signingUp = true
    private val ref = FirebaseDatabase.getInstance().getReference("username")

    // Required for interacting with realtime database
    class LoginUser(val id: String, val username: String, val email: String) {
        constructor() : this("", "", "") {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()

        supportActionBar?.title = "Sign Up"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        signInLoader.isVisible = false

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

        var email = ""

        if (!Patterns.EMAIL_ADDRESS.matcher(registerUsername.text).matches()) {
            // Get username's corresponding email from firebase database
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        for (aUser in p0.children) {
                            val userObject = aUser.getValue(LoginUser::class.java)
                            if (userObject!!.username == registerUsername.text.toString()
                                    .toLowerCase()
                            ) {
                                email = userObject.email
                                break
                            }
                        }
                    }
                    if (email.isEmpty()) {
                        signInLoader.isVisible = false
                        snack("Username does not exist. Please sign up for an account.")
                        return
                    }
                    performFirebaseLogin(email) // Sign In
                }

                override fun onCancelled(p0: DatabaseError) {
                    signInLoader.isVisible = false
                    snack("Unable to create account. Err Code: *READ_ERR")
                }
            })

        } else {
            // Else was email entered
            email = registerUsername.text.toString()
            performFirebaseLogin(email)
        }

    }

    private fun performFirebaseLogin(email: String) {
        auth.signInWithEmailAndPassword(email, registerPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    signInLoader.isVisible = false
                    if (user!!.isEmailVerified) {
                        updateUI(true, user)
                    } else {
                        auth.signOut()
                        snack("Please verify your email address")
                    }
                } else {
                    // If sign in fails, display why to the user.
                    Log.w("*Fail", "createUserWithEmail:failure", task.exception)
                    signInLoader.isVisible = false
                    snack(task.exception?.message.toString())
                }
            }
    }

    private fun signUpUser() {
        // Check for unique username
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var isUnique = true
                if (p0.exists()) {
                    for (aUser in p0.children) {
                        val userObject = aUser.getValue(LoginUser::class.java)
                        if (userObject!!.username == registerUsername.text.toString()) {
                            isUnique = false
                            break
                        }
                    }
                }

                if (isUnique) {
                    createFirebaseUserAccount() // Username is unique. Continue with account creation.
                } else {
                    signInLoader.isVisible = false
                    snack("Username already exists. Please select a new username.")
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                signInLoader.isVisible = false
                snack("Unable to create account. Err Code: *READ_ERR")
            }
        })
    }

    private fun createFirebaseUserAccount() {
        // Checks completed. Continue with sign up. Create new account.
        auth.createUserWithEmailAndPassword(
            registerEmail.text.toString(),
            registerPassword.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save username and email pair to Firebase database
                    val userID = ref.push().key // Creates key inside username
                    val theUsername = registerUsername.text.toString()
                    val theEmail = registerEmail.text.toString()
                    if (userID != null) {
                        ref.child(userID).setValue(LoginUser(userID, theUsername, theEmail))
                    } else {
                        signInLoader.isVisible = false
                        snack("Unable to create account. Err Code: *WRITE_ERR")
                        return@addOnCompleteListener
                    }

                    // Send email verification
                    auth.currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { emailTask ->
                            if (emailTask.isSuccessful) {
                                createNewFirebaseUser() // Add new user to firebase database
                                auth.signOut()
                                signInLoader.isVisible = false
                                snack("Email Sent. Verify email and login.")
                                toggleSignInAndSignUp()
                            } else {
                                signInLoader.isVisible = false
                                snack(emailTask.exception?.message.toString())
                            }
                        }
                } else {
                    // If sign in fails, display why to the user.
                    signInLoader.isVisible = false
                    snack(task.exception?.message.toString())
                }
            }
    }

    // Called after a new user has registered
    private fun createNewFirebaseUser() {
        val username = registerUsername.text.toString()
        val email = registerEmail.text.toString()

        // Todo: Create new user on Firebase
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