package vinhlong.ditagis.com.qlts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import vinhlong.ditagis.com.qlts.async.NewLoginAsycn
import vinhlong.ditagis.com.qlts.entities.entitiesDB.User
import vinhlong.ditagis.com.qlts.utities.CheckConnectInternet
import vinhlong.ditagis.com.qlts.utities.DApplication
import vinhlong.ditagis.com.qlts.utities.Preference


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var isLastLogin: Boolean = false

    private var mApplication: DApplication? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.mApplication = application as DApplication
        btnLogin.setOnClickListener(this)
        txt_login_changeAccount.setOnClickListener(this)


//        txtUsername!!.setText( "quanlymangluoi")
//        txtPassword!!.setText( "quanlymangluoi")
        create()
    }

    private fun create() {
        Preference.instance.setContext(this)
        val preference_userName = Preference.instance.loadPreference(getString(R.string.preference_username))

        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        if (preference_userName == null || preference_userName!!.isEmpty()) {
            layout_login_tool.visibility = View.GONE
            layout_login_username.visibility = View.VISIBLE
            isLastLogin = false
        } else {
            isLastLogin = true
            layout_login_tool.visibility = View.VISIBLE
            layout_login_username.visibility = View.GONE
        }//ngược lại


    }

    private fun login() {
        if (!CheckConnectInternet.isOnline(this)) {
            txt_login_validation!!.setText(R.string.validate_no_connect)
            txt_login_validation!!.visibility = View.VISIBLE
            return
        }
        txt_login_validation!!.visibility = View.GONE

        val userName: String?
        if (isLastLogin)
            userName = Preference.instance.loadPreference(getString(R.string.preference_username))
        else
            userName = txtUsername!!.text.toString().trim { it <= ' ' }
        val passWord = txtPassword!!.text.toString().trim { it <= ' ' }
        if (userName!!.length == 0 || passWord.length == 0) {
            handleInfoLoginEmpty()
            return
        }
        val loginAsycn = NewLoginAsycn(this, object : NewLoginAsycn.AsyncResponse {
            override fun processFinish(output: Void?) {
                if (mApplication!!.user != null)
                    handleLoginSuccess(mApplication!!.user!!)
                else
                    handleLoginFail()
            }
        })
        loginAsycn.execute(userName, passWord)
    }

    private fun handleInfoLoginEmpty() {
        txt_login_validation!!.setText(R.string.info_login_empty)
        txt_login_validation!!.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        txt_login_validation!!.setText(R.string.validate_login_fail)
        txt_login_validation!!.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(user: User) {


        Preference.instance.savePreferences(getString(R.string.preference_username), user.userName!!)
        Preference.instance.savePreferences(getString(R.string.preference_password), user.passWord!!)
        Preference.instance.savePreferences(getString(R.string.preference_displayname), this.mApplication!!.user!!.displayName!!)
        //        txtUsername.setText("");
        //        txtPassword.setText("");
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }

    private fun changeAccount() {
        txtUsername!!.setText("")
        txtPassword!!.setText("")

        Preference.instance.savePreferences(getString(R.string.preference_username), "")
        create()
    }

    override fun onPostResume() {
        super.onPostResume()
        create()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> login()
            R.id.txt_login_changeAccount -> changeAccount()
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (txtPassword!!.text.toString().trim { it <= ' ' }.length > 0) {
                    login()
                    return true
                }
                return super.onKeyUp(keyCode, event)
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}
