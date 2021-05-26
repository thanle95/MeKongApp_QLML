package mekong.ditagis.com.qlts

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_camera.*
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DAlertDialog
import mekong.ditagis.com.qlts.utities.DApplication
import mekong.ditagis.com.qlts.utities.DBitmap
import java.io.IOException


class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var mCamera: Camera? = null
    private var mPictureCallback: Camera.PictureCallback? = null
    private var mParameters: Camera.Parameters? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private lateinit var mApplication: DApplication
    private var mOrientation: Int = 0
    private  lateinit var  mOrientationListener: OrientationEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_camera)

        mSurfaceHolder = surfaceViewFragmentCamera.holder
        mSurfaceHolder!!.addCallback(this)
        mApplication = application as DApplication
        val DEBUG_TAG = "orientationp-----"
        mOrientationListener = object : OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            override fun onOrientationChanged(orientation: Int) {
                mOrientation = orientation
                Log.v(DEBUG_TAG,
                        "Orientation changed to $orientation")
            }
        }

        if (mOrientationListener.canDetectOrientation() === true) {
            Log.v(DEBUG_TAG, "Can detect orientation")
            mOrientationListener.enable()
        } else {
            Log.v(DEBUG_TAG, "Cannot detect orientation")
            mOrientationListener.disable()
        }
    }
    private fun getParameters() {
        //todo: dùng camera2 cho sdk >=21
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
        } else {

        }
        try {
            mCamera = Camera.open()

            mParameters = mCamera!!.parameters
            mCamera!!.setDisplayOrientation(90)
            mParameters!!.previewFrameRate = 30
            mParameters!!.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

            var bestSize : Camera.Size? = null
            var sizeList = mCamera!!.getParameters().getSupportedJpegThumbnailSizes()
            bestSize = sizeList.get(0)
            for ( size in sizeList) {
                if ((size.width * size.height) > (bestSize!!.width * bestSize.height)) {
                    bestSize = size;
                }
            }
        } catch (e: Exception) {
            DAlertDialog().show(this, e)
        }
    }

    @SuppressLint("StaticFieldLeak")

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        try {
            getParameters()
            mCamera!!.parameters = mParameters
            mSurfaceHolder = surfaceHolder
            mCamera!!.setPreviewDisplay(surfaceHolder)
            mCamera!!.startPreview()

            mPictureCallback = Camera.PictureCallback { bytes, camera ->

                val bitmap =   DBitmap().getBitmap(bytes)
                var orientation = mOrientation
                val div = orientation / 90
                val mod = orientation % 90
                if (mod < 45){
                    orientation = div * 90
                }
                else{
                    orientation = (div + 1) * 90
                }
                val resultBitmap = rotateBitmap(bitmap, (orientation  - 270 ) .toFloat())

                mApplication.bitmaps = arrayListOf(resultBitmap)
                mOrientationListener.disable()
                val intent = Intent(this@CameraActivity, ShowCaptureActivity::class.java)
                this@CameraActivity.startActivityForResult(intent, Constant.RequestCode.SHOW_CAPTURE)


            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {

        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun captureImage() {
        mCamera!!.takePicture(null, null, mPictureCallback)
    }

    @Throws(IOException::class)
    private fun turnOnOffFlashCamera() {
        //auto
        if (mCamera!!.parameters.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
            cameraFlash.setImageResource(R.drawable.ic_flash_auto)
            mCamera = Camera.open()
            getParameters()
            mParameters!!.flashMode = Camera.Parameters.FLASH_MODE_AUTO
            mCamera!!.parameters = mParameters
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
            mCamera!!.startPreview()
        } else if (mCamera!!.parameters.flashMode == Camera.Parameters.FLASH_MODE_AUTO) {
            cameraFlash.setImageResource(R.drawable.ic_flash_on)
            mCamera = Camera.open()
            getParameters()
            mParameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            mCamera!!.parameters = mParameters
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
            mCamera!!.startPreview()
        } else if (mCamera!!.parameters.flashMode == Camera.Parameters.FLASH_MODE_TORCH) {
            cameraFlash.setImageResource(R.drawable.ic_flash_off)
            mCamera = Camera.open()
            getParameters()
            mParameters!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = mParameters
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
            mCamera!!.startPreview()
        }//turn off
        //turn on
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {

    }

    override fun onBackPressed() {
        goHomeCancel()
    }


    private fun goHome() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun goHomeCancel() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)

    }


    fun onClick(view: View) {
        when (view.id) {
            R.id.cameraFlash -> try {
                turnOnOffFlashCamera()
            } catch (e: Exception) {
                Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            }

            R.id.cameraBack -> goHomeCancel()
            R.id.cameraCapture -> captureImage()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.RequestCode.SHOW_CAPTURE -> if (resultCode == RESULT_OK)
                goHome()
        }
    }
}