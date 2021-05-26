package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */
@SuppressLint("StaticFieldLeak")
class DeleteAttachmentAsync(private val mActivity: Activity, selectedArcGISFeature: ArcGISFeature, private val mAttachment: Attachment, private val mDelegate: AsyncResponse) : AsyncTask<Void, Boolean, Void>() {
  private var mDialog: BottomSheetDialog? = null
  private val mServiceFeatureTable: ServiceFeatureTable
  private var mSelectedArcGISFeature: ArcGISFeature? = null

  interface AsyncResponse {
    fun processFinish(success: Boolean?)
  }

  init {
    mServiceFeatureTable = selectedArcGISFeature.featureTable as ServiceFeatureTable
    mSelectedArcGISFeature = selectedArcGISFeature

  }

  @SuppressLint("SetTextI18n")
  override fun onPreExecute() {
    super.onPreExecute()
    mDialog = BottomSheetDialog(this.mActivity)
    val layoutView = mActivity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
    layoutView.txtProgressDialogTitle.text = "Đang xóa ảnh..."
    mDialog!!.setContentView(layoutView)
    mDialog!!.setCancelable(false)

    mDialog!!.show()
  }

  override fun doInBackground(vararg params: Void): Void? {

    val voidListenableFuture = mSelectedArcGISFeature!!.deleteAttachmentAsync(mAttachment)
    voidListenableFuture.addDoneListener {
      try {

        val voidListenableFuture1 = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature!!)
        voidListenableFuture1.addDoneListener {
          val applyEditsAsync = mServiceFeatureTable.applyEditsAsync()
          applyEditsAsync.addDoneListener {
            try {
              val featureEditResults = applyEditsAsync.get()
              if (featureEditResults.size > 0) {
                if (!featureEditResults[0].hasCompletedWithErrors()) {
                  publishProgress(true)
                } else {
                  publishProgress()
                }
              } else {
                publishProgress()
              }
            } catch (e: InterruptedException) {
              e.printStackTrace()
              publishProgress()
            } catch (e: ExecutionException) {
              e.printStackTrace()
              publishProgress()
            }


          }


        }


      } catch (e: Exception) {
        publishProgress()
      }
    }
    return null
  }

  override fun onProgressUpdate(vararg values: Boolean?) {
    super.onProgressUpdate(*values)
    if (mDialog != null && mDialog!!.isShowing) {
      mDialog!!.dismiss()
    }
    if (values.isNotEmpty() && values[0]!!) {
      mDelegate.processFinish(true)
    } else
      mDelegate.processFinish(false)

  }


}
