package vinhlong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.AsyncTask
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlts.utities.Constant
import vinhlong.ditagis.com.qlts.utities.DApplication
import vinhlong.ditagis.com.qlts.utities.DBitmap
import vinhlong.ditagis.com.qlts.utities.Preference


@SuppressLint("StaticFieldLeak")
class AddAttachmentsAsync(private val mActivity: Activity, private val mBitmaps: ArrayList<Bitmap>, private val mFeature: ArcGISFeature,
                          asyncResponse: AsyncResponse) : AsyncTask<Bitmap, List<Attachment>, Void?>() {
    private val mApplication: DApplication = mActivity.application as DApplication
    private val mServiceFeatureTable = mFeature.featureTable as ServiceFeatureTable
    private var delegate: AsyncResponse? = null
    private val mUsername: String

    init {
        this.delegate = asyncResponse

        this.mUsername = Preference.instance.loadPreference(Constant.PreferenceKey.USERNAME)!!
    }

    interface AsyncResponse {
        fun processFinish(o: List<Attachment>)
    }

    override fun doInBackground(vararg params: Bitmap): Void? {
        var attachments = arrayListOf<Attachment>()
        mBitmaps.forEach { bitmap ->
            run {
                val attachmentName = String.format(Constant.AttachmentName.UPDATE,
                        mUsername, System.currentTimeMillis())
                val addAttachmentAsync = mFeature.addAttachmentAsync(DBitmap().getByteArray(bitmap), Constant.CompressFormat.TYPE_UPDATE.toString(), attachmentName)
                addAttachmentAsync.addDoneListener {
                    attachments.add(addAttachmentAsync.get())
                    if (attachments.size == mBitmaps.size)
                        mServiceFeatureTable?.updateFeatureAsync(mFeature)?.addDoneListener { applyServerEdits(attachments) }
                }
            }
        }

        return null
    }

    private fun applyServerEdits(attachments: List<Attachment>) {

        try {
            // check that the feature table was successfully updated
            // apply edits to the server
            val updatedServerResult = mServiceFeatureTable?.applyEditsAsync()
            updatedServerResult?.addDoneListener {
                try {
                    val edits = updatedServerResult.get()
                    if (edits.size > 0) {
                        if (!edits[0].hasCompletedWithErrors()) {
                            publishProgress(attachments)
                            //attachmentList.add(fileName);
                        } else {
                            publishProgress(arrayListOf())
                        }
                    } else {
                        publishProgress(arrayListOf())
                    }
                } catch (e: Exception) {
                    publishProgress(arrayListOf())
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onProgressUpdate(vararg values:  List<Attachment>) {
        super.onProgressUpdate(*values)
        mApplication.progressDialog?.dismiss()
        delegate!!.processFinish(values.first())
        mApplication.bitmaps = null
    }


}
