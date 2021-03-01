package vinhlong.ditagis.com.qlts.async

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlts.adapter.ObjectsAdapter
import vinhlong.ditagis.com.qlts.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException


class QuerySearchAsycn( private val mServiceFeatureTable: ServiceFeatureTable, private val mDelegate: AsyncResponse) : AsyncTask<String, List<ObjectsAdapter.Item>, Void>() {

    interface AsyncResponse {
        fun processFinish(output: List<ObjectsAdapter.Item>?)
    }

    override fun doInBackground(vararg params: String): Void? {
        try {
            if (params != null && params.size > 0) {
                val items = ArrayList<ObjectsAdapter.Item>()
                val searchStr = params[0]
                val queryParameters = QueryParameters()
                val builder = StringBuilder()
                for (field in mServiceFeatureTable.fields) {
                    when (field.fieldType) {
                        Field.Type.OID, Field.Type.INTEGER, Field.Type.SHORT -> try {
                            val search = Integer.parseInt(searchStr)
                            builder.append(String.format("%s = %s", field.name, search))
                            builder.append(" or ")
                        } catch (e: Exception) {

                        }

                        Field.Type.FLOAT, Field.Type.DOUBLE -> try {
                            val search = java.lang.Double.parseDouble(searchStr)
                            builder.append(String.format("%s = %s", field.name, search))
                            builder.append(" or ")
                        } catch (e: Exception) {

                        }

                        Field.Type.TEXT -> {
                            builder.append(field.name + " like N'%" + searchStr + "%'")
                            builder.append(" or ")
                        }
                    }
                }
                builder.append(" 1 = 2 ")
                queryParameters.whereClause = builder.toString()
                queryParameters.isReturnGeometry = false
                val feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters)
                feature.addDoneListener {
                    try {
                        val result = feature.get()
                        val iterator = result.iterator()
                        if (iterator.hasNext()) {
                            while (iterator.hasNext()) {
                                val item = iterator.next() as Feature
                                val attributes = item.attributes
                                val objectid = attributes[Constant.OBJECTID].toString()
                                items.add(ObjectsAdapter.Item(objectid, objectid, ""))

                                //                        queryByObjectID(Integer.parseInt(attributes.get(Constant.OBJECTID).toString()));
                            }
                            publishProgress(items)
                        } else
                            publishProgress()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        publishProgress()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                        publishProgress()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Lỗi tim kiem", e.toString())
            publishProgress()
        }

        return null
    }

    override fun onProgressUpdate(vararg values: List<ObjectsAdapter.Item>) {
        super.onProgressUpdate(*values)

        if (values != null && values.isNotEmpty())
            mDelegate.processFinish(values[0])
        else
            mDelegate.processFinish(null)
    }

}
