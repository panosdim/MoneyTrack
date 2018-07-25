package com.panosdim.moneytrack

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

const val EXTRA_MESSAGE = "com.panosdim.moneytrack.MESSAGE"

/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : AppCompatActivity() {

    // UI references.
    private var mUsernameView: EditText? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
        mUsernameView = findViewById(R.id.username) as EditText
        mPasswordView = findViewById(R.id.password) as EditText
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        val mSignInButton = findViewById(R.id.sign_in_button) as Button
        mSignInButton.setOnClickListener { attemptLogin() }

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)

        // Check if we return from logout button
        // Get the Intent that started this activity and extract the string
        val loggedOut = intent.getBooleanExtra(EXTRA_MESSAGE, false)
        if (!loggedOut) {
            // Show a progress spinner, and kick off a background task to
            // check for active session.
            snackbar = Snackbar.make(findViewById(android.R.id.content), "Checking for active session!", Snackbar.LENGTH_LONG)
            snackbar!!.show()
            showProgress(true)
            CheckSessionTask(this).execute()
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        mUsernameView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val username = mUsernameView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView!!.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView!!.error = getString(R.string.error_field_required)
            focusView = mUsernameView
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
            UserLoginTask(this).execute(username, password)
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

        mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
        mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    companion object {
        class CheckSessionTask internal constructor(context: LoginActivity) : AsyncTask<Void, Void, String>() {
            private val context: WeakReference<LoginActivity> = WeakReference(context)

            override fun doInBackground(vararg params: Void): String? {
                val wsh = WebServiceHandler()
                return wsh.performGetCall("php/session.php")
            }

            override fun onPostExecute(result: String?) {
                try {
                    val resp = JSONObject(result)
                    if (resp.getBoolean("loggedIn")) {
                        val intent = Intent(context.get(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.get()!!.snackbar!!.dismiss()
                        context.get()!!.startActivity(intent)
                    } else {
                        context.get()!!.showProgress(false);
                        context.get()!!.snackbar!!.dismiss()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Represents an asynchronous login/registration task used to authenticate
         * the user.
         */
        class UserLoginTask internal constructor(context: LoginActivity) : AsyncTask<String, Void, Boolean>() {
            private val context: WeakReference<LoginActivity> = WeakReference(context)

            override fun doInBackground(vararg params: String): Boolean? {
                // attempt authentication against a network service.
                val jsonParam = JSONObject()
                try {
                    jsonParam.put("username", params[0])
                    jsonParam.put("password", params[1])
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val wsh = WebServiceHandler()
                val result = wsh.performPostCall("php/login.php", jsonParam)
                try {
                    val resp = JSONObject(result)
                    return resp.getString("status") != "error"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                return false
            }

            override fun onPostExecute(success: Boolean?) {
                context.get()!!.showProgress(false)

                if (success!!) {
                    val intent = Intent(context.get(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.get()!!.startActivity(intent)
                } else {
                    Toast.makeText(context.get(), "Login Failed. Please check username and password!",
                            Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled() {
                context.get()!!.showProgress(false)
            }
        }
    }
}

