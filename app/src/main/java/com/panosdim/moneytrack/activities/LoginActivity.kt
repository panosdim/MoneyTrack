package com.panosdim.moneytrack.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.prefs
import com.panosdim.moneytrack.repository
import com.panosdim.moneytrack.rest.requests.LoginRequest
import com.panosdim.moneytrack.utils.downloadData
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindProgressButton(btnLogin)
        btnLogin.attachTextChangeAnimator()

        btnLogin.setOnClickListener { attemptLogin() }
    }

    override fun onResume() {
        super.onResume()
        tvEmail.setText(prefs.email)
        tvPassword.setText(prefs.password)
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        tvEmail.error = null
        tvPassword.error = null

        // Store values at the time of the login attempt.
        val email = tvEmail.text.toString()
        val password = tvPassword.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (password.isEmpty() || !isPasswordValid(password)) {
            tvPassword.error = getString(R.string.error_invalid_password)
            focusView = tvPassword
            cancel = true
        }

        // Check for a valid email address.
        if (email.isEmpty()) {
            tvEmail.error = getString(R.string.error_field_required)
            focusView = tvEmail
            cancel = true
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmail.error = getString(R.string.error_invalid_email)
            focusView = tvEmail
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            btnLogin.showProgress {
                buttonTextRes =
                    R.string.checking_credentials
                progressColor = Color.WHITE
            }
            val scope = CoroutineScope(Dispatchers.Main)

            scope.launch() {
                try {
                    withContext(Dispatchers.IO) {
                        val response = repository.login(LoginRequest(email, password))
                        prefs.token = response.token
                        prefs.email = tvEmail.text.toString()
                        prefs.password = tvPassword.text.toString()

                        downloadData(this@LoginActivity)
                    }

                    btnLogin.hideProgress(R.string.sign_in)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } catch (e: HttpException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login was unsuccessful. Please check your email or password.",
                        Toast.LENGTH_LONG
                    ).show()
                    btnLogin.hideProgress(R.string.sign_in)
                }
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }
}

