package mekong.ditagis.com.qlts

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import com.esri.arcgisruntime.data.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.android.synthetic.main.item_add_feature_date.view.*
import kotlinx.android.synthetic.main.item_add_feature_edittext.view.*
import kotlinx.android.synthetic.main.item_add_feature_spinner.view.*
import kotlinx.android.synthetic.main.layout_select_time.view.*
import mekong.ditagis.com.qlts.async.EditAsync
import mekong.ditagis.com.qlts.utities.Constant
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.*


class UpdateActivity : AppCompatActivity() {
    private lateinit var mApplication: DApplication

    private var mArcGISFeature: ArcGISFeature? = null

    private val attributes: HashMap<String, Any>
        get() {
            val attributes = HashMap<String, Any>()
            var currentFieldName: String
            for (i in 0 until llayoutField.childCount) {
                val viewI = llayoutField.getChildAt(i) as LinearLayout
                for (j in 0 until viewI.childCount) {
                    try {
                        val viewJ = viewI.getChildAt(j) as TextInputLayout
                        if (viewJ.visibility == View.VISIBLE
                                && viewJ.hint != null) {
                            val fieldName = viewJ.tag.toString()
                            val field = (mArcGISFeature!!.featureTable as ServiceFeatureTable) .getField(fieldName)
                            currentFieldName = fieldName
                            if (currentFieldName.isEmpty()) continue
                            for (k in 0 until viewJ.childCount) {
                                val viewK = viewJ.getChildAt(k)
                                if (viewK is FrameLayout) {
                                    for (l in 0 until viewK.childCount) {
                                        val viewL = viewK.getChildAt(l)
                                        if (viewL is TextInputEditText) {
                                            if (field.domain != null) {
                                                val codedValues = (field.domain as CodedValueDomain).codedValues

                                                val valueDomain = getCodeDomain(codedValues, viewL.text.toString())
                                                if (valueDomain != null) attributes[currentFieldName] = valueDomain.toString()
                                            } else {
                                                attributes[currentFieldName] = viewL.text.toString()
                                            }

                                        }
                                    }
                                } else if (viewK is AppCompatSpinner) {
                                    if (field.domain != null) {
                                        val codedValues = (field.domain as CodedValueDomain).codedValues
                                        val valueDomain = getCodeDomain(codedValues, viewK.selectedItem.toString())
                                        if (valueDomain != null) attributes[currentFieldName] = valueDomain.toString()
                                    }

                                }
                            }

                        }
                    } catch (e: Exception) {

                    }
                }
            }
            return attributes
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        mApplication = application as DApplication
        initViews()
    }

    @SuppressLint("SetTextI18n")
    private fun update() {
        mApplication.progressDialog.changeTitle(this@UpdateActivity, root, "Đang lưu...")
        EditAsync(btnUpdate, this@UpdateActivity, mArcGISFeature!!.featureTable as ServiceFeatureTable,
                mApplication.selectedFeature!! as ArcGISFeature, object : EditAsync.AsyncResponse {
            override fun processFinish(feature: Boolean?) {
                mApplication.progressDialog.dismiss()
                feature?.let {
                    Toast.makeText(this@UpdateActivity, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this@UpdateActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }
            }
        }).execute(attributes)

    }

    private fun getCodeDomain(codedValues: List<CodedValue>, value: String): Any? {
        var code: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.name == value) {
                code = codedValue.code
                break
            }
        }
        return code
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        btnUpdate.setOnClickListener { update() }
        mApplication.progressDialog.changeTitle(this@UpdateActivity, root, "Đang khởi tạo thuộc tính...")
        mArcGISFeature = mApplication.selectedFeature!! as ArcGISFeature

        swipe.setOnRefreshListener {
            loadData()
            swipe.isRefreshing = false
        }

