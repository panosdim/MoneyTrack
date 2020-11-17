package com.panosdim.moneytrack

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.panosdim.moneytrack.api.Webservice
import com.panosdim.moneytrack.api.data.LoginRequest
import com.panosdim.moneytrack.api.data.LoginResponse
import com.panosdim.moneytrack.api.webservice
import com.panosdim.moneytrack.utils.generateTextWatcher
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
    private var client: Webservice = webservice
    private val textWatcher = generateTextWatcher(::validateForm)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        username.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

        password.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                login()
            }
            false
        }

        login.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val scope = CoroutineScope(Dispatchers.Main)
        lateinit var response: LoginResponse
        val username = username.text.toString()
        val password = password.text.toString()
        loading.visibility = View.VISIBLE
        login.isEnabled = false

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    response = client.login(
                        LoginRequest(username, password)
                    )
                    prefs.token = response.token
                    prefs.email = username
                    prefs.password = password
                }
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                //Complete and destroy login activity once successful
                finish()
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, R.string.login_failed, Toast.LENGTH_SHORT)
                    .show()
            } finally {
                loading.visibility = View.GONE
                login.isEnabled = true
            }
        }
    }

    private fun validateForm() {
        login.isEnabled = true
        username.error = null
        password.error = null

        // Store values.
        val email = username.text.toString()
        val pass = password.text.toString()

        if (!isUserNameValid(email)) {
            username.error = getString(R.string.invalid_email)
            login.isEnabled = false
        }

        if (!isPasswordValid(pass)) {
            password.error = getString(R.string.invalid_password)
            login.isEnabled = false
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            false
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isFormValid(): Boolean {
        return username.error == null && password.error == null
    }
}