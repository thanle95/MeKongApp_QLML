package mekong.ditagis.com.qlts.async

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.databinding.LayoutProgressDialogBinding
import mekong.ditagis.com.qlts.entities.DAppInfo
import mekong.ditagis.com.qlts.entities.DLayerInfo
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class PreparingTask(private val delegate: Response) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mDialog: BottomSheetDialog

    interface Response {
        fun post(output: List<DLayerInfo>?)
    }

    fun execute(activity: Activity, application: DApplication) {
        preExecute(activity)
        executor.execute {
            getCapabilities(application)
            if (isAccess(application)) {
                getAppInfo(application)
                val layerInfos = getLayerInfo(application)
                handler.post {
                    postExecute()
                    delegate.post(layerInfos)
                }
            } else {
                handler.post {
                    postExecute()
                    delegate.post(null)
                }
            }
        }
    }

    private fun preExecute(activity: Activity) {
        mDialog = BottomSheetDialog(activity)
        val layout = LayoutProgressDialogBinding.inflate(activity.layoutInflater)
        layout.txtProgressDialogTitle.text = "Đang tải dữ liệu.."
        mDialog.setContentView(layout.root)
        mDialog.setCancelable(false)

        mDialog.show()
    }

    private fun postExecute() {
        if (mDialog.isShowing)
            mDialog.dismiss()
    }

    private fun isAccess(application: DApplication): Boolean {
        if (Constant.AppID.LIST.find { id -> application.user!!.capability == id } != null)
            return true
        return false
    }

    private fun getCapabilities(application: DApplication) {
        try {
            val url = URL(Constant.URL_API.CAPABILITIES)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + application.user!!.accessToken)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                application.user!!.capability = parseStringArray(builder.toString())!![0]
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }
    }

    private fun getAppInfo(application: DApplication) {
        try {
            val url = URL(Constant.URL_API.APP_INFO + application.user!!.capability)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + application.user!!.accessToken)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                application.appInfo = parseAppInfo(builder.toString())
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }
    }

    private fun getLayerInfo(application: DApplication): List<DLayerInfo>? {
        try {
            val API_URL = Constant.URL_API.LAYER_INFO
            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + application.user!!.accessToken)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                return parseLayerInfo(builder.toString())
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }
        return listOf()
    }

    @Throws(JSONException::class)
    private fun parseLayerInfo(data: String?): List<DLayerInfo>? {
        val outputType = object : TypeToken<List<DLayerInfo>>() {}.type
        val gson = Gson()
        val list: List<DLayerInfo> = gson.fromJson(data, outputType)
        return list
    }

    @Throws(JSONException::class)
    private fun parseAppInfo(data: String?): DAppInfo? {
        val outputType = object : TypeToken<DAppInfo>() {}.type
        val gson = Gson()
        val dAppInfo: DAppInfo = gson.fromJson(data, outputType)
        return dAppInfo
    }

    @Throws(JSONException::class)
    private fun parseStringArray(data: String?): Array<String>? {
        val outputType = object : TypeToken<Array<String>>() {}.type
        val gson = Gson()
        val array: Array<String> = gson.fromJson(data, outputType)
        return array
    }
}