package vinhlong.ditagis.com.qlts.utities

import android.content.Context
import android.content.SharedPreferences

import android.content.Context.MODE_PRIVATE

/**
 * Created by ThanLe on 4/11/2018.
 */

class Preference private constructor() {
    private var mContext: Context? = null

    val preferences: SharedPreferences
        get() = mContext!!.getSharedPreferences("LOGGED_IN", MODE_PRIVATE)

    fun setContext(context: Context) {
        mContext = context
    }

    /**
     * Method used to save Preferences
     */
    fun savePreferences(key: String, value: String) {
        val sharedPreferences = preferences
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.commit()
    }

    fun savePreferences(key: String, values: Set<String>) {
        val sharedPreferences = preferences
        val editor = sharedPreferences.edit()
        editor.putStringSet(key, values)
        editor.commit()
    }

    fun deletePreferences(key: String): Boolean {
        val editor = preferences.edit()
        editor.remove(key).commit()
        return false
    }

    fun deletePreferences(): Boolean {
        val editor = preferences.edit()
        editor.clear().commit()
        return false
    }

    /**
     * Method used to load Preferences
     */
    fun loadPreference(key: String): String? {
        try {
            val sharedPreferences = preferences
            return sharedPreferences.getString(key, "")
        } catch (nullPointerException: NullPointerException) {
            return null
        }

    }

    fun loadPreferences(key: String): Set<String>? {
        try {
            val sharedPreferences = preferences
            return sharedPreferences.getStringSet(key, null)
        } catch (nullPointerException: NullPointerException) {
            return null
        }

    }

    companion object {
        private var mInstance: Preference? = null

        val instance: Preference
            get() {
                if (mInstance == null)
                    mInstance = Preference()
                return mInstance!!
            }
    }
}
