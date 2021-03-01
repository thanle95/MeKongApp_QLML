package vinhlong.ditagis.com.qlts.async

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi

import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureEditResult
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.MapView

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.ExecutionException

import vinhlong.ditagis.com.qlts.R
import vinhlong.ditagis.com.qlts.utities.Constant
import vinhlong.ditagis.com.qlts.utities.DAlertDialog
import vinhlong.ditagis.com.qlts.utities.DApplication

/**
 * Created by ThanLe on 4/16/2018.
 */

class SingleTapAddFeatureAsync(private val mActivity: Activity, private val mServiceFeatureTable: ServiceFeatureTable, private val mMapView: MapView, delegate: AsyncResponse) : AsyncTask<Point, Any, Void>() {
    private val mDialog: ProgressDialog?
    private val mDApplication: DApplication
    private val mSelectedArcGISFeature: ArcGISFeature? = null

    var mDelegate: AsyncResponse? = null

    private val dateString: String
        @RequiresApi(api = Build.VERSION_CODES.N)
        get() {
            val timeStamp = Constant.DATE_FORMAT.format(Calendar.getInstance().time)

            val writeDate = SimpleDateFormat("dd_MM_yyyy HH:mm:ss")
            writeDate.timeZone = TimeZone.getTimeZone("GMT+07:00")
            return writeDate.format(Calendar.getInstance().time)
        }

    private val timeID: String
        get() = Constant.DATE_FORMAT.format(Calendar.getInstance().time)

    init {
        this.mDApplication = mActivity.application as DApplication
        this.mDialog = ProgressDialog(mActivity, android.R.style.Theme_Material_Dialog_Alert)
        this.mDelegate = delegate
    }

    interface AsyncResponse {
        fun processFinish(feature: Feature?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage("Đang thêm đối tượng...")
        mDialog.setCancelable(false)
        mDialog.setButton("Hủy") { dialogInterface, i -> publishProgress(null) }
        mDialog.show()
    }

    override fun doInBackground(vararg params: Point): Void? {
        val clickPoint = params[0]
        val feature: Feature
        try {
            feature = mServiceFeatureTable.createFeature()
            feature.geometry = clickPoint
            val typeIdField = mServiceFeatureTable.typeIdField
            val fields = mServiceFeatureTable.fields
            if (fields.size > 0) {
                for (field in fields) {
                    if (field.name.toUpperCase() == mActivity.getString(R.string.NGAYTHEMMOI)) {
                        val currentTime = Calendar.getInstance()
                        feature.attributes[field.name] = currentTime
                    }
                    if (field.name.toUpperCase() == mActivity.getString(R.string.NGAYCAPNHAT)) {
                        val currentTime = Calendar.getInstance()
                        feature.attributes[field.name] = currentTime
                    }
                    if (field.name.toUpperCase() == mActivity.getString(R.string.NGUOICAPNHAT)) {
                        feature.attributes[field.name] = this.mDApplication.user!!.userName
                    }
                    if (field.name == typeIdField) {
                        val codedValueDomain = field.domain as CodedValueDomain
                        val codedValue = codedValueDomain.codedValues[0]
                        feature.attributes[typeIdField] = codedValue.code
                    }
                }
            }

            val mapViewResult = mServiceFeatureTable.addFeatureAsync(feature)
            mapViewResult.addDoneListener {
                val listListenableEditAsync = mServiceFeatureTable.applyEditsAsync()
                listListenableEditAsync.addDoneListener {
                    try {
                        val featureEditResults = listListenableEditAsync.get()
                        if (featureEditResults.size > 0) {
                            val objectId = featureEditResults[0].objectId
                            val queryParameters = QueryParameters()
                            val query = "OBJECTID = $objectId"
                            queryParameters.whereClause = query
                            val feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                            feature.addDoneListener {
                                try {
                                    val features = feature.get()
                                    val iterator = features.iterator()
                                    if (iterator.hasNext()) {
                                        val feature1 = iterator.next()
                                        if (feature1 != null)
                                            publishProgress(feature1)
                                        else
                                            publishProgress(false)
                                    } else
                                        publishProgress(false)
                                } catch (e: InterruptedException) {
                                    publishProgress(e.toString())
                                } catch (e: ExecutionException) {
                                    publishProgress(e.toString())
                                }
                            }
                        } else
                            publishProgress(false)
                    } catch (e: Exception) {
                        publishProgress(e.toString())
                    }
                }
            }

        } catch (e: Exception) {
            publishProgress(e.toString())
        }


        return null
    }


    override fun onProgressUpdate(vararg values: Any) {
        super.onProgressUpdate(*values)
        if (mDialog != null && mDialog.isShowing) {
            mDialog.dismiss()
        }
        if (values != null && values.size > 0) {
            if (values[0] is Feature) {
                DAlertDialog().show(mActivity, "Thông báo", "Thêm thành công!")
                mDelegate!!.processFinish(values[0] as Feature)
            } else if (values[0] is String) {
                DAlertDialog().show(mActivity, "Có lỗi xảy ra", values[0] as String)
                mDelegate!!.processFinish(null)
            } else {
                DAlertDialog().show(mActivity, "Có lỗi xảy ra")
                mDelegate!!.processFinish(null)
            }
        } else {
            DAlertDialog().show(mActivity, "Có lỗi xảy ra")
            mDelegate!!.processFinish(null)
        }
    }


}