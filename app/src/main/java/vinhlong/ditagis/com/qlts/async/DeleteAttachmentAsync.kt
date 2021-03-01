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


@SuppressLint("StaticFieldLeak")
class DeleteAttachmentAsync(private val mActivity: Activity, private val mFeature: ArcGISFeature,
                            asyncResponse: AsyncResponse) : AsyncTask<Attachment, Any, Void?>() {
    private val mApplication: DApplication = mActivity.application as DApplication
    private val mServiceFeatureTable = mFeature.featureTable as ServiceFeatureTable
    private var delegate: AsyncResponse? = null
    private val mUsername: String

    init {
        this.delegate = asyncResponse

        this.mUsername = Preference.instance.loadPreference(Constant.PreferenceKey.USERNAME)!!
    }

    interface AsyncResponse {
        fun processFinish(o: Any)
    }

    override fun doInBackground(vararg params: Attachment): Void? {
        if (params.isEmpty()) {
            publishProgress("Không có ảnh để xóa!")
            return null
        }
        val deleteAttachmentAsync = mFeature.deleteAttachmentAsync(params.first())
        deleteAttachmentAsync.addDoneListener {
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
                            publishProgress(false)
                        }
                    } else {
                        publishProgress(false)
                    }
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
