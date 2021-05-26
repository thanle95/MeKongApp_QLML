package mekong.ditagis.com.qlts.async

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by ThanLe on 4/16/2018.
 */
class AddAttachmentTask(private val delegate: Response) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mDialog: BottomSheetDialog
    interface Response {
        fun post(output: Boolean?)
    }
    fun execute(activity: Activity, image: ByteArray, application: DApplication){
        val feature = application.selectedFeature as ArcGISFeature
        if(feature == null) postExecute()
        val serviceFeatureTable = feature!!.featureTable as ServiceFeatureTable
        preExecute(activity)
        executor.execute {
            val attachmentName = String.format(Constant.AttachmentName.ADD, application.user?.username,
                    System.currentTimeMillis())
            val addResult = feature.addAttachmentAsync(image,
                    Constant.CompressFormat.TYPE_UPDATE.toString(), attachmentName)
            addResult.addDoneListener {
                try {
                    val attachment = addResult.get()
                    if (attachment.size > 0) {
                        val voidListenableFuture = serviceFeatureTable.updateFeatureAsync(feature)
                        voidListenableFuture.addDoneListener {
                            val applyEditsAsync = serviceFeatureTable.applyEditsAsync()
                            applyEditsAsync.addDoneListener {
                                try {
                                    val featureEditResults = applyEditsAsync.get()
                                    if (featureEditResults.size > 0) {
                                        if (!featureEditResults[0].hasCompletedWithErrors()) {
                                            postExecute(true)
                                        } else {
                                            postExecute()
                                        }
                                    } else {
                                        postExecute()
                                    }
                                } catch (e: Exception) {
                                    postExecute()
                                }


                            }


                        }
                    } else {
                        postExecute()
                    }

                } catch (e: Exception) {
                    postExecute()
                }
            }
        }
    }
    private fun preExecute(activity: Activity){
        mDialog = BottomSheetDialog(activity)
        val layoutView = activity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        layoutView.txtProgressDialogTitle.text = "Đang thêm ảnh..."
        mDialog!!.setContentView(layoutView)
        mDialog.setCancelable(false)

        mDialog.show()
    }
    private fun postExecute(vararg values: Boolean?){
        handler.post {
            if (mDialog.isShowing)
                mDialog.dismiss()
            if (values.isNotEmpty() && values[0]!!) {
                delegate.post(true)
            } else
                delegate.post(false)
        }
    }

}
