package mekong.ditagis.com.qlts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import mekong.ditagis.com.qlts.async.LoginTask
import mekong.ditagis.com.qlts.databinding.ActivityLoginBinding
import mekong.ditagis.com.qlts.entities.entitiesDB.User
import mekong.ditagis.com.qlts.utities.CheckConnectInternet
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import mekong.ditagis.com.qlts.utities.Preference


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityLoginBinding
    private var mApplication: DApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        this.mApplication = application as DApplication
        mBinding.btnLogin.setOnClickListener(this)

        mBinding.txtUsername.setText("tiwamytho_qlml")
        mBinding.txtPassword.setText("tiwamytho_qlml")
        mBinding.txtVersion.text = "v" + packageManager.getPackageInfo(packageName, 0).versionName
        create()
    }

    private fun create() {
        Preference.instance.setContext(this)
        val username = Preference.instance.loadPreference(Constant.PreferenceKey.USERNAME)
        val password = Preference.instance.loadPreference(Constant.PreferenceKey.PASSWORD)
        if (username != null && password != null) {
            mBinding.txtUsername.setText(username)
            mBinding.txtPassword.setText(password)
        }
    }

    private fun login() {
        if (!CheckConnectInternet.isOnline(this)) {
            mBinding.txtLoginValidation.setText(R.string.validate_no_connect)
            mBinding.txtLoginValidation.visibility = View.VISIBLE
            return
        }
        mBinding.txtLoginValidation.visibility = View.GONE

        val username = mBinding.txtUsername.text.toString()
        val password = mBinding.txtPassword.text.toString()
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
        mBinding.txtLoginValidation.setText(R.string.info_login_empty)
        mBinding.txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mBinding.txtLoginValidation.setText(R.string.validate_login_fail)
        mBinding.txtLoginValidation.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(username: String, password: String) {


        Preference.instance.savePreferences(Constant.PreferenceKey.USERNAME, username)
        Preference.instance.savePreferences(Constant.PreferenceKey.PASSWORD, password)
        mBinding.txtUsername.setText("");
        mBinding.txtPassword.setText("");
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        mBinding.txtUsername.setText("")
        mBinding.txtPassword.setText("")

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
                if (mBinding.txtPassword.text.toString().isNotEmpty()) {
                    login()
                    return true
                }
                return super.onKeyUp(keyCode, event)
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}