        loadData()
    }

    private fun loadData() {

        llayoutField.removeAllViews()


        for (fieldName in mArcGISFeature!!.attributes.keys) {
            if (Constant.Field.NONE_UPDATE_FIELDS.find { f -> f == fieldName } != null) continue
            val field = mArcGISFeature!!.featureTable.getField(fieldName)
            var value: Any? = null
            if (mArcGISFeature != null) {
                value = mArcGISFeature!!.attributes[field.name]
            }
            if (field.domain != null) {
                val layoutView = layoutInflater.inflate(R.layout.item_add_feature_spinner, null)
                val codedValueDomain = field.domain as CodedValueDomain
                val adapter = ArrayAdapter(this@UpdateActivity, android.R.layout.simple_list_item_1, ArrayList<String>())
                layoutView.spinnerAddSpinnerValue.adapter = adapter
                val values = ArrayList<String>()
                values.add(Constant.EMPTY)
                var selectedValue: String? = null
                for (codedValue in codedValueDomain.codedValues) {
                    values.add(codedValue.name)
                    if (value != null && codedValue.code == value)
                        selectedValue = codedValue.name
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

                        val layoutView =layoutInflater.inflate(R.layout.item_add_feature_edittext, null)
                        layoutView.llayoutAddFeatureEdittext.hint = field.alias
                        layoutView.llayoutAddFeatureEdittext.tag = fieldName
                        if (value != null) {
                            layoutView.etxtNumber.setText(value.toString())
                        }
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
                        val layoutView =layoutInflater.inflate(R.layout.item_add_feature_date, null) as LinearLayout
                        layoutView.textInputLayoutAddFeatureDate.hint = field.alias
                        layoutView.textInputLayoutAddFeatureDate.tag = fieldName
                        if (value != null)
                            layoutView.editAddDateValue.setText(Constant.DATE_FORMAT.format((value as Calendar).time))
                        layoutView.btnAddDate.setOnClickListener { selectDate(field, layoutView) }
                        llayoutField.addView(layoutView)
                    }

                    else -> {
//                        setViewVisible(layoutView, layoutView.llayout_add_feature_spinner, field)
                    }
                }
            }
        }
        mApplication.progressDialog.dismiss()

    }

    private fun selectDate(field: Field, layoutView: LinearLayout) {
//        mRootView.fab_parent.close(false)
        val dialog = BottomSheetDialog(this@UpdateActivity)
        dialog.setCancelable(true)
        val layoutSelectTime = layoutInflater.inflate(R.layout.layout_select_time, null)
        val calendar = Calendar.getInstance()

        if (layoutView.editAddDateValue.text!!.trim().isNotEmpty()) {
            val date = Constant.DATE_FORMAT.parse(layoutView.editAddDateValue.text!!.trim().toString())

            calendar.time = date

        }
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        layoutSelectTime.numberPickerYear.value = year
        layoutSelectTime.numberPickerMonth.value = month
        layoutSelectTime.numberPickerDay.value = day
        layoutSelectTime.numberPickerMonth.setOnValueChangedListener { picker, oldVal, newVal ->
            when (newVal) {
                1, 3, 5, 7, 8, 10, 12 -> {
                    layoutSelectTime.numberPickerDay.maxValue = 31
                }
                4, 6, 9, 11 -> {
                    layoutSelectTime.numberPickerDay.maxValue = 30
                }
                2 -> {
                    val year = layoutSelectTime.numberPickerYear.value
                    if (year % 400 == 0 || (year % 4 == 0 && year % 100 > 0)) {
                        //la nam nhuan
                        layoutSelectTime.numberPickerDay.maxValue = 29
                    } else {
                        layoutSelectTime.numberPickerDay.maxValue = 28
                    }
                }
            }
        }
        layoutSelectTime.numberPickerYear.setOnValueChangedListener { picker, oldVal, newVal ->
            val month = layoutSelectTime.numberPickerMonth.value
            if (month == 2)
                if (newVal % 400 == 0 || (newVal % 4 == 0 && newVal % 100 > 0)) {
                    //la nam nhuan
                    layoutSelectTime.numberPickerDay.maxValue = 29
                } else {
                    layoutSelectTime.numberPickerDay.maxValue = 28
                }
        }
        layoutSelectTime.btnOK.setOnClickListener {
            val year = layoutSelectTime.numberPickerYear.value
            val month = layoutSelectTime.numberPickerMonth.value
            val day = layoutSelectTime.numberPickerDay.value
            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day)
            val date = calendar.time
            layoutView.editAddDateValue.setText(Constant.DATE_FORMAT.format(date))
            dialog.dismiss()
        }

        dialog.setContentView(layoutSelectTime)

        dialog.show()
    }
}