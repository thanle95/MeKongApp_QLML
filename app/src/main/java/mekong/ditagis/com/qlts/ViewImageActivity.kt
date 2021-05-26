package mekong.ditagis.com.qlts

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_view_image.*
import mekong.ditagis.com.qlts.utities.DApplication


class ViewImageActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        mApplication = application as DApplication
        var bitmap = mApplication.selectedBitmap
        title = mApplication.selectedAttachment!!.name
        imgViewAttachment.setImageBitmap(bitmap)
        btnRorateLeft.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(-90F)
                val bitmap = (imgViewAttachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                imgViewAttachment.setImageBitmap(rotatedBitmap)
            }
        }
        btnRorateRight.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(90F)
                val bitmap = (imgViewAttachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                imgViewAttachment.setImageBitmap(rotatedBitmap)
            }
        }


    }
}
