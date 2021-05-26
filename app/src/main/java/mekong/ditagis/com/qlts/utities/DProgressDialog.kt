package mekong.ditagis.com.qlts.utities

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import mekong.ditagis.com.qlts.R

class DProgressDialog {
    private var mDialog: Dialog? = null
    private var mView: View? = null

    fun show(activity: Activity, view: ViewGroup, title: String, isCancelable: Boolean = false) {
        mDialog = BottomSheetDialog(activity)
        mView = activity.layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        mView!!.txtProgressDialogTitle.text = title

        mDialog!!.setCancelable(isCancelable)
        mDialog!!.setContentView(mView!!)


        if (mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
        mDialog!!.show()
    }

    fun changeTitle(activity: Activity, view: ViewGroup, title: String) {
        if (mDialog != null && mDialog!!.isShowing) {
            mView!!.txtProgressDialogTitle.text = title
        } else {
            show(activity, view, title)
        }
    }

    fun dismiss() {
        if (mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
    }
}
