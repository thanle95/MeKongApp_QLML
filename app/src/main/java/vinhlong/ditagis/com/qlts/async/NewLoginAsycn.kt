package vinhlong.ditagis.com.qlts.async

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import vinhlong.ditagis.com.qlts.R
import vinhlong.ditagis.com.qlts.entities.entitiesDB.User
import vinhlong.ditagis.com.qlts.utities.Constant
import vinhlong.ditagis.com.qlts.utities.DApplication
import vinhlong.ditagis.com.qlts.utities.Preference

class NewLoginAsycn(private val mActivity: Activity, private val mDelegate: AsyncResponse) : AsyncTask<String, Void, Void?>() {
    private val exception: Exception? = null
    private var mDialog: ProgressDialog? = null
    private val mDApplication: DApplication

    private val displayName: String
        get() {
            val API_URL = Constant.URL_API.PROFILE
            var displayName = ""
            try {
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.doOutput = false
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Authorization", Preference.instance.loadPreference(mActivity.getString(R.string.preference_login_api)))
                    conn.connect()

                    val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                    val line = bufferedReader.readLine()

                        displayName = pajsonRouteeJSon(line)


                } catch (e: Exception) {
                    Log.e("error", e.toString())
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                return displayName
            }
        }

    interface AsyncResponse {
        fun processFinish(output: Void?)
    }

    init {
        this.mDApplication = mActivity.application as DApplication
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mActivity, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mActivity.getString(R.string.connect_message))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: String): Void? {
        val userName = params[0]
        val pin = params[1]
        var conn: HttpURLConnection? = null
        try {
            val API_URL = Constant.URL_API.LOGIN
            val url = URL(API_URL)
            conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.instanceFollowRedirects = false
            conn.requestMethod = Constant.HTTPRequest.POST_METHOD

            val cred = JSONObject()
            cred.put("Username", userName)
            cred.put("Password", pin)


            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.useCaches = false
            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(cred.toString())
            wr.flush()

            conn.connect()

            val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
            val builder = StringBuilder()
            var line: String? = null
            while (true) {
                line = bufferedReader.readLine()
                if(line == null)
                    break
                builder.append(line)
            }
            Preference.instance.savePreferences(mActivity.getString(R.string.preference_login_api), builder.toString().replace("\"", ""))
            val user = User()
            user.displayName = displayName
            user.userName = userName
            user.passWord = pin
            user.token = builder.toString().replace("\"", "")
            this.mDApplication.user = user


        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }

    override fun onPostExecute(user: Void?) {
        //        if (user != null) {
        mDialog!!.dismiss()
        //        }
        this.mDelegate.processFinish(null)
    }

    @Throws(JSONException::class)
    private fun pajsonRouteeJSon(data: String?): String {
        if (data == null)
            return ""
        var displayName = ""
        val myData = "{ \"account\": [$data]}"
        val jsonData = JSONObject(myData)
        val jsonRoutes = jsonData.getJSONArray("account")
        //        jsonData.getJSONArray("account");
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)
            displayName = jsonRoute.getString(mActivity.getString(R.string.sql_coloumn_login_displayname))
        }
        return displayName

    }
}
