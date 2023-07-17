package com.android.notepad.services.statistics_service

import android.content.Context
import com.android.notepad.SharedPreferencesKeys
import com.google.gson.Gson

class StatisticsStorage() {
    private val gson = Gson()

    fun saveStatistics(statistics: StatisticsData, context: Context) {
        val json = gson.toJson(statistics)
        val sharedPreferences =
            context.getSharedPreferences(SharedPreferencesKeys.ApplicationStorageName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(SharedPreferencesKeys.Statistics, json)
        editor.apply()
    }

    fun getStatistics(context: Context): StatisticsData {
        val sharedPreferences =
            context.getSharedPreferences(SharedPreferencesKeys.ApplicationStorageName, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(SharedPreferencesKeys.Statistics, "")
        return if (json.isNullOrEmpty()) StatisticsData() else gson.fromJson(json, StatisticsData::class.java)
    }
}