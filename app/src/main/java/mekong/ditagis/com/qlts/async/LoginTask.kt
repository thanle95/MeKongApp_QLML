package mekong.ditagis.com.qlts.async

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.entities.User
import mekong.ditagis.com.qlts.utities.Constant
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LoginTask(private val delegate: Response) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mDialog: BottomSheetDialog

    interface Response {
        fun post(user: User?)
    }

    fun execute(activity: Activity, username: String, password: String) {
        preExecute(activity)
        executor.execute {
            val user = getUser(username, password)
            handler.post {
                postExecute()
                delegate.post(user)
            }
        }
    }

    private fun preExecute(activity: Activity) {
        mDialog = BottomSheetDialog(activity)
        val layoutView = activity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        layoutView.txtProgressDialogTitle.text = "Đang đăng nhập..."
        mDialog!!.setContentView(layoutView)
        mDialog.setCancelable(false)

        mDialog.show()
    }

    private fun postExecute() {
        if (mDialog.isShowing)
            mDialog.dismiss()
    }

    private fun getUser(username: String, password: String): User? {
        var conn: HttpURLConnection? = null
        try {
            val API_URL = Constant.URL_API.LOGIN
            val url = URL(API_URL)
            conn = url.openConnection() as HttpURLConnection
            conn!!.doOutput = true
            conn.instanceFollowRedirects = false
            conn.requestMethod = Constant.HTTPRequest.POST_METHOD
            val cred = JSONObject()
            cred.put("username", username)
            cred.put("password", password)
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.useCaches = false
            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(cred.toString())
            wr.flush()
            conn.connect()
            val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (true) {
                line = bufferedReader.readLine()
                if (line == null)
                    break
                stringBuilder.append(line)

            }

            bufferedReader.close()
            conn.disconnect()
            return parseUser(stringBuilder.toString())
        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }

    private fun parseUser(data: String?): User {
        val userType = object : TypeToken<User>() {}.type
        return Gson().fromJson(data, userType)
    }

}
