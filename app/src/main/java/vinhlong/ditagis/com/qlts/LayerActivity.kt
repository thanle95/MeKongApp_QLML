package vinhlong.ditagis.com.qlts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import kotlinx.android.synthetic.main.activity_layer.*
import kotlinx.android.synthetic.main.item_list_layer.view.*
import vinhlong.ditagis.com.qlts.utities.DApplication


class LayerActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layer)
        mApplication = application as DApplication

        val layout = llayout_activity_layer_
        layout.removeAllViews()

        try {
            for (`object` in mApplication!!.layerVisible.keys) {
                var name = ""
                if (`object` is ArcGISSublayer) {
                    name = `object`.name
                } else if (`object` is FeatureLayer) {
                    name = `object`.name
                }
                if (name.isEmpty())
                    continue
                val view = layoutInflater.inflate(R.layout.item_list_layer, null, false) as LinearLayout
                val layoutImage = view.llayout_item_list_layer_images
                for (layerLegend in mApplication!!.layerLegendList) {
                    if (layerLegend.`object` == `object`) {
                        for (bitmap in layerLegend.legendMap) {
                            val imageView = ImageView(layoutImage.context)
                            imageView.setImageBitmap(bitmap)
                            layoutImage.addView(imageView)
                        }
                    }
                }
                if (layoutImage.childCount == 0)
                    layoutImage.visibility = View.GONE
                val textView = view.txt_item_feature_
                textView.text = name
                layout.addView(view)
                //
            }
        } catch (e: Exception) {
            Log.e("Lỗi layerActivity", e.toString())
        }

    }

    private fun selectionSort(arr: Array<Layer>): Array<Layer> {
        val n = arr.size

        // One by one move boundary of unsorted subarray
        for (i in 0 until n - 1) {
            // Find the minimum element in unsorted array
            var min_idx = i

            for (j in i + 1 until n) {
                val urlCurrent = getURL(arr[j])
                if (urlCurrent.compareTo(getURL(arr[min_idx])) < 0) {
                    //nameCurrent less than nameMin
                    min_idx = j
                }
            }
            // Swap the found minimum element with the first
            // element
            val temp = arr[min_idx]
            arr[min_idx] = arr[i]
            arr[i] = temp
        }
        return arr
    }

    private fun getURL(layer: Layer): String {
        var url = ""
        if (layer is ArcGISMapImageLayer) {
            url = layer.mapServiceInfo.url
        } else if (layer is FeatureLayer) {
            url = (layer.featureTable as ServiceFeatureTable).uri
        }
        return url
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // todo: goto back activity from here
                goHome()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        goHome()
    }

    private fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
