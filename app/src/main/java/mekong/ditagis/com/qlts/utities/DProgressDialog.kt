package mekong.ditagis.com.qlts.utities

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import mekong.ditagis.com.qlts.databinding.LayoutProgressDialogBinding

class DProgressDialog {
    private var mDialog: Dialog? = null
    private var mBinding: LayoutProgressDialogBinding? = null

    fun show(activity: Activity, view: ViewGroup, title: String, isCancelable: Boolean = false) {
        mDialog = BottomSheetDialog(activity)
        mBinding = LayoutProgressDialogBinding.inflate(activity.layoutInflater)
        mBinding!!.txtProgressDialogTitle.text = title

        mDialog!!.setCancelable(isCancelable)
        mDialog!!.setContentView(mBinding!!.root)


        if (mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
        mDialog!!.show()
    }

    fun changeTitle(activity: Activity, view: ViewGroup, title: String) {
        if (mDialog != null && mDialog!!.isShowing) {
            mBinding!!.txtProgressDialogTitle.text = title
        } else {
            show(activity, view, title)
        }
    }

    fun dismiss() {
        if (mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
    }
}
