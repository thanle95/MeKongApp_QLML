package vinhlong.ditagis.com.qlts

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_show_capture.*
import vinhlong.ditagis.com.qlts.utities.DApplication

class ShowCaptureActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_show_capture)
        mApplication = application as DApplication
        show_capture_imageView.setImageBitmap(mApplication!!.bitmaps!!.first())
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.show_capture_cancel -> goHomeCancel()
            R.id.show_capture_ok -> goHome()
        }
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
}
