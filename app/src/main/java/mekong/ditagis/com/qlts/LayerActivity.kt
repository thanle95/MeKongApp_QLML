package mekong.ditagis.com.qlts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import mekong.ditagis.com.qlts.databinding.ActivityLayerBinding
import mekong.ditagis.com.qlts.databinding.ItemListLayerBinding
import mekong.ditagis.com.qlts.utities.DApplication


class LayerActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null
    private lateinit var mBinding: ActivityLayerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLayerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mApplication = application as DApplication

        mBinding.llayoutActivityLayer.removeAllViews()

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
                val bindingView = ItemListLayerBinding.inflate(layoutInflater)
                val layoutImage = bindingView.llayoutItemListLayerImages
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
                val textView = bindingView.txtItemFeature
                textView.text = name
                mBinding.llayoutActivityLayer.addView(bindingView.root)
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
