package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.*


/**
 * Created by ThanLe on 4/16/2018.
 */

class GetFeatureLegendAsync(@field:SuppressLint("StaticFieldLeak")
                            private val mActivity: Activity, @field:SuppressLint("StaticFieldLeak")
                            private val mDelegate: AsyncResponse) : AsyncTask<FeatureLayer, Any, Void?>() {
    private val mApplication: DApplication = mActivity.application as DApplication

    interface AsyncResponse {
        fun processFinish(o: Any)
    }

    override fun doInBackground(vararg layers: FeatureLayer): Void? {
        if (layers.isNotEmpty())
            getLegend(layers.first())
        return null
    }

    private fun getLegend(layer: Layer) {
        if (layer is FeatureLayer) {
            val listenableFuture = layer.fetchLegendInfosAsync()
            listenableFuture.addDoneListener {
                try {
                    val legendInfos = listenableFuture.get()
                    val renderSymbols = HashMap<Bitmap, String>()
                    for (legendInfo in legendInfos) {
                        val symbol = legendInfo.symbol
                        val name = legendInfo.name
                        val swatchAsync = symbol.createSwatchAsync(mActivity.applicationContext, Color.TRANSPARENT)
                        swatchAsync.addDoneListener {
                            try {
                                val bitmap = swatchAsync.get()
                                if (bitmap != null) {
                                    renderSymbols[bitmap] = name
                                    if (renderSymbols.size == legendInfos.size) {
                                        publishProgress(renderSymbols)
                                    }
                                }
                            } catch (e: Exception) {
                                publishProgress(e)
                            }
                        }

                    }
                } catch (e: Exception) {
                    publishProgress(e)
                }
            }

        }
    }

    @SafeVarargs
    override fun onProgressUpdate(vararg values: Any) {
        if (values.isNotEmpty())
            mDelegate.processFinish(values.first())
        else mDelegate.processFinish("Không tải được chú thích")
    }


}