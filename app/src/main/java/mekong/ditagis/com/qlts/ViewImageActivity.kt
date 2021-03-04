package mekong.ditagis.com.qlts

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import mekong.ditagis.com.qlts.databinding.ActivityViewImageBinding
import mekong.ditagis.com.qlts.utities.DApplication


class ViewImageActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication
    private lateinit var mBinding: ActivityViewImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityViewImageBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mApplication = application as DApplication
        var bitmap = mApplication.selectedBitmap
        title = mApplication.selectedAttachment!!.name
        mBinding.imgViewAttachment.setImageBitmap(bitmap)
        mBinding.btnRorateLeft.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(-90F)
                val bitmap = (mBinding.imgViewAttachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                mBinding.imgViewAttachment.setImageBitmap(rotatedBitmap)
            }
        }
        mBinding.btnRorateRight.setOnClickListener {
            runOnUiThread {
                val matrix = Matrix()

                matrix.postRotate(90F)
                val bitmap = (mBinding.imgViewAttachment.drawable as BitmapDrawable).bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, true)

                val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
                mBinding.imgViewAttachment.setImageBitmap(rotatedBitmap)
            }
        }


    }
}
