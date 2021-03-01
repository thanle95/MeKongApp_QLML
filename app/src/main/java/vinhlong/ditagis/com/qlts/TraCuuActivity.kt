package vinhlong.ditagis.com.qlts

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.ServiceFeatureTable
import kotlinx.android.synthetic.main.activity_tra_cuu.*
import kotlinx.android.synthetic.main.layout_dialog_update_feature_listview.view.*
import kotlinx.android.synthetic.main.layout_viewmoreinfo_feature.view.*
import vinhlong.ditagis.com.qlts.adapter.FeatureViewMoreInfoAdapter
import java.util.*

class TraCuuActivity : AppCompatActivity() {
    private var mServiceFeatureTable: ServiceFeatureTable? = null
    private var mLstFeatureType: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tra_cuu)

        mServiceFeatureTable = ServiceFeatureTable("")

        setContentView(R.layout.activity_tra_cuu)
        mLstFeatureType = ArrayList()
        for (i in 0 until mServiceFeatureTable!!.featureTypes.size) {
            mLstFeatureType!!.add(mServiceFeatureTable!!.featureTypes[i].name)
        }
        val layout = layout_tracuu_include
        val lstViewInfo = layout.lstView_alertdialog_info
        lstViewInfo.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> edit(parent, view, position, id) }
    }

    private fun edit(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent.getItemAtPosition(position) is FeatureViewMoreInfoAdapter.Item) {
            val item = parent.getItemAtPosition(position) as FeatureViewMoreInfoAdapter.Item
            if (item.isEdit) {
                val builder = AlertDialog.Builder(this,
                        android.R.style.Theme_Material_Light_Dialog_Alert)
                builder.setTitle("Cập nhật thuộc tính")
                builder.setMessage(item.alias)
                builder.setCancelable(false).setNegativeButton("Hủy") { dialog, which -> dialog.dismiss() }
                val layout = this.layoutInflater.inflate(R.layout.layout_dialog_update_feature_listview, null) as android.widget.LinearLayout
                builder.setView(layout)
                val layoutTextView = layout.layout_edit_viewmoreinfo_TextView
                val textView = layout.txt_edit_viewmoreinfo
                val layoutEditText = layout.layout_edit_viewmoreinfo_Editext
                val editText = layout.etxt_edit_viewmoreinfo
                val layoutSpin = layout.layout_edit_viewmoreinfo_Spinner
                val spin = layout.spin_edit_viewmoreinfo

                val domain = mServiceFeatureTable!!.getField(item.fieldName!!).domain
                if (item.fieldName == mServiceFeatureTable!!.typeIdField) {
                    layoutSpin.visibility = View.VISIBLE
                    val adapter = android.widget.ArrayAdapter(layout.context,
                            android.R.layout.simple_list_item_1, mLstFeatureType!!)
                    spin.adapter = adapter
                    if (item.value != null)
                        spin.setSelection(mLstFeatureType!!.indexOf(item.value!!))
                } else if (domain != null) {
                    layoutSpin.visibility = View.VISIBLE
                    val codedValues = (domain as com.esri.arcgisruntime.data.CodedValueDomain).codedValues
                    if (codedValues != null) {
                        val codes = ArrayList<String>()
                        for (codedValue in codedValues)
                            codes.add(codedValue.name)
                        val adapter = android.widget.ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, codes)
                        spin.adapter = adapter
                        if (item.value != null)
                            spin.setSelection(codes.indexOf(item.value!!))

                    }
                } else
                    when (item.fieldType) {
                        Field.Type.DATE -> {
                            layoutTextView.visibility = View.VISIBLE
                            textView.text = item.value
                        }
                        Field.Type.TEXT -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.setText(item.value)
                        }
                        Field.Type.SHORT -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                            editText.setText(item.value)
                        }
                        Field.Type.DOUBLE -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.inputType = android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                            editText.setText(item.value)
                        }
                    }//                        button.setOnClickListener(new View.OnClickListener() {
                //                            @Override
                //                            public void onClick(View v) {
                //                                final View dialogView = View.inflate(TraCuuActivity.this, R.layout.date_time_picker, null);
                //                                final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(TraCuuActivity.this).create();
                //                                dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                //                                    @Override
                //                                    public void onClick(View view) {
                //                                        DatePicker datePicker =  dialogView.findViewById(R.id.date_picker);
                //
                //                                        String s = String.format("%02d_%02d_%d",
                //                                                datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());
                //
                //                                        textView.setText(s);
                //                                        alertDialog.dismiss();
                //                                    }
                //                                });
                //                                alertDialog.setView(dialogView);
                //                                alertDialog.show();
                //                            }
                //                        });
                builder.setPositiveButton("Cập nhật") { dialog, which ->
                    if (item.fieldName == mServiceFeatureTable!!.typeIdField || domain != null) {
                        item.value = spin.selectedItem.toString()
                    } else {
                        when (item.fieldType) {
                            Field.Type.DATE -> item.value = textView.text.toString()
                            Field.Type.DOUBLE -> try {
                                val x = java.lang.Double.parseDouble(editText.text.toString())
                                item.value = editText.text.toString()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(this@TraCuuActivity, "Số liệu nhập vào không đúng định dạng!!!", android.widget.Toast.LENGTH_LONG).show()
                            }

                            Field.Type.TEXT -> item.value = editText.text.toString()
                            Field.Type.SHORT -> try {
                                val x = java.lang.Short.parseShort(editText.text.toString())
                                item.value = editText.text.toString()
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(this@TraCuuActivity, "Số liệu nhập vào không đúng định dạng!!!", android.widget.Toast.LENGTH_LONG).show()
                            }

                        }
                    }


                    dialog.dismiss()
                    val adapter = parent.adapter as FeatureViewMoreInfoAdapter
                    adapter.notifyDataSetChanged()
                }

                val dialog = builder.create()
                dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                dialog.show()

            }
        }

    }

}