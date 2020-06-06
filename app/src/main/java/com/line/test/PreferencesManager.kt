package com.line.test

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager private constructor(context: Context) {

    private val mPref: SharedPreferences

    companion object {
        private const val PREF_NAME = "line_test"
        private lateinit var sInstance: PreferencesManager

        @Synchronized
        fun initializeInstance(context: Context) {
            sInstance = PreferencesManager(context)
        }

        @get:Synchronized
        val instance: PreferencesManager
            get() {
                return sInstance
            }
    }

    init {
        mPref = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun setValue(key: String?, value: Boolean) {
        mPref.edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getValue(key: String?): Boolean {
        return mPref.getBoolean(key, false)
    }

    fun remove(key: String?) {
        mPref.edit()
            .remove(key)
            .apply()
    }

    fun clear(): Boolean {
        return mPref.edit()
            .clear()
            .commit()
    }
}