package mekong.ditagis.com.qlts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_attachment.*
import kotlinx.android.synthetic.main.layout_handle_add_attachment.view.*
import mekong.ditagis.com.qlts.adapter.AttachmentAdapter
import mekong.ditagis.com.qlts.async.AddAttachmentTask
import mekong.ditagis.com.qlts.async.DeleteAttachmentAsync
import mekong.ditagis.com.qlts.async.FetchAttachmentAsync
import mekong.ditagis.com.qlts.async.FetchDataAsync
import mekong.ditagis.com.qlts.entities.DAttachment
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DAlertDialog
import mekong.ditagis.com.qlts.utities.DApplication
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class AttachmentActivity : AppCompatActivity() {
    private lateinit var mAdapter: AttachmentAdapter
    private lateinit var mLayoutDialog: LinearLayout
    private lateinit var mApplication: DApplication
    private lateinit var mDialog: BottomSheetDialog
    private var mSelectedItem: DAttachment? = null
    private var mAttachments: List<Attachment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment)

        mApplication = application as DApplication
        initDialog()

        mAdapter = AttachmentAdapter(this, ArrayList())
        initGridView()
    }

    private fun initGridView() {
        gridAddAttachment.adapter = mAdapter

        gridAddAttachment.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ -> this.onGridViewItemClick(parent, position) }


        reload()
    }

    private fun reload() {
        mAdapter.clear()
        //tải ảnh của feature đã chọn
        FetchAttachmentAsync(this@AttachmentActivity, "Đang tải hình ảnh đính kèm",
                mApplication.selectedFeature!! as ArcGISFeature, object : FetchAttachmentAsync.AsyncResponse {
            override fun processFinish(attachments: List<Attachment>?) {
                mAttachments = attachments
                var hasImage = false
                if (attachments != null && attachments.isNotEmpty()) {

                    for (attachment in attachments) {
                        if (attachment.contentType.toLowerCase().trim { it <= ' ' }.contains(Constant.CompressFormat.JPEG.toString().toLowerCase())
                                || attachment.contentType.toLowerCase().trim { it <= ' ' }.contains(Constant.CompressFormat.PNG.toString().toLowerCase())) {
                            hasImage = true
                        }
                    }
                    if (hasImage) {

                        FetchDataAsync(this@AttachmentActivity, object : FetchDataAsync.AsyncResponse {
                            override fun processFinish(dAttachment: DAttachment?) {
                                if (dAttachment != null) {
//                                for (itemAdapter in mAdapter.getItems()) {

//                                    if (dAttachment.name.contains(Constant.APP_NAME)) {
//                                        if (itemAdapter.image != null) {
                                    //một mục có nhiều ảnh
                                    mAdapter.getItems() += DAttachment(dAttachment.attachment, dAttachment.image, dAttachment.name, dAttachment.title)
//                                        } else {
//                                            itemAdapter.image = dAttachment.image
//                                            itemAdapter.attachment = dAttachment.attachment
//                                        }
//                                        break
//                                    }
//                                }
                                    mAdapter.sort { o1, o2 -> o1.name.compareTo(o2.name) }
                                    mAdapter.notifyDataSetChanged()
                                }
                            }
                        }).execute(attachments)
                    }
                }
                if(!hasImage){
                    mApplication.alertDialog.show(this@AttachmentActivity, "Thông báo", "Chưa có hình ảnh!")

                    //neu khong co hinh anh nao thi them 1 item cho adapter
                    mAdapter.add(DAttachment(null, null, "", "Chưa có hình ảnh"))
                    mAdapter.notifyDataSetChanged()
                }
            }
        }).execute()
    }

    private fun initDialog() {
        mDialog = BottomSheetDialog(this@AttachmentActivity)
        mLayoutDialog = layoutInflater.inflate(R.layout.layout_handle_add_attachment, null) as LinearLayout
        mDialog.setContentView(mLayoutDialog)


    }

    private fun viewPhoto() {
        mApplication.selectedAttachment = mSelectedItem!!.attachment
        mApplication.selectedBitmap = mSelectedItem!!.image
        val intent = Intent(this@AttachmentActivity, ViewImageActivity::class.java)
        this@AttachmentActivity.startActivity(intent)

    }

    private fun capture() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), Constant.RequestCode.PERMISSION)

        } else {
            mApplication.bitmaps = null
            val cameraIntent = Intent(this@AttachmentActivity, CameraActivity::class.java)
            this@AttachmentActivity.startActivityForResult(cameraIntent, Constant.RequestCode.CAMERA)
        }
    }

    private fun onGridViewItemClick(parent: AdapterView<*>, position: Int) {
        //show dialog lựa chọn

        mSelectedItem = parent.getItemAtPosition(position) as DAttachment
        mDialog.show()
        mLayoutDialog.llayoutHandleImageView.setOnClickListener { this.onLayoutDialogItemClick(it) }
        mLayoutDialog.llayoutHandleImageCapture.setOnClickListener { this.onLayoutDialogItemClick(it) }
        mLayoutDialog.llayoutHandleImagePick.setOnClickListener { this.onLayoutDialogItemClick(it) }
        mLayoutDialog.llayoutHandleImageDelete.setOnClickListener { this.onLayoutDialogItemClick(it) }
    }

    private fun onLayoutDialogItemClick(v: View) {
        mSelectedItem?.let { _selectedItem ->
            mDialog.dismiss()
            when (v.id) {
                R.id.llayoutHandleImageView -> _selectedItem.image?.let { viewPhoto() }
                        ?: run {
                            Toast.makeText(this@AttachmentActivity, "Không có hình ảnh!", Toast.LENGTH_SHORT).show()
                        }

                R.id.llayoutHandleImageCapture ->
                    capture()
                R.id.llayoutHandleImageDelete ->
                    if (_selectedItem.image != null && _selectedItem.attachment != null) {

                        showDialogConfirmDelete()
                    }

                else -> {
                    Toast.makeText(this@AttachmentActivity, "Không có hình ảnh!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDialogConfirmUpdate(isCapture: Boolean) {
        mSelectedItem?.let {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@AttachmentActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
            builder.setTitle("Xác nhận")
            builder.setMessage(String.format("Bạn có chắc muốn cập nhật ảnh %s", it.name))
            builder.setNegativeButton("Cập nhật") { _, _ ->
                if (isCapture)
                    capture()
            }.setPositiveButton("Hủy") { dialog, _ -> dialog.dismiss() }.setCancelable(false)
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
    }

    private fun showDialogConfirmDelete() {
        mSelectedItem?.let {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this@AttachmentActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
            builder.setTitle("Xác nhận")
            builder.setMessage(String.format("Bạn có chắc muốn xoá ảnh %s", it.name))
            builder.setNegativeButton("Xoá") { _, _ ->
                DeleteAttachmentAsync(this@AttachmentActivity, mApplication.selectedFeature!! as ArcGISFeature,
                        it.attachment!!, object : DeleteAttachmentAsync.AsyncResponse {
                    override fun processFinish(success: Boolean?) {
                        success?.let {
                            reload()

                        } ?: run {
                            val snackbar = Snackbar.make(gridAddAttachment, "Không xóa được ảnh!", 2000)
                            snackbar.show()
                        }
                    }
                }).execute()
            }.setPositiveButton("Hủy") { dialog, _ -> dialog.dismiss() }.setCancelable(false)
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
    }

    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Constant.CompressFormat.TYPE_COMPRESS, 100, outputStream)
        val image = outputStream.toByteArray()
        try {
            outputStream.close()
        } catch (e: IOException) {

        }

        return image
    }

    private fun addOrUpdateWithBitmap(bitmap: Bitmap?) {
        bitmap?.let { bitmap ->
            //Kiểm tra nếu mSelectedItem đã có hình ảnh thì cập nhật attachment đó
//            mSelectedItem?.let { it ->
            val response = object : AddAttachmentTask.Response {
                override fun post(success: Boolean?) {
                    success?.let {
                        if (it) {
                            Toast.makeText(gridAddAttachment.context, "Thêm ảnh thành công", Toast.LENGTH_SHORT).show()
                            reload()
                        } else {
                            val snackBar = Snackbar.make(gridAddAttachment, "Không thêm được ảnh!", 2000)
                            snackBar.show()
                        }
                    } ?: run {
                        val snackBar = Snackbar.make(gridAddAttachment, "Không thêm được ảnh!", 2000)
                        //                                    snackbar.setAction("Thử lại", new View.OnClickListener() {
                        //                                        @Override
                        //                                        public void onClick(View v) {
                        //
                        //                                        }
                        //                                    });
                        snackBar.show()
                    }
                }
            }


            AddAttachmentTask(response).execute(this@AttachmentActivity,
                    getByteArrayFromBitmap(bitmap), mApplication)
//            }

            // }

//            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.RequestCode.CAMERA ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val bitmaps = mApplication.bitmaps
                        try {
                            if (bitmaps != null && bitmaps.isNotEmpty()) {
                                addOrUpdateWithBitmap(bitmaps.first())
                            } else {
                                DAlertDialog().show(this@AttachmentActivity, "Thông báo", Constant.Message.UNDEFINED)
                            }
                        } catch (e: Exception) {
                            DAlertDialog().show(this, e)
                        }

                    }
                    Activity.RESULT_CANCELED -> Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT)
                    else -> Toast.makeText(this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT)
                }


        }
    }

}
