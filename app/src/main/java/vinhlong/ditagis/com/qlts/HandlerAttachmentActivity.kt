package vinhlong.ditagis.com.qlts

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ArcGISFeature
import kotlinx.android.synthetic.main.activity_handler_attachment.*
import vinhlong.ditagis.com.qlts.async.UpdateAttachmentAsync
import vinhlong.ditagis.com.qlts.utities.DAlertDialog
import vinhlong.ditagis.com.qlts.utities.DApplication
import vinhlong.ditagis.com.qlts.utities.DBitmap

class HandlerAttachmentActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handler_attachment)

        mApplication = application as DApplication
        var bitmap = mApplication.selectedBitmap
        title = mApplication.selectedAttachment!!.name
        img_view_attachment.setImageBitmap(bitmap)


        btn_rorate_left.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(-90F)
                val bitmap = (img_view_attachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                img_view_attachment.setImageBitmap(rotatedBitmap)
            }
        }
        btn_rorate_right.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(90F)
                val bitmap = (img_view_attachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                img_view_attachment.setImageBitmap(rotatedBitmap)
            }
        }
        btn_update.setOnClickListener{
            mApplication.progressDialog?.show(this, layout_handler_attachment, "Đang cập nhật hình ảnh...")
            UpdateAttachmentAsync(this, mApplication.selectedFeature!! as ArcGISFeature, mApplication.selectedAttachment!!,

                    DBitmap().getByteArray((img_view_attachment.drawable as BitmapDrawable).bitmap), object : UpdateAttachmentAsync.AsyncResponse {
                override fun processFinish(o: Any) {
                    mApplication.progressDialog.dismiss()

                    if (o is Boolean) {
                        if (o) {
                            DAlertDialog().show(this@HandlerAttachmentActivity, "Thông báo", "Cập nhật thành công")
                        } else {
                            DAlertDialog().show(this@HandlerAttachmentActivity, "Thông báo", "Cập nhật thất bại")
                        }
                    } else if (o is Exception) {
                        DAlertDialog().show(this@HandlerAttachmentActivity, o)
                    }
                }

            }).execute()

        }

    }
}
