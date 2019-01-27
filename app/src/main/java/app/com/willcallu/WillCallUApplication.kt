package app.com.willcallu

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class WillCallUApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        mPref = getSharedPreferences(AppConstants.PREF_APP, Context.MODE_PRIVATE)

        applicationContext()
    }

    companion object {
        private var instance: WillCallUApplication? = null
        private var mPref: SharedPreferences? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

//        fun getPref(): SharedPreferences {
//            mPref = applicationContext().getSharedPreferences(AppConstants.PREF_APP, Context.MODE_PRIVATE)
//            return mPref!!
//        }

        /**
         * incoming ring time set by user, by default its 25
         */
        fun getRingTime(): Int {
            return mPref!!.getInt(AppConstants.PREF_RING_TIME, 25)
        }

        fun setRingTime(mPrefs: SharedPreferences, seconds: Int) {
            val editor = mPrefs.edit()
            editor.putInt(AppConstants.PREF_RING_TIME, seconds)
            editor.apply()
        }

        /**
         * check whether the only contacts flag is enable, that means if its enable
         * then sms will be sent to contacts only
         */
        fun getOnlyContactsBoolEnable(): Boolean {
            return mPref!!.getBoolean(AppConstants.PREF_BOOL_ONLY_CONTACTS, false)
        }

        fun setOnlyContactsBoolEnable(mPrefs: SharedPreferences, flag: Boolean) {
            val editor = mPrefs.edit()
            editor.putBoolean(AppConstants.PREF_BOOL_ONLY_CONTACTS, flag)
            editor.apply()
        }
    }
}