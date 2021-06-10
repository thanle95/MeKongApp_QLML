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
import mekong.ditagis.com.qlts.entities.DAppInfo
import mekong.ditagis.com.qlts.entities.DDongHoKhachHang
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


class ThongTinDongHoTask(private val delegate: Response) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mDialog: BottomSheetDialog

    interface Response {
        fun post(output: List<DDongHoKhachHang>?)
    }

    fun execute(activity: Activity, application: DApplication, objectID: Long) {
        preExecute(activity)
        executor.execute {
            val infos =getInfo(application, objectID)
            handler.post {
                postExecute()
                delegate.post(infos)
            }
        }
    }

    private fun preExecute(activity: Activity) {
        mDialog = BottomSheetDialog(activity)
        val layoutView = activity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        layoutView.txtProgressDialogTitle.text = "Đang tải dữ liệu..."
        mDialog.setContentView(layoutView)
        mDialog.setCancelable(false)

        mDialog.show()
    }

    private fun postExecute() {
        if (mDialog.isShowing)
            mDialog.dismiss()
    }


    private fun getInfo(application: DApplication, objectID: Long): List<DDongHoKhachHang> {
        try {
            val url = URL(Constant.URL_API.THONG_TIN_DONG_HO + objectID)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                return parseStringArray(builder.toString())!!
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
    private fun parseStringArray(data: String?): List<DDongHoKhachHang>? {
        val outputType = object : TypeToken<List<DDongHoKhachHang>>() {}.type
        val gson = Gson()
        val array: List<DDongHoKhachHang> = gson.fromJson(data, outputType)


        return array
    }
}