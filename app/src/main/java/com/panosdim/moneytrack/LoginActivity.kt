package com.panosdim.moneytrack

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.panosdim.moneytrack.network.checkForActiveSession
import com.panosdim.moneytrack.network.login
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : AppCompatActivity() {

    // UI references.
    private lateinit var mSnackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
        tvPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        btnLogin.setOnClickListener { attemptLogin() }

        // Check if we return from logout button
        // Get the Intent that started this activity and extract the string
        val loggedOut = intent.getBooleanExtra(LOGGEDOUT_MESSAGE, false)
        if (!loggedOut) {
            // Show a progress spinner, and kick off a background task to
            // check for active session.
            mSnackbar = Snackbar.make(root_layout, "Checking for active session!", Snackbar.LENGTH_LONG)
            mSnackbar.show()
            showProgress(true)
            checkForActiveSession {
                val resp = JSONObject(it)
                if (resp.getBoolean("success")) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    mSnackbar.dismiss()
                    startActivity(intent)
                } else {
                    showProgress(false)
                    mSnackbar.dismiss()
                }
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        tvUsername.error = null
        tvPassword.error = null

        // Store values at the time of the login attempt.
        val username = tvUsername.text.toString()
        val password = tvPassword.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            tvPassword.error = getString(R.string.error_invalid_password)
            focusView = tvPassword
            cancel = true
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            tvUsername.error = getString(R.string.error_field_required)
            focusView = tvUsername
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            val jsonParam = JSONObject()
            jsonParam.put("username", username)
            jsonParam.put("password", password)

            login({
                showProgress(false)

                val resp = JSONObject(it)
                if (resp.getBoolean("success")) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, resp.getString("message"),
                            Toast.LENGTH_LONG).show()
                }
            }, jsonParam)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                login_form.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                login_progress.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }
}

