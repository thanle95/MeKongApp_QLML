package vinhlong.ditagis.com.qlts.utities

import android.content.Intent
import android.net.Uri
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import kotlinx.android.synthetic.main.activity_quan_ly_tai_san.*
import kotlinx.android.synthetic.main.content.*
import kotlinx.android.synthetic.main.date_time_picker.view.*
import kotlinx.android.synthetic.main.layout_dialog_update_feature_listview.view.*
import kotlinx.android.synthetic.main.layout_popup_infos.view.*
import kotlinx.android.synthetic.main.layout_viewmoreinfo_feature.view.*
import vinhlong.ditagis.com.qlts.AttachmentActivity
import vinhlong.ditagis.com.qlts.MainActivity
import vinhlong.ditagis.com.qlts.R
import vinhlong.ditagis.com.qlts.adapter.FeatureViewInfoAdapter
import vinhlong.ditagis.com.qlts.adapter.FeatureViewMoreInfoAdapter
import vinhlong.ditagis.com.qlts.async.EditAsync
import vinhlong.ditagis.com.qlts.async.QueryHanhChinhAsync
import vinhlong.ditagis.com.qlts.libs.FeatureLayerDTG
import java.util.*


class Popup(private val mMainActivity: MainActivity, private val mMapView: MapView, private val mCallout: Callout?) : AppCompatActivity() {
    private var mServiceFeatureTable: ServiceFeatureTable? = null
    private var mFeatureLayerDTG: FeatureLayerDTG? = null
    private var lstUniqueValues: MutableList<String>? = null
    private var fieldNameDrawInfo: String? = null
    private var linearLayout: LinearLayout? = null
    private var mUri: Uri? = null
    private var mFeatureViewMoreInfoAdapter: FeatureViewMoreInfoAdapter? = null
    private var quanhuyen_features: ArrayList<Feature>? = null
    private var quanhuyen_feature: Feature? = null
    private val mApplication: DApplication

    private val isDeleteFeature: Boolean
        get() {
            var isPast1DateOfCurrentDate = false
            var isSameUser = false
            val attr = mApplication.selectedFeature!!.attributes
            val fields = mApplication.selectedFeature!!.featureTable.fields
            for (field in fields) {
                if (field.name.toUpperCase() == this.mMainActivity.resources.getString(R.string.NGAYTHEMMOI)) {
                    val ngayThemMoi = attr[field.name]
                    if (ngayThemMoi != null) {
                        val calendar = ngayThemMoi as Calendar?
                        val currentDate = Calendar.getInstance()
                        isPast1DateOfCurrentDate = isPast1DateOfCurrentDate(calendar, currentDate)
                    }
                }
                if (field.name.toUpperCase() == this.mMainActivity.resources.getString(R.string.NGUOICAPNHAT)) {
                    val nguoiCapNhat = attr[field.name]
                    if (nguoiCapNhat != null) {
                        isSameUser = nguoiCapNhat.toString() == this.mApplication.user!!.userName
                    }
                }
            }
            return isPast1DateOfCurrentDate && isSameUser
        }

    init {
        this.mApplication = mMainActivity.application as DApplication
    }


    fun setmSFTHanhChinh(mSFTHanhChinh: ServiceFeatureTable) {
        mApplication.progressDialog.changeTitle(mMainActivity, mMainActivity.main_activity_drawer_layout, "Đang lấy dữ liệu hành chính...")
        QueryHanhChinhAsync( mSFTHanhChinh, object : QueryHanhChinhAsync.AsyncResponse {
            override fun processFinish(output: ArrayList<Feature>?) {
                mApplication.progressDialog.dismiss()
                quanhuyen_features = output
            }
        }).execute()
    }

    fun setFeatureLayerDTG(layerDTG: FeatureLayerDTG) {
        this.mFeatureLayerDTG = layerDTG
    }

    fun refreshPopup() {
        val hiddenFields = mMainActivity.resources.getStringArray(R.array.hiddenFields)
        val attributes = mApplication.selectedFeature!!.attributes
        val listView = linearLayout!!.lstview_thongtinsuco
        val featureViewInfoAdapter = FeatureViewInfoAdapter(mMainActivity, ArrayList())
        listView.adapter = featureViewInfoAdapter

        val renderer = (mApplication.selectedFeature!!.featureTable as ServiceFeatureTable).layerInfo.drawingInfo.renderer
        var uniqueValueRenderer: UniqueValueRenderer? = null
        if (renderer is UniqueValueRenderer) {
            uniqueValueRenderer = renderer
            fieldNameDrawInfo = uniqueValueRenderer.fieldNames[0]

        }
        var checkHiddenField: Boolean
        val idHanhChinh = attributes[mMainActivity.getString(R.string.MAXA)]
        if (idHanhChinh != null) {
            getHanhChinhFeature(idHanhChinh.toString())
        }
        for (field in this.mApplication.selectedFeature!!.featureTable.fields) {
            checkHiddenField = false
            for (hiddenField in hiddenFields) {
                if (hiddenField == field.name) {
                    checkHiddenField = true
                    break
                }
            }
            val value = attributes[field.name]
            if (value != null && !checkHiddenField) {
                val item = FeatureViewInfoAdapter.Item()
                item.alias = field.alias
                item.fieldName = field.name
                if (item.fieldName!!.toUpperCase() == mMainActivity.getString(R.string.MAXA)) {
                    if (quanhuyen_feature != null)
                        item.value = quanhuyen_feature!!.attributes[mMainActivity.getString(R.string.TENHANHCHINH)].toString()
                    else
                        item.value = value.toString()
                } else if (item.fieldName!!.toUpperCase() == mMainActivity.getString(R.string.MAHUYEN)) {
                    if (quanhuyen_feature != null)
                        item.value = quanhuyen_feature!!.attributes[mMainActivity.getString(R.string.TENHUYEN)].toString()
                    else {
                        item.value = value.toString()
                    }
                } else if (item.fieldName == fieldNameDrawInfo) {
                    val uniqueValues = uniqueValueRenderer!!.uniqueValues
                    if (uniqueValues.size > 0) {
                        val valueFeatureType = getLabelUniqueRenderer(uniqueValues, value.toString())
                        if (valueFeatureType != null) item.value = valueFeatureType.toString()
                    } else
                        item.value = value.toString()

                } else if (field.domain != null) {
                    val codedValues = (this.mApplication.selectedFeature!!.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                    val valueDomainObject = getValueDomain(codedValues, value.toString())
                    if (valueDomainObject != null) item.value = valueDomainObject.toString()
                } else
                    when (field.fieldType) {
                        Field.Type.DATE -> item.value = Constant.DATE_FORMAT.format((value as Calendar).time)
                        else -> item.value = value.toString()
                    }

                featureViewInfoAdapter.add(item)
                featureViewInfoAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun viewMoreInfo() {
        val attr = mApplication.selectedFeature!!.attributes
        val builder = AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val layout = mMainActivity.layoutInflater.inflate(R.layout.layout_viewmoreinfo_feature, null)
        mFeatureViewMoreInfoAdapter = FeatureViewMoreInfoAdapter(mMainActivity, ArrayList())
        val lstViewInfo = layout.lstView_alertdialog_info
        lstViewInfo.adapter = mFeatureViewMoreInfoAdapter
        lstViewInfo.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> edit(parent, view, position, id) }

        val updateFields = mFeatureLayerDTG!!.updateFields
        val unedit_Fields = mMainActivity.resources.getStringArray(R.array.unedit_Fields)
        val hiddenFields = mMainActivity.resources.getStringArray(R.array.hiddenFields)
        val renderer = (mApplication.selectedFeature!!.featureTable as ServiceFeatureTable).layerInfo.drawingInfo.renderer
        var uniqueValueRenderer: UniqueValueRenderer? = null
        if (renderer is UniqueValueRenderer) {
            uniqueValueRenderer = renderer
        }
        var checkHiddenField: Boolean
        val idHanhChinh = attr[mMainActivity.getString(R.string.MAXA)]
        if (idHanhChinh != null) {
            getHanhChinhFeature(idHanhChinh.toString())
        }
        for (field in this.mApplication.selectedFeature!!.featureTable.fields) {
            checkHiddenField = false
            for (hiddenField in hiddenFields) {
                if (field.name.toUpperCase() == hiddenField.toUpperCase()) {
                    checkHiddenField = true
                    break
                }
            }
            if (checkHiddenField) continue
            val value = attr[field.name]
            if (field.name == Constant.IDSU_CO) {
                if (value != null)
                    layout.txt_alertdialog_id_su_co.text = value.toString()
            } else {
                val item = FeatureViewMoreInfoAdapter.Item()
                item.alias = field.alias
                item.fieldName = field.name
                if (updateFields!!.size > 0) {
                    if (updateFields[0] == "*" || updateFields[0] == "" || updateFields[0] === "null") {
                        item.isEdit = true
                    } else {
                        for (updateField in updateFields) {
                            if (item.fieldName == updateField) {
                                item.isEdit = true
                                break
                            }
                        }
                    }
                }
                for (unedit_Field in unedit_Fields) {
                    if (unedit_Field.toUpperCase() == item.fieldName!!.toUpperCase()) {
                        item.isEdit = false
                        break
                    }
                }
                if (value != null) {
                    if (item.fieldName == fieldNameDrawInfo) {
                        val uniqueValues = uniqueValueRenderer!!.uniqueValues
                        if (uniqueValues.size > 0) {
                            val valueFeatureType = getLabelUniqueRenderer(uniqueValues, value.toString())
                            if (valueFeatureType != null)
                                item.value = valueFeatureType.toString()
                        } else
                            item.value = value.toString()

                    } else if (item.fieldName!!.toUpperCase() == mMainActivity.getString(R.string.MAXA)) {
                        if (quanhuyen_feature != null)
                            item.value = quanhuyen_feature!!.attributes[mMainActivity.getString(R.string.TENHANHCHINH)].toString()
                        else
                            item.value = value.toString()
                    } else if (item.fieldName!!.toUpperCase() == mMainActivity.getString(R.string.MAHUYEN)) {
                        if (quanhuyen_feature != null)
                            item.value = quanhuyen_feature!!.attributes[mMainActivity.getString(R.string.TENHUYEN)].toString()
                        else
                            item.value = value.toString()
                    } else if (field.domain != null) {
                        val codedValues = (this.mApplication.selectedFeature!!.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                        val valueDomainObject = getValueDomain(codedValues, value.toString())
                        if (valueDomainObject != null) item.value = valueDomainObject.toString()
                    } else
                        when (field.fieldType) {
                            Field.Type.DATE -> item.value = Constant.DATE_FORMAT.format((value as Calendar).time)
                            Field.Type.OID, Field.Type.TEXT -> item.value = value.toString()
                            Field.Type.DOUBLE, Field.Type.INTEGER, Field.Type.SHORT -> item.value = value.toString()
                        }
                }


                item.fieldType = field.fieldType
                mFeatureViewMoreInfoAdapter!!.add(item)
                mFeatureViewMoreInfoAdapter!!.notifyDataSetChanged()
            }
        }

        builder.setView(layout)
        builder.setCancelable(false)
        builder.setPositiveButton("Thoát") { dialog, which -> }
        builder.setNegativeButton("Cập nhật") { dialog, which -> }
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            buttonPositive.setOnClickListener { dialog.dismiss() }
            button.setOnClickListener {
                if ((mApplication.selectedFeature!! as ArcGISFeature).canUpdateGeometry()) {
                    EditAsync(mMainActivity, mServiceFeatureTable!!, (mApplication.selectedFeature as ArcGISFeature?)!!, object :
                            EditAsync.AsyncResponse {
                        override fun processFinish(isSuccess: Boolean) {
                            if (isSuccess) {
                                dialog.dismiss()
                                refreshPopup()
                                DAlertDialog().show(mMainActivity, "Thông báo", "Cập nhật thành công")
                            } else {
                                DAlertDialog().show(mMainActivity, "Thông báo", "Cập nhật thất bại")
                            }
                        }
                    }).execute(mFeatureViewMoreInfoAdapter)
                } else
                    Toast.makeText(mMainActivity, "Không được quyền chỉnh sửa dữ liệu!!!", Toast.LENGTH_LONG).show()
            }
        }
        dialog.show()


    }


    private fun getValueDomain(codedValues: List<CodedValue>, code: String): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code.toString() == code) {
                value = codedValue.name
                break
            }

        }
        return value
    }

    private fun getValueFeatureType(featureTypes: List<FeatureType>, code: String): Any? {
        var value: Any? = null
        for (featureType in featureTypes) {
            if (featureType.id != null && featureType.id.toString() == code) {
                value = featureType.name
                break
            }
        }
        return value
    }

    private fun getLabelUniqueRenderer(uniqueValues: List<UniqueValueRenderer.UniqueValue>, code: String): Any? {
        var value: Any? = null
        for (uniqueValue in uniqueValues) {
            if (uniqueValue.values != null && uniqueValue.values[0].toString() == code) {
                value = uniqueValue.label
                break
            }
        }
        return value
    }

    private fun edit(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent.getItemAtPosition(position) is FeatureViewMoreInfoAdapter.Item) {
            val item = parent.getItemAtPosition(position) as FeatureViewMoreInfoAdapter.Item
            if (item.isEdit) {
                val layout = mMainActivity.layoutInflater.inflate(R.layout.layout_dialog_update_feature_listview, null) as LinearLayout

                val layoutTextView = layout.layout_edit_viewmoreinfo_TextView
                val textView = layout.txt_edit_viewmoreinfo
                val txtNotifyInCorrect = layout.txtNotifyInCorrect
                val img_selectTime = layout.img_selectLayer
                val layoutEditText = layout.layout_edit_viewmoreinfo_Editext
                val editText = layout.etxt_edit_viewmoreinfo
                val layoutSpin = layout.layout_edit_viewmoreinfo_Spinner
                val spin = layout.spin_edit_viewmoreinfo

                val domain = mApplication.selectedFeature!!.featureTable.getField(item.fieldName!!).domain
                if (item.fieldName == fieldNameDrawInfo) {
                    if (lstUniqueValues!!.size > 0) {
                        layoutSpin.visibility = View.VISIBLE
                        val adapter = ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, lstUniqueValues!!)
                        spin.adapter = adapter
                        if (item.value != null)
                            spin.setSelection(lstUniqueValues!!.indexOf(item.value!!))
                    } else {
                        layoutEditText.visibility = View.VISIBLE
                        editText.inputType = InputType.TYPE_CLASS_NUMBER
                        editText.setText(item.value)
                    }
                } else if (domain != null) {
                    layoutSpin.visibility = View.VISIBLE
                    val codedValues = (domain as CodedValueDomain).codedValues
                    if (codedValues != null) {
                        val codes = ArrayList<String>()
                        for (codedValue in codedValues)
                            codes.add(codedValue.name)
                        val adapter = ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, codes)
                        spin.adapter = adapter
                        if (item.value != null)
                            spin.setSelection(codes.indexOf(item.value!!))

                    }
                } else
                    when (item.fieldType) {
                        Field.Type.DATE -> {
                            layoutTextView.visibility = View.VISIBLE
                            textView.text = item.value
                            img_selectTime.setOnClickListener {
                                val dialogView = View.inflate(mMainActivity, R.layout.date_time_picker, null)
                                val alertDialog = android.app.AlertDialog.Builder(mMainActivity).create()
                                dialogView.date_time_set.setOnClickListener {
                                    val datePicker = dialogView.date_picker
                                    val s = String.format(mMainActivity.resources.getString(R.string.format_typeday), datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
                                    textView.setText(s)
                                    alertDialog.dismiss()
                                }
                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                        Field.Type.TEXT -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.setText(item.value)
                        }
                        Field.Type.INTEGER, Field.Type.SHORT -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.inputType = InputType.TYPE_CLASS_NUMBER
                            editText.setText(item.value)
                        }
                        Field.Type.DOUBLE, Field.Type.FLOAT -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                            editText.setText(item.value)
                        }
                    }
                val dialog = AlertDialog.Builder(mMainActivity)
                        .setView(layout)
                        .setMessage(item.alias)
                        .setTitle("Cập nhật thuộc tính")
                        .setPositiveButton(R.string.btn_Update, null) //Set to null. We override the onclick
                        .setNegativeButton(R.string.btn_Esc, null)
                        .create()

                dialog.setOnShowListener { dialogInterface ->

                    val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                    button.setOnClickListener { view1 ->
                        var value: String? = null
                        if (lstUniqueValues!!.size > 0 && item.fieldName == fieldNameDrawInfo || domain != null) {
                            value = spin.selectedItem.toString()
                        } else {
                            when (item.fieldType) {
                                Field.Type.DATE -> value = textView.text.toString()
                                Field.Type.FLOAT, Field.Type.DOUBLE -> try {
                                    val x = java.lang.Double.parseDouble(editText.text.toString())
                                    value = editText.text.toString()
                                } catch (e: Exception) {
                                    txtNotifyInCorrect.visibility = View.VISIBLE
                                }

                                Field.Type.TEXT -> value = editText.text.toString()
                                Field.Type.INTEGER -> try {
                                    val x = Integer.parseInt(editText.text.toString())
                                    value = editText.text.toString()
                                } catch (e: Exception) {
                                    txtNotifyInCorrect.visibility = View.VISIBLE
                                }

                                Field.Type.SHORT -> try {
                                    val x = java.lang.Short.parseShort(editText.text.toString())
                                    value = editText.text.toString()
                                } catch (e: Exception) {
                                    txtNotifyInCorrect.visibility = View.VISIBLE
                                }

                            }
                        }
                        if (value != null) {
                            dialog.dismiss()
                            item.value = value
                            val adapter = parent.adapter as FeatureViewMoreInfoAdapter
                            adapter.notifyDataSetChanged()
                        } else
                            txtNotifyInCorrect.visibility = View.VISIBLE
                    }
                }
                dialog.show()
            }
        }
    }

    fun clearSelection() {
        if (mFeatureLayerDTG != null) {
            val featureLayer = mFeatureLayerDTG!!.featureLayer
            featureLayer.clearSelection()
        }
    }

    fun dimissCallout() {
        this.clearSelection()
        if (mCallout != null && mCallout.isShowing) {
            mCallout.dismiss()
        }
    }

    private fun getHanhChinhFeature(idHanhChinh: String) {
        quanhuyen_feature = null
        if (quanhuyen_features != null) {
            for (feature in quanhuyen_features!!) {
                val maDonViHanhChinh = feature.attributes[mMainActivity.getString(R.string.IDHanhChinh)]
                if (maDonViHanhChinh != null && maDonViHanhChinh == idHanhChinh) {
                    quanhuyen_feature = feature
                }
            }
        }
    }

    fun showPopup(selectedFeature: ArcGISFeature, clickMap: Boolean?) {
        dimissCallout()
        mServiceFeatureTable = mFeatureLayerDTG!!.featureLayer.featureTable as ServiceFeatureTable
        this.mApplication.selectedFeature = selectedFeature
        val featureLayer = selectedFeature.featureTable.featureLayer
        featureLayer.selectFeature(mApplication.selectedFeature)
        lstUniqueValues = ArrayList()
        fieldNameDrawInfo = null
        val renderer = (mApplication.selectedFeature!!.featureTable as ServiceFeatureTable).layerInfo.drawingInfo.renderer
        var uniqueValues: List<UniqueValueRenderer.UniqueValue>? = null
        if (renderer is UniqueValueRenderer) {
            uniqueValues = renderer.uniqueValues
        }
        if (uniqueValues != null) {
            for (i in uniqueValues.indices) {
                lstUniqueValues!!.add(uniqueValues[i].label.toString())
            }
        }

        val inflater = LayoutInflater.from(this.mMainActivity.applicationContext)
        linearLayout = inflater.inflate(R.layout.layout_popup_infos, null) as LinearLayout
        refreshPopup()
        linearLayout!!.txt_title_layer.text = mFeatureLayerDTG!!.featureLayer.name
        val imgBtn_ViewMoreInfo = linearLayout!!.imgBtn_ViewMoreInfo
        if (mFeatureLayerDTG!!.action!!.isEdit) {
            imgBtn_ViewMoreInfo.visibility = View.VISIBLE
            imgBtn_ViewMoreInfo.setOnClickListener { v -> viewMoreInfo() }
        } else
            imgBtn_ViewMoreInfo.visibility = View.GONE
        val imgBtn_view_attachment = linearLayout!!.imgBtn_view_attachment
        if ((this.mApplication.selectedFeature!! as ArcGISFeature).canEditAttachments()) {
            imgBtn_view_attachment.visibility = View.VISIBLE
            imgBtn_view_attachment.setOnClickListener { v ->
                val intent = Intent(mMainActivity, AttachmentActivity::class.java)
                mMainActivity.startActivity(intent)
            }
        } else
            imgBtn_view_attachment.visibility = View.GONE
        val imgBtn_delete = linearLayout!!.imgBtn_delete

        //        if (mFeatureLayerDTG.getAction().isDelete() && isDeleteFeature()) {
        if (mFeatureLayerDTG!!.action!!.isDelete) {
            imgBtn_delete.visibility = View.VISIBLE
            imgBtn_delete.setOnClickListener { v ->
                mApplication.selectedFeature!!.featureTable.featureLayer.clearSelection()
                deleteFeature()
            }
        } else
            imgBtn_delete.visibility = View.GONE
        linearLayout!!.btn_layer_close.setOnClickListener { v -> dimissCallout() }

        linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val envelope = mApplication.selectedFeature!!.geometry.extent
//        if (!clickMap!!) mMapView.setViewpointGeometryAsync(envelope, 0.0)
//        mMapView.setViewpointCenterAsync(envelope.center)
//        if (mMapView.mapScale > featureLayer.minScale && featureLayer.minScale != 0.0)
//            mMapView.setViewpointScaleAsync(featureLayer.minScale)
//        // show CallOut
//        mCallout!!.location = envelope.center
//        mCallout.content = linearLayout!!
//        this.runOnUiThread {
//            mCallout.refresh()
//            mCallout.show()
//        }
        showCallout(envelope.center, linearLayout, mMainActivity.mapView.mapScale)
    }

    private fun showCallout(point: Point?, view: View?, scale: Double) {
        mMainActivity.runOnUiThread {
            val viewpointCenterAsync = mMainActivity.mapView.setViewpointCenterAsync(point, scale)
            viewpointCenterAsync.addDoneListener {
                val result = viewpointCenterAsync.get()
                if (result) {
                    mCallout!!.location = point
                    mCallout.content = view
                    mCallout.refresh()
                    mCallout.show()
                } else {
                    showCallout(point, view, scale)
                }
            }

        }
    }

    private fun deleteFeature() {
        val builder = AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn xóa đối tượng này không?")
        builder.setPositiveButton("Có") { dialog, which ->
            dialog.dismiss()
            (mApplication.selectedFeature!! as ArcGISFeature).loadAsync()

            // update the selected feature
            (mApplication.selectedFeature!! as ArcGISFeature).addDoneLoadingListener {
                if ((mApplication.selectedFeature!! as ArcGISFeature).loadStatus == LoadStatus.FAILED_TO_LOAD) {
                    Log.d(mMainActivity.resources.getString(R.string.app_name), "Error while loading feature")
                }
                try {
                    // update feature in the feature table
                    val mapViewResult = mServiceFeatureTable!!.deleteFeatureAsync(mApplication.selectedFeature!!)
                    mapViewResult.addDoneListener {
                        // apply change to the server
                        val serverResult = mServiceFeatureTable!!.applyEditsAsync()
                        serverResult.addDoneListener {
                            var edits: List<FeatureEditResult>? = null
                            try {
                                edits = serverResult.get()
                                if (edits!!.size > 0) {
                                    if (!edits[0].hasCompletedWithErrors()) {
                                        Log.e("", "Feature successfully updated")
                                        DAlertDialog().show(mMainActivity, "Thông báo", "Xóa thành công")

                                    } else DAlertDialog().show(mMainActivity, "Thông báo", "Xóa thất bại")
                                } else DAlertDialog().show(mMainActivity, "Thông báo", "Xóa thất bại")
                            } catch (e: Exception) {
                                DAlertDialog().show(mMainActivity, e)
                            }
                        }
                    }

                } catch (e: Exception) {
                    DAlertDialog().show(mMainActivity, e)
                }
            }
            mCallout?.dismiss()
        }.setNegativeButton("Không") { dialog, which -> dialog.dismiss() }.setCancelable(false)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()


    }


    companion object {


        fun isPast1DateOfCurrentDate(calendar: Calendar?, currentDate: Calendar?): Boolean {
            kotlin.require(!(calendar == null || currentDate == null)) { "The dates must not be null" }
            val deviationTime = currentDate!!.timeInMillis - calendar!!.timeInMillis
            val time = 24 * 60 * 60 * 1000
            return time - deviationTime > 0
        }
    }

}
