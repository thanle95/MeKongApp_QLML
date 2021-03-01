package vinhlong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import vinhlong.ditagis.com.qlts.utities.Constant
import vinhlong.ditagis.com.qlts.utities.DApplication

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class ViewAttachmentAsync(private val mActivity: Activity, private val mRootView: ViewGroup, selectedArcGISFeature: ArcGISFeature, private val mDelegate: AsyncResponse) :
        AsyncTask<Void, Attachment, Void>() {
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var builder: AlertDialog.Builder? = null
    private var layout: View? = null
    private val mApplication: DApplication = mActivity.application as DApplication
    private var mSize = 0

    init {
        mSelectedArcGISFeature = selectedArcGISFeature
    }

    interface AsyncResponse {
        fun processFinish(item: Attachment?)
    }

    @SuppressLint("WrongThread")
    override fun doInBackground(vararg params: Void): Void? {

        val attachmentResults = mSelectedArcGISFeature!!.fetchAttachmentsAsync()
        attachmentResults.addDoneListener {
            try {

                val attachments = attachmentResults.get()
                mSize = attachments.size
                // if selected feature has attachments, display them in a list fashion
                if (!attachments.isEmpty()) {
                    //
                    for (attachment in attachments) {
                        if (attachment.contentType.contains(
                                        Constant.CompressFormat.JPEG.toString().toLowerCase())
                                || attachment.contentType.contains(Constant.CompressFormat.PNG.toString().toLowerCase())) {
                            publishProgress(attachment)


                        } else {
                            publishProgress()
                        }
                    }

                } else {
                    publishProgress()
                    //                        MySnackBar.make(mCallout, "Không có file hình ảnh đính kèm", true);
                }

            } catch (e: Exception) {
                publishProgress()
                Log.e("ERROR", e.message)
            }
        }
        return null
    }


    override fun onProgressUpdate(vararg values: Attachment) {
        super.onProgressUpdate(*values)
        if (values.isNotEmpty()) mDelegate.processFinish(values[0])
        else mDelegate.processFinish(null)

    }

}

