package mekong.ditagis.com.qlts.async

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.FeatureEditResult
import com.esri.arcgisruntime.data.ServiceFeatureTable
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
class UpdateAttachmentAsync constructor(activity: Activity, selectedArcGISFeature: ArcGISFeature, image: ByteArray) : AsyncTask<Void?, Void?, Void?>() {
    private val mDialog: ProgressDialog?
    private val mContext: Context
    private val mServiceFeatureTable: ServiceFeatureTable
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private val mImage: ByteArray
    private val mDApplication: DApplication
    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage(mContext.getString(R.string.async_dang_xu_ly))
        mDialog.setCancelable(false)
        mDialog.show()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        val attachmentName: String = String.format(Constant.AttachmentName.UPDATE,
                mDApplication.user!!.username, System.currentTimeMillis())
        val addResult: ListenableFuture<Attachment> = mSelectedArcGISFeature!!.addAttachmentAsync(mImage, Bitmap.CompressFormat.PNG.toString(), attachmentName)
        addResult.addDoneListener {
            if (mDialog != null && mDialog.isShowing) mDialog.dismiss()
            try {
                val attachment: Attachment = addResult.get()
                if (attachment.size > 0) {
                    val tableResult: ListenableFuture<Void> = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature)
                    tableResult.addDoneListener {
                        val updatedServerResult: ListenableFuture<List<FeatureEditResult>> = mServiceFeatureTable.applyEditsAsync()
                        updatedServerResult.addDoneListener {
                            var edits: List<FeatureEditResult>? = null
                            try {
                                edits = updatedServerResult.get()
                                if (edits.isNotEmpty()) {
                                    if (!edits[0].hasCompletedWithErrors()) {
                                        //attachmentList.add(fileName);
                                        val s: String = mSelectedArcGISFeature!!.getAttributes().get("objectid").toString()
                                        // update the attachment list view/ on the control panel
                                    } else {
                                    }
                                } else {
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            if (mDialog != null && mDialog.isShowing) {
                                mDialog.dismiss()
                            }
                        }
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return null
    }

    init {
        mContext = activity
        mDApplication = activity.application as DApplication
        mServiceFeatureTable = selectedArcGISFeature.featureTable as ServiceFeatureTable
        mSelectedArcGISFeature = selectedArcGISFeature
        mDialog = ProgressDialog(activity, android.R.style.Theme_Material_Dialog_Alert)
        mImage = image
    }
}