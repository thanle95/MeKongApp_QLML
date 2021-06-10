package mekong.ditagis.com.qlts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_detail_info_customer.*
import mekong.ditagis.com.qlts.adapter.FeatureViewInfoAdapter
import mekong.ditagis.com.qlts.utities.DApplication
import java.util.ArrayList

class DetailInfoCustomer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_info_customer)

        val application = application as DApplication
        val adapter = FeatureViewInfoAdapter(this, ArrayList())
        lstview.adapter = adapter

        if(!application.dongHoKhachHangInfos.isNullOrEmpty()) {
            val dongHo = application.dongHoKhachHangInfos.first()
            val item1 = FeatureViewInfoAdapter.Item()
            item1.alias = "Tên khách hàng"
            item1.value = dongHo.TenKH
            adapter.add(item1)

            val item2 = FeatureViewInfoAdapter.Item()
            item2.alias = "ID khách hàng"
            item2.value = dongHo.IDKH
            adapter.add(item2)

            val item3 = FeatureViewInfoAdapter.Item()
            item3.alias = "Danh bộ"
            item3.value = dongHo.SoDB
            adapter.add(item3)

            val item4 = FeatureViewInfoAdapter.Item()
            item4.alias = "Địa chỉ"
            item4.value = dongHo.SONHA
            adapter.add(item4)

            val item5 = FeatureViewInfoAdapter.Item()
            item5.alias = "Tên đường"
            item5.value = dongHo.TenConDuong
            adapter.add(item5)

            val item6 = FeatureViewInfoAdapter.Item()
            item6.alias = "Huyện/TP"
            item6.value = dongHo.TenHuyen
            adapter.add(item6)

            val item7 = FeatureViewInfoAdapter.Item()
            item7.alias = "SDT"
            item7.value = dongHo.SDT
            adapter.add(item7)

            val item8 = FeatureViewInfoAdapter.Item()
            item8.alias = "Hợp đồng"
            item8.value = dongHo.SoHD
            adapter.add(item8)

            val item9 = FeatureViewInfoAdapter.Item()
            item9.alias = "Hiệu"
            item9.value = dongHo.TLKHieu
            adapter.add(item9)

            val item10 = FeatureViewInfoAdapter.Item()
            item10.alias = "Cỡ"
            item10.value = dongHo.KichCoDH
            adapter.add(item10)

            val item11 = FeatureViewInfoAdapter.Item()
            item11.alias = "Định mức"
            item11.value = dongHo.DinhMuc
            adapter.add(item11)
        }
        adapter.notifyDataSetChanged()
    }
}