package mekong.ditagis.com.qlts

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.layers.FeatureLayer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_add_feature.*
import kotlinx.android.synthetic.main.item_add_feature_date.view.*
import kotlinx.android.synthetic.main.item_add_feature_edittext.view.*
import kotlinx.android.synthetic.main.item_add_feature_spinner.view.*
import kotlinx.android.synthetic.main.layout_select_time.view.*
import mekong.ditagis.com.qlts.async.AddFeatureTask
import mekong.ditagis.com.qlts.entities.FeatureLayerValueIDField
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class AddFeatureActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mApplication: DApplication
    private var mImages: MutableList<ByteArray>? = null
    private var mUri: Uri? = null
    private var mFeatureLayerValueIDField: FeatureLayerValueIDField? = null
    private val mAdapterLayer: ArrayAdapter<String>? = null
    private var mFeatureLayer: FeatureLayer? = null
    private var mFeature: ArcGISFeature? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_feature)
        mApplication = application as DApplication
        initViews()
    }

    private fun initViews() {
        val id = intent.getIntExtra(Constant.IntentExtra.ID_ADD_FEATURE, 0)
        mFeatureLayerValueIDField = mApplication.idFeatureLayerToAdd[id]
        if (mFeatureLayerValueIDField != null) {

            mImages = ArrayList()
            btnCapture.setOnClickListener { view: View -> onClick(view) }
            btnAdd.setOnClickListener { view: View -> onClick(view) }
            btnPickPhoto.setOnClickListener { view: View -> onClick(view) }
            Objects.requireNonNull(supportActionBar)?.setDisplayHomeAsUpEnabled(true)
            Objects.requireNonNull(supportActionBar)?.setDisplayShowHomeEnabled(true)
            mFeatureLayer = mFeatureLayerValueIDField!!.featureLayer
            mFeature = (mFeatureLayer!!.featureTable as ServiceFeatureTable).createFeature() as ArcGISFeature
            loadData()
//        LoadingDataFeatureAsync(this@AddFeatureActivity, mFeatureLayer!!.featureTable.fields,
//                object : LoadingDataFeatureAsync.AsyncResponse {
//                    override fun processFinish(views: List<View?>?) {
//                        if (views != null) for (view1 in views) {
//                            llayout_add_feature_field!!.addView(view1)
//                        }
//                        llayout_add_feature_progress!!.visibility = View.GONE
//                        llayout_add_feature_main!!.visibility = View.VISIBLE
//                    }
//
//                }).execute(true)
        }
    }

    private fun loadData() {
        mApplication!!.progressDialog.changeTitle(this@AddFeatureActivity, root, "Đang khởi tạo thuộc tính...")
        llayoutField.removeAllViews()
        for (field in mFeatureLayer!!.featureTable.fields) {
            val fieldName = field.name
            if (Constant.Field.NONE_UPDATE_FIELDS.find { f -> f == fieldName } != null) continue
            val field = mFeatureLayer!!.featureTable.fields.find { field -> field.name == fieldName }
            if (field == null) continue
            if (field.domain != null) {
                val layoutView =layoutInflater.inflate(R.layout.item_add_feature_spinner,null)
                val codedValueDomain = field.domain as CodedValueDomain
                val adapter = ArrayAdapter(this@AddFeatureActivity, android.R.layout.simple_list_item_1, ArrayList<String>())
                layoutView.spinnerAddSpinnerValue.adapter = adapter
                val values = ArrayList<String>()
                values.add(Constant.EMPTY)
                var selectedValue: String? = null
                for (codedValue in codedValueDomain.codedValues) {
                    values.add(codedValue.name)
                }
                layoutView.llayoutAddFeatureSpinner.hint = field.alias
                layoutView.llayoutAddFeatureSpinner.tag = fieldName

                layoutView.txtSpinTitle.text = field.alias
                adapter.addAll(values)
                adapter.notifyDataSetChanged()

                for (i in values.indices) {
                    if (selectedValue != null && values[i] == selectedValue) {
                        layoutView.spinnerAddSpinnerValue.setSelection(i)
                        break
                    }
                }
                llayoutField.addView(layoutView)
            } else {
//                val nm = NumberFormat.getCurrencyInstance()
                when (field.fieldType) {
                    Field.Type.INTEGER, Field.Type.SHORT, Field.Type.DOUBLE, Field.Type.FLOAT, Field.Type.TEXT -> {

                        val layoutView = layoutInflater.inflate(R.layout.item_add_feature_edittext, null)
                        layoutView.llayoutAddFeatureEdittext.hint = field.alias
                        layoutView.llayoutAddFeatureEdittext.tag = fieldName
                        when (field.fieldType) {
                            Field.Type.INTEGER, Field.Type.SHORT -> {
                                layoutView.etxtNumber.inputType = InputType.TYPE_CLASS_NUMBER
                            }
                            Field.Type.DOUBLE, Field.Type.FLOAT -> {
                                layoutView.etxtNumber.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED
                            }
                        }
                        llayoutField.addView(layoutView)
                    }
                    Field.Type.DATE -> {
                        val layoutView = layoutInflater.inflate(R.layout.item_add_feature_date, null) as LinearLayout
                        layoutView.textInputLayoutAddFeatureDate.hint = field.alias
                        layoutView.textInputLayoutAddFeatureDate.tag = fieldName
                        layoutView.btnAddDate.setOnClickListener { selectDate(field, layoutView) }
                        llayoutField.addView(layoutView)
                    }

                    else -> {
//                        setViewVisible(layoutView, layoutView.llayout_add_feature_spinner, field)
                    }
                }
            }
        }
        val typeIDFieldName = mFeature!!.featureTable.typeIdField
        if (!typeIDFieldName.isNullOrEmpty()) {
            val typeIDField = mFeature!!.featureTable.getField(typeIDFieldName)
            if (typeIDField.domain != null) {
                val codedValueDomain = typeIDField.domain as CodedValueDomain
                val codedValue = codedValueDomain.codedValues.find { codedValue -> codedValue.name == mFeatureLayerValueIDField!!.valueIDField }
                if (codedValue != null) {
                    mFeature!!.attributes[typeIDFieldName] = codedValue.code
                } else {

                }
            }
        }
        mApplication!!.progressDialog.dismiss()
        if (!mFeature!!.canEditAttachments()) {
            btnCapture.visibility = View.GONE
        }

    }

    private fun selectDate(field: Field, layoutView: LinearLayout) {
//        mRootView.fab_parent.close(false)
        val dialog = BottomSheetDialog(this@AddFeatureActivity)
        dialog.setCancelable(true)
        val layout = layoutInflater.inflate(R.layout.layout_select_time, null)
        val calendar = Calendar.getInstance()

        if (layoutView.editAddDateValue.text!!.trim().isNotEmpty()) {
            val date = Constant.DATE_FORMAT.parse(layoutView.editAddDateValue.text!!.trim().toString())

            calendar.time = date

        }
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        layout.numberPickerYear.value = year
        layout.numberPickerMonth.value = month
        layout.numberPickerDay.value = day
        layout.numberPickerMonth.setOnValueChangedListener { picker, oldVal, newVal ->
            when (newVal) {
                1, 3, 5, 7, 8, 10, 12 -> {
                    layout.numberPickerDay.maxValue = 31
                }
                4, 6, 9, 11 -> {
                    layout.numberPickerDay.maxValue = 30
                }
                2 -> {
                    val year = layout.numberPickerYear.value
                    if (year % 400 == 0 || (year % 4 == 0 && year % 100 > 0)) {
                        //la nam nhuan
                        layout.numberPickerDay.maxValue = 29
                    } else {
                        layout.numberPickerDay.maxValue = 28
                    }
                }
            }
        }
        layout.numberPickerYear.setOnValueChangedListener { picker, oldVal, newVal ->
            val month = layout.numberPickerMonth.value
            if (month == 2)
                if (newVal % 400 == 0 || (newVal % 4 == 0 && newVal % 100 > 0)) {
                    //la nam nhuan
                    layout.numberPickerDay.maxValue = 29
                } else {
                    layout.numberPickerDay.maxValue = 28
                }
        }
        layout.btnOK.setOnClickListener {
            val year = layout.numberPickerYear.value
            val month = layout.numberPickerMonth.value
            val day = layout.numberPickerDay.value
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day)
            val date = calendar.time
            layoutView.editAddDateValue.setText(Constant.DATE_FORMAT.format(date))
            dialog.dismiss()
        }

        dialog.setContentView(layout)

        dialog.show()
    }

    private fun hadPoint(): Boolean {
        return mApplication!!.center != null
    }

    private fun capture() {
        val cameraIntent = Intent(this@AddFeatureActivity, CameraActivity::class.java)
        this.startActivityForResult(cameraIntent, Constant.RequestCode.ADD_FEATURE_ATTACHMENT)

    }

    private fun pickPhoto() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, Constant.RequestCode.PICK_PHOTO)
    }

    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Constant.CompressFormat.TYPE_COMPRESS, 100, outputStream)
        val image = outputStream.toByteArray()
        try {
            outputStream.close()
        } catch (e: IOException) {

        }

        return image
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnAdd -> if (!hadPoint()) {
                Toast.makeText(this@AddFeatureActivity, R.string.message_add_feature_had_not_point, Toast.LENGTH_LONG).show()
            } else if (mFeatureLayer == null) {
                Toast.makeText(this@AddFeatureActivity, R.string.message_add_feature_had_not_feature, Toast.LENGTH_LONG).show()
            } else {
                mApplication.progressDialog.changeTitle(this@AddFeatureActivity, root,
                        "Đang lưu...")
                AddFeatureTask(object : AddFeatureTask.Response {
                    override fun post(output: Feature?) {
                        if (output != null) {
                            mApplication.selectedFeature = output
                            goHome()
                        } else {
                            Toast.makeText(root.context, "Nhập thiếu dữ liệu hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                        }
                        mApplication.progressDialog.dismiss()
                    }

                }).execute(this@AddFeatureActivity, mApplication!!, llayoutField, mFeature!!)


            }
            R.id.btnCapture -> capture()
            R.id.btnPickPhoto -> pickPhoto()
        }
    }


    private fun handlingImage(bitmap: Bitmap?, isFromCamera: Boolean) {
        try {
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                val imageView = ImageView(llayoutImage.context)
                imageView.setPadding(0, 0, 0, 10)
                if (isFromCamera) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    imageView.setImageBitmap(bitmap)
                }
                val image = getByteArrayFromBitmap(bitmap)
                Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
                llayoutImage.addView(imageView)
                mImages!!.add(image)
                mApplication!!.images = mImages
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.RequestCode.ADD_FEATURE_ATTACHMENT -> if (resultCode == Activity.RESULT_OK) {

                val bitmaps = mApplication!!.bitmaps!!
                handlingImage(bitmaps.first(), true)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT)
            }
            Constant.RequestCode.PICK_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        handlingImage(bitmap, false)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddFeatureActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        goHomeCancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun goHomeCancel() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
}