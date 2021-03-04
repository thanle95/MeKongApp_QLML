package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout
import com.esri.arcgisruntime.data.Attachment
import mekong.ditagis.com.qlts.databinding.LayoutProgressDialogBinding
import mekong.ditagis.com.qlts.entities.DAttachment
import mekong.ditagis.com.qlts.utities.Constant

/**
 * Created by ThanLe on 4/16/2018.
 */

class FetchDataAsync(@field:SuppressLint("StaticFieldLeak") private val mActivity: Activity, private val mDelegate: AsyncResponse) : AsyncTask<List<Attachment>, DAttachment, Void>() {
    private var mDialog: BottomSheetDialog? = null
    private var builder: AlertDialog.Builder? = null
    private var mSize = 0

    interface AsyncResponse {
        fun processFinish(dAttachment: DAttachment?)
    }

    @SuppressLint("SetTextI18n")
    override fun onPreExecute() {
        super.onPreExecute()


        mDialog = BottomSheetDialog(this.mActivity)
        val bindingView = LayoutProgressDialogBinding.inflate(mActivity.layoutInflater)
        bindingView.txtProgressDialogTitle.text = "Đang tải hình ảnh..."
        mDialog!!.setContentView(bindingView.root)
        mDialog!!.setCancelable(false)

        mDialog!!.show()
    }

    @SuppressLint("DefaultLocale")
    override fun doInBackground(vararg params: List<Attachment>): Void? {
        if (params.isEmpty() || params[0] == null) {
            publishProgress()
            return null
        }
        builder = AlertDialog.Builder(mActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val attachments = params[0]
        mSize = attachments.size

        for (attachment in attachments) {
            if (attachment.contentType.toLowerCase().trim { it <= ' ' }.contains(Constant.CompressFormat.JPEG.toString().toLowerCase())
                    || attachment.contentType.toLowerCase().trim { it <= ' ' }.contains(Constant.CompressFormat.PNG.toString().toLowerCase())) {
                val item = DAttachment(attachment.name, "")
                val inputStreamListenableFuture = attachment.fetchDataAsync()
                inputStreamListenableFuture.addDoneListener {
                    try {
                        val inputStream = inputStreamListenableFuture.get()

                        //Kiểm tra nếu adapter có phần tử và attachment là phần tử cuối cùng thì show dialog
                        //                                           item.setImage(IOUtils.toByteArray(inputStream));
                        var item = DAttachment(attachment, BitmapFactory.decodeStream(inputStream), attachment.name, attachment.name)
                        publishProgress(item)
                    } catch (e: Exception) {
                        publishProgress()
                    }
                }

            } else {
                publishProgress()
            }
        }

        return null
    }


    override fun onProgressUpdate(vararg values: DAttachment?) {
        if (values.isNotEmpty())
            mDelegate.processFinish(values[0])
        else mDelegate.processFinish(null)
        mSize--

        if (mSize == 0 && mDialog != null && mDialog!!.isShowing) {
            mDialog!!.dismiss()
        }

        super.onProgressUpdate(*values)

    }


}

