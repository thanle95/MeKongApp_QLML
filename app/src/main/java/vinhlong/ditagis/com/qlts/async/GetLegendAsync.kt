package vinhlong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.layers.ArcGISSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.LegendInfo
import com.esri.arcgisruntime.symbology.Symbol

import java.util.ArrayList
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger

import vinhlong.ditagis.com.qlts.entities.LayerLegend
import vinhlong.ditagis.com.qlts.utities.DApplication


/**
 * Created by ThanLe on 4/16/2018.
 */

class GetLegendAsync(@field:SuppressLint("StaticFieldLeak")
                     private val mActivity: Activity, @field:SuppressLint("StaticFieldLeak")
                     private val mDelegate: AsyncResponse) : AsyncTask<Void, Void, Void>() {
    private val mApplication: DApplication

    interface AsyncResponse {
        fun processFinish()
    }

    init {
        this.mApplication = mActivity.application as DApplication
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg aVoids: Void): Void? {
        getLegend()
        return null
    }

    private fun getLegend() {
        val sizeLayer = mApplication.layerVisible.size
        val iLayer = AtomicInteger()
        for (`object` in mApplication.layerVisible.keys) {
            var listListenableFuture: ListenableFuture<List<LegendInfo>>? = null
            if (`object` is FeatureLayer) {
                listListenableFuture = `object`.fetchLegendInfosAsync()
            } else if (`object` is ArcGISSublayer) {
                listListenableFuture = `object`.fetchLegendInfosAsync()
            }
            if (listListenableFuture == null) continue
            val finalListListenableFuture = listListenableFuture
            listListenableFuture.addDoneListener {
                iLayer.incrementAndGet()

                val bitmaps = ArrayList<Bitmap>()
                try {
                    val legendInfos = finalListListenableFuture.get()
                    val size = legendInfos.size
                    val i = AtomicInteger()
                    for (legendInfo in legendInfos) {
                        val symbol = legendInfo.symbol
                        val swatchAsync = symbol.createSwatchAsync(mActivity.applicationContext, Color.TRANSPARENT)
                        swatchAsync.addDoneListener {
                            try {

                                val bitmap = swatchAsync.get()
                                if (bitmap != null)
                                    bitmaps.add(bitmap)


                                i.incrementAndGet()
                                if (i.get() == size) {
                                    mApplication.layerLegendList.add(LayerLegend(`object`, bitmaps))
                                    if (iLayer.get() == sizeLayer)
                                        publishProgress()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                publishProgress()
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    publishProgress()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    publishProgress()
                }
            }
        }
    }

    @SafeVarargs
    override fun onProgressUpdate(vararg values: Void) {
        mDelegate.processFinish()
    }


}