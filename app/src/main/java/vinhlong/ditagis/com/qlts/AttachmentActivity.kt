package vinhlong.ditagis.com.qlts

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.darsh.multipleimageselect.activities.AlbumSelectActivity
import com.darsh.multipleimageselect.helpers.Constants
import com.darsh.multipleimageselect.models.Image
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_attachment.*
import org.apache.commons.io.IOUtils
import ru.whalemare.sheetmenu.SheetMenu
import ru.whalemare.sheetmenu.layout.LinearLayoutProvider
import vinhlong.ditagis.com.qlts.adapter.FeatureViewMoreInfoAttachmentsAdapter
import vinhlong.ditagis.com.qlts.async.AddAttachmentsAsync
import vinhlong.ditagis.com.qlts.async.DeleteAttachmentAsync
import vinhlong.ditagis.com.qlts.async.ViewAttachmentAsync
import vinhlong.ditagis.com.qlts.utities.Constant
import vinhlong.ditagis.com.qlts.utities.DAlertDialog
import vinhlong.ditagis.com.qlts.utities.DApplication
import vinhlong.ditagis.com.qlts.utities.DBitmap
import java.io.File

class AttachmentActivity : AppCompatActivity() {

    private lateinit var mApplication: DApplication
    private lateinit var mAdapter: FeatureViewMoreInfoAttachmentsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachment)

        try {
            shimmer_view_container.startShimmerAnimation()
            mApplication = application as DApplication
            val lstViewAttachment = lstView_alertdialog_attachments

            mAdapter = FeatureViewMoreInfoAttachmentsAdapter(this, mutableListOf())
            lstViewAttachment.adapter = mAdapter


            val viewAttachmentAsync = ViewAttachmentAsync(this, container_attachment, (mApplication.selectedFeature as ArcGISFeature?)!!, object : ViewAttachmentAsync.AsyncResponse {
                override fun processFinish(attachment: Attachment?) {
                    if (attachment == null) {
                        DAlertDialog().show(this@AttachmentActivity, "Thông báo", "Không có ảnh đính kèm!")
                        shimmer_view_container.stopShimmerAnimation()
                        shimmer_view_container.visibility = View.GONE
                        return
                    }
                    var item = FeatureViewMoreInfoAttachmentsAdapter.Item()

                    item.attachment = attachment
                    val inputStreamListenableFuture = item.attachment?.fetchDataAsync()
                    inputStreamListenableFuture?.addDoneListener {
                        try {
                            val inputStream = inputStreamListenableFuture.get()
                            val img = IOUtils.toByteArray(inputStream)
                            val bmp = DBitmap().getBitmap(img)
                            item.bitmap = bmp
                            mAdapter.add(item)
                            mAdapter.notifyDataSetChanged()
                            shimmer_view_container.stopShimmerAnimation()
                            shimmer_view_container.visibility = View.GONE
                        } catch (e: Exception) {
                            DAlertDialog().show(this@AttachmentActivity, e)
                        }
                    }

                }

            })
            viewAttachmentAsync.execute()
            lstViewAttachment.setOnItemClickListener { parent, view, position, id ->
                run {
                    val attachmentItem = mAdapter.getItem(position)
                    if (attachmentItem != null) {


                        SheetMenu(
                                context = this,
                                title = "",
                                menu = R.menu.attachment_item_click, // you can just pass menu resource if you need static items
                                layoutProvider = LinearLayoutProvider(), // linear layout enabled by default
                                onClick = { item ->
                                    run {
                                        when (item.id) {
                                            R.id.attachment_item_view -> {
                                                mApplication.selectedAttachment = attachmentItem.attachment
                                                mApplication.selectedBitmap = attachmentItem.bitmap
                                                val intent = Intent(this@AttachmentActivity, HandlerAttachmentActivity::class.java)
                                                this@AttachmentActivity.startActivity(intent)
                                            }
                                            R.id.attachment_item_delete -> {
                                                val builder = AlertDialog.Builder(this@AttachmentActivity)
                                                builder.setTitle("Xác nhận")
                                                        .setMessage("Bạn có chắc muốn xóa ảnh ${attachmentItem.attachment!!.name}?")
                                                        .setNegativeButton("Xóa") { dialog, which ->
                                                            mApplication.progressDialog.show(this, container_attachment, "Đang thêm ảnh...")
                                                            DeleteAttachmentAsync(this, mApplication?.selectedFeature!! as ArcGISFeature,
                                                                    object : DeleteAttachmentAsync.AsyncResponse {
                                                                        override fun processFinish(o: Any) {
                                                                            mApplication.bitmaps = null
                                                                            mApplication.progressDialog.dismiss()
                                                                            if (o is Boolean && o) {
                                                                                DAlertDialog().show(this@AttachmentActivity, "Thông báo", "Xóa ảnh thành công!")
                                                                                mAdapter.remove(attachmentItem)
                                                                                mAdapter.notifyDataSetChanged()
                                                                            } else {
                                                                                DAlertDialog().show(this@AttachmentActivity, "Thông báo", "Xóa ảnh thất bại!")

                                                                            }
                                                                        }
                                                                    }).execute(attachmentItem.attachment)
                                                        }
                                                        .setPositiveButton("Hủy") { dialog, which ->

                                                        }.setCancelable(true)
                                                val dialog = builder.create()
                                                dialog.show()
                                            }
                                            else -> {

                                            }
                                        }
                                    }
                                }
                        ).show(this)

                    } else {
                        Toast.makeText(this@AttachmentActivity, "Không có ảnh!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            DAlertDialog().show(this, e)
        }

    }

    // create an action bar button
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        menuInflater.inflate(R.menu.attachment, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == R.id.action_add) {
            SheetMenu(
                    context = this,
                    title = "Thêm ảnh",
                    menu = R.menu.selection_method_add_attachment, // you can just pass menu resource if you need static items
                    layoutProvider = LinearLayoutProvider(), // linear layout enabled by default
                    onClick = { item ->
                        run {
                            when (item.id) {
                                R.id.selection_method_capture -> {
                                    capture()
                                }
                                R.id.selection_method_pick -> {
                                    pick()
                                }
                                else -> {

                                }
                            }
                        }
                    }
            ).show(this)
            // do something here
        }
        return super.onOptionsItemSelected(item)
    }

    fun capture() {
        mApplication?.bitmaps = null
        val cameraIntent = Intent(this, CameraActivity::class.java)
        startActivityForResult(cameraIntent, Constant.Request.CAMERA)


    }

    fun pick() {
        mApplication?.bitmaps = null
        val intent = Intent(this, AlbumSelectActivity::class.java)
//set limit on number of images that can be selected, default is 10
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 3)
        startActivityForResult(intent, Constants.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {
                //The array list has the image paths of the selected images
                val images: ArrayList<Image> = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES)
                var bitmaps = arrayListOf<Bitmap>()
                images.forEach { image ->
                    run {
                        val imgFile = File(image.path);

                        if (imgFile.exists()) {

                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            bitmaps.add(DBitmap().getDecreaseSizeBitmap(myBitmap))

                        }
                    }
                }
                mApplication?.bitmaps = bitmaps
                try {
                        mApplication.progressDialog.show(this, container_attachment, "Đang thêm ảnh...")
                        AddAttachmentsAsync(this, bitmaps, mApplication?.selectedFeature!! as ArcGISFeature,
                                object : AddAttachmentsAsync.AsyncResponse {
                                    override fun processFinish(o: List<Attachment>) {
                                        addAttachmentsDone(o)
                                    }
                                }).execute()
                } catch (e: Exception) {
                    DAlertDialog().show(this, e)
                }
            }
            Constant.Request.CAMERA -> if (resultCode == Activity.RESULT_OK) {
//            }
                if (mApplication!!.bitmaps != null) {
                    try {
                            mApplication.progressDialog.show(this, container_attachment, "Đang thêm ảnh...")
                            AddAttachmentsAsync(this, mApplication.bitmaps!!, mApplication?.selectedFeature!! as ArcGISFeature,
                                    object : AddAttachmentsAsync.AsyncResponse {
                                        override fun processFinish(o: List<Attachment>) {
                                            addAttachmentsDone(o)
                                        }
                                    }).execute()
                    } catch (e: Exception) {
                        DAlertDialog().show(this, e)
                    }
                }

            } else {


                Snackbar.make(container_attachment, "Hủy chụp ảnh", Snackbar.LENGTH_LONG).show()
            }

        }
    }

    fun addAttachmentsDone(o: List<Attachment>) {
        mApplication.bitmaps = null
        mApplication.progressDialog.dismiss()
        if (o.isNotEmpty()) {
            o.forEach { attachment ->
                run {
                    var item = FeatureViewMoreInfoAttachmentsAdapter.Item()

                    item.attachment = attachment
                    val inputStreamListenableFuture = item.attachment?.fetchDataAsync()
                    inputStreamListenableFuture?.addDoneListener {
                        try {
                            val inputStream = inputStreamListenableFuture.get()
                            val img = IOUtils.toByteArray(inputStream)
                            val bmp = DBitmap().getBitmap(img)
                            item.bitmap = bmp
                            mAdapter.add(item)
                            mAdapter.notifyDataSetChanged()
                            shimmer_view_container.stopShimmerAnimation()
                            shimmer_view_container.visibility = View.GONE
                        } catch (e: Exception) {
                            DAlertDialog().show(this@AttachmentActivity, e)
                        }
                    }
                }
            }
            DAlertDialog().show(this@AttachmentActivity, "Thông báo", "Thêm ảnh thành công!")

        } else {
            DAlertDialog().show(this@AttachmentActivity, "Thông báo", "Thêm ảnh thất bại!")

        }
    }
}

