package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R

/**
 * Created by ThanLe on 4/16/2018.
 */

class FetchAttachmentAsync(@field:SuppressLint("StaticFieldLeak") private val mActivity: Activity, private val mTitle: String, selectedArcGISFeature: ArcGISFeature, private val mDelegate: AsyncResponse) : AsyncTask<Void, List<Attachment>, Void>() {
    private var mDialog: BottomSheetDialog? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var builder: AlertDialog.Builder? = null

    init {
        mSelectedArcGISFeature = selectedArcGISFeature
    }

    interface AsyncResponse {
        fun processFinish(attachments: List<Attachment>?)
    }

    override fun onPreExecute() {
        super.onPreExecute()


        mDialog = BottomSheetDialog(this.mActivity)
        val layoutView = mActivity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        layoutView.txtProgressDialogTitle.text = mTitle
        mDialog!!.setContentView(layoutView)
        mDialog!!.setCancelable(false)

        mDialog!!.show()
    }

    override fun doInBackground(vararg params: Void): Void? {
        builder = AlertDialog.Builder(mActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
//        LayoutInflater.from(mActivity)

        val attachmentResults = mSelectedArcGISFeature!!.fetchAttachmentsAsync()
        attachmentResults.addDoneListener {
            try {

                val attachments = attachmentResults.get()
//                intArrayOf(attachments.size)
//                ArrayList<DAttachment>()
                // if selected feature has attachments, display them in a list fashion
                if (attachments.isNotEmpty()) {
                    publishProgress(attachments)

                } else {
                    publishProgress()
                    //                        MySnackBar.make(mCallout, "Không có file hình ảnh đính kèm", true);
                }

            } catch (e: Exception) {
                Log.e("ERROR", e.toString())
                publishProgress()
            }
        }
        return null
    }


    override fun onProgressUpdate(vararg values: List<Attachment>?) {
        if (values != null && values.isNotEmpty())
            mDelegate.processFinish(values[0])
        else mDelegate.processFinish(null)
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.dismiss()
        }

        super.onProgressUpdate(*values)

    }


}

