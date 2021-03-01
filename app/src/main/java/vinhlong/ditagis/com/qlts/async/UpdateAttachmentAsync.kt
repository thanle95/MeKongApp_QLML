package vinhlong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlts.utities.Constant
import vinhlong.ditagis.com.qlts.utities.DApplication
import vinhlong.ditagis.com.qlts.utities.Preference
import java.util.*


@SuppressLint("StaticFieldLeak")
class UpdateAttachmentAsync(private val mActivity: Activity, private val mFeature: ArcGISFeature,
                            private val mAttachment: Attachment,
                            private val mByteArray: ByteArray,
                            asyncResponse: AsyncResponse) : AsyncTask<Void?, Any, Void?>() {
    private val mApplication: DApplication = mActivity.application as DApplication
private val mServiceFeatureTable = mFeature.featureTable as ServiceFeatureTable
    private var delegate: AsyncResponse? = null
    private val mThoiGian = Calendar.getInstance()
    private val mUsername: String
    init {
        this.delegate = asyncResponse

        this.mUsername = Preference.instance.loadPreference(Constant.PreferenceKey.USERNAME)!!
    }

    interface AsyncResponse {
        fun processFinish(o: Any)
    }

    override fun onPreExecute() {
        super.onPreExecute()

    }

    override fun doInBackground(vararg params: Void?): Void? {
        val attachmentName = String.format(Constant.AttachmentName.UPDATE,
                mUsername, System.currentTimeMillis())
        mFeature.updateAttachmentAsync(mAttachment, mByteArray, Constant.CompressFormat.TYPE_UPDATE.toString(), attachmentName)
                .addDoneListener {
                    mServiceFeatureTable?.updateFeatureAsync(mFeature)?.addDoneListener { applyServerEdits() }
                }
        return null
    }

    private fun applyServerEdits() {

        try {
            // check that the feature table was successfully updated
            // apply edits to the server
            val updatedServerResult = mServiceFeatureTable?.applyEditsAsync()
            updatedServerResult?.addDoneListener {
                try {
                    val edits = updatedServerResult.get()
                    if (edits.size > 0) {
                        if (!edits[0].hasCompletedWithErrors()) {
                            publishProgress(true)
                            //attachmentList.add(fileName);
                            } else {
                          publishProgress(false) }
                    } else {
                  publishProgress(false) }
                } catch (e: Exception) {
                    publishProgress(e)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    override fun onProgressUpdate(vararg values: Any) {
        super.onProgressUpdate(*values)
        mApplication.progressDialog?.dismiss()
        delegate!!.processFinish(values[0])
        mApplication.bitmaps = null
    }


}
