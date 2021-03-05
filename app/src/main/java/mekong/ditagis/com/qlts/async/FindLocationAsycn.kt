package mekong.ditagis.com.qlts.async

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import mekong.ditagis.com.qlts.R
import mekong.ditagis.com.qlts.entities.DAddress
import java.io.IOException
import java.util.*

@SuppressLint("StaticFieldLeak")
class FindLocationAsycn(private val mContext: Context, private val mIsFromLocationName: Boolean,
                        private val mDelegate: AsyncResponse) : AsyncTask<String?, Void?, List<DAddress>?>() {
    private val mGeocoder: Geocoder = Geocoder(mContext, Locale.getDefault())

    private var mLongtitude = 0.0
    private var mLatitude = 0.0

    interface AsyncResponse {
        fun processFinish(output: List<DAddress>?)
    }

    fun setLongtitude(mLongtitude: Double) {
        this.mLongtitude = mLongtitude
    }

    fun setLatitude(mLatitude: Double) {
        this.mLatitude = mLatitude
    }

    override fun doInBackground(vararg params: String?): List<DAddress>? {
        if (!Geocoder.isPresent()) return null
        val lstLocation: MutableList<DAddress> = ArrayList()
        if (mIsFromLocationName) {
            val text = params[0]
            try {
                val addressList = mGeocoder.getFromLocationName(text, 5)
                for (address in addressList) lstLocation.add(DAddress(address.longitude, address.latitude,
                        address.subAdminArea, address.adminArea, address.getAddressLine(0)))
            } catch (ignored: IOException) {
                //todo grpc failed
                Log.e("error", ignored.toString())
            }
        } else {
            try {
                val addressList = mGeocoder.getFromLocation(mLatitude, mLongtitude, 1)
                for (address in addressList) lstLocation.add(DAddress(address.longitude, address.latitude,
                        address.subAdminArea, address.adminArea, address.getAddressLine(0)))
            } catch (ignored: IOException) {
                Log.e("error", ignored.toString())
            }
        }
        return lstLocation
    }

    override fun onPostExecute(addressList: List<DAddress>?) {
        super.onPostExecute(addressList)
        if (addressList == null || addressList.isEmpty()) Toast.makeText(mContext, R.string.message_no_geocoder_available, Toast.LENGTH_LONG).show()
        else
            mDelegate.processFinish(addressList)
    }

}