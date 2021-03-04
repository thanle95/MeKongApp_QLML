package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.os.AsyncTask
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.MapView
import mekong.ditagis.com.qlts.libs.FeatureLayerDTG
import mekong.ditagis.com.qlts.utities.Popup
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */

class SingleTapMapViewAsync( private val mFeatureLayerDTGs: List<FeatureLayerDTG>,
                            private val mPopUp: Popup,
                            private val mMapView: MapView,private val mDelegate: AsyncResponse) : AsyncTask<android.graphics.Point, FeatureLayerDTG, Void>() {
    private var featureLayer: FeatureLayer? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var mClickPoint: android.graphics.Point? = null

    interface AsyncResponse {
        fun processFinish(output: Void?)
    }

    val featureLayerDTG: FeatureLayerDTG?
        get() {
            val id = featureLayer!!.id
            for (featureLayerDTG in mFeatureLayerDTGs) {
                val idLayer = featureLayerDTG.featureLayer.id
                if (idLayer == id) return featureLayerDTG
            }
            return null
        }



    fun setFeatureLayer(featureLayer: FeatureLayer) {
        this.featureLayer = featureLayer
    }

    @SuppressLint("WrongThread")
    override fun doInBackground(vararg params: android.graphics.Point): Void? {
        mClickPoint = params[0]
        val identifyLayerResultListenableFuture = mMapView.identifyLayerAsync(featureLayer, mClickPoint!!, 5.0, false, 1)
        identifyLayerResultListenableFuture.addDoneListener {
            try {
                val identifyLayerResult = identifyLayerResultListenableFuture.get()
                val elements = identifyLayerResult.elements
                if (elements.size > 0) {
                    if (elements[0] is ArcGISFeature) {
                        mSelectedArcGISFeature = elements[0] as ArcGISFeature
                        val featureLayerDTG = featureLayerDTG
                        publishProgress(featureLayerDTG)
                    }
                }

                publishProgress(null)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return null

    }

    override fun onProgressUpdate(vararg values: FeatureLayerDTG) {
        super.onProgressUpdate(*values)
        if (values.isNotEmpty()) {
            val featureLayerDTG = values[0]
            if (featureLayerDTG != null) {
                mPopUp.setFeatureLayerDTG(featureLayerDTG)
                if (mSelectedArcGISFeature != null) mPopUp.showPopup(mSelectedArcGISFeature!!, true)
            }

        }
        this.mDelegate.processFinish(null)
    }


}