package mekong.ditagis.com.qlts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import mekong.ditagis.com.qlts.async.LoginTask
import mekong.ditagis.com.qlts.entities.User
import mekong.ditagis.com.qlts.utities.CheckConnectInternet
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import mekong.ditagis.com.qlts.utities.Preference


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var mApplication: DApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.mApplication = application as DApplication
        btnLogin.setOnClickListener(this)

//        txtUsername.setText("tiwamytho_qlml")
//        txtPassword.setText("tiwamytho_qlml")
        txtVersion.text = "v" + packageManager.getPackageInfo(packageName, 0).versionName
        create()
    }

    private fun create() {
        Preference.instance.setContext(this)
        val username = Preference.instance.loadPreference(Constant.PreferenceKey.USERNAME)
        val password = Preference.instance.loadPreference(Constant.PreferenceKey.PASSWORD)
        if (username != null && password != null) {
            txtUsername.setText(username)
            txtPassword.setText(password)
        }
    }

    private fun login() {
        if (!CheckConnectInternet.isOnline(this)) {
            txtLoginValidation.setText(R.string.validate_no_connect)
            txtLoginValidation.visibility = View.VISIBLE
            return
        }
        txtLoginValidation.visibility = View.GONE

        val username = txtUsername.text.toString()
        val password = txtPassword.text.toString()
        if (username!!.isEmpty() || password.isEmpty()) {
            handleInfoLoginEmpty()
            return
        }
        LoginTask(object : LoginTask.Response {
            override fun post(user: User?) {
                if (user != null) {
                    mApplication!!.user = user
                    handleLoginSuccess(username, password)
                } else handleLoginFail()
            }
        }).execute(this@LoginActivity, username, password)
    }

    private fun handleInfoLoginEmpty() {
        txtLoginValidation.setText(R.string.info_login_empty)
        txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        txtLoginValidation.setText(R.string.validate_login_fail)
        txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(username: String, password: String) {


        Preference.instance.savePreferences(Constant.PreferenceKey.USERNAME, username)
        Preference.instance.savePreferences(Constant.PreferenceKey.PASSWORD, password)
        txtUsername.setText("");
        txtPassword.setText("");
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        txtUsername.setText("")
        txtPassword.setText("")

        Preference.instance.savePreferences(Constant.PreferenceKey.USERNAME, "")
        create()
    }

    override fun onPostResume() {
        super.onPostResume()
        create()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> login()
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (txtPassword.text.toString().isNotEmpty()) {
                    login()
                    return true
                }
                return super.onKeyUp(keyCode, event)
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}
