package com.quack.ecoalhelper

import android.content.Context
import java.sql.Timestamp

//class eCoalPrefs (val fuelLevelWarning: Int, val fuelLevelAlarm: Int, val timeout: Int){
class eCoalPrefs (val context: Context, val appWidgetId: Int){

    var fuelLevelAlarm: Int = 0
    var fuelLevelWarning: Int = 0
    var timeout: Int = 0

    init {
        loadPrefs()
    }

    private fun validateLimits(): Boolean {
        if ((fuelLevelWarning < 0) || (fuelLevelWarning > 100)) {
            return false
        }

        if ((fuelLevelAlarm < 0) || (fuelLevelAlarm > 100)) {
            return false
        }

        if (timeout <= 0) {
            return false
        }

        return true
    }
    private fun validateCoherence(): Boolean {
        if (fuelLevelWarning <= fuelLevelAlarm) return false
        return true
    }

    fun validate(): Boolean {

        if (!(validateLimits())) return false

        if (!(validateCoherence())) return false

        return true
    }

    private fun getKey(resId: Int): String {
        return Companion.PREF_PREFIX_KEY + appWidgetId + context.getString(resId)
    }

    /* Save preferences with prefix and widget id to have separate setting for every created widget */
    fun savePrefs() {
        val prefs = context.getSharedPreferences(Companion.PREFS_NAME, 0).edit()
        prefs.putInt(getKey(R.string.pref_fuel_level_alarm), this.fuelLevelAlarm)
        prefs.putInt(getKey(R.string.pref_fuel_level_warning), this.fuelLevelWarning)
        prefs.putInt(getKey(R.string.pref_timeout), this.timeout)
        prefs.apply()
    }

    /* Read the prefix from the SharedPreferences object for this widget. */
    /* If there is no preference saved, get the default from a resource */
    fun loadPrefs() {
        val prefs = context.getSharedPreferences(Companion.PREFS_NAME, 0)
        fuelLevelAlarm = prefs.getInt(
            getKey(R.string.pref_fuel_level_alarm), context.resources.getInteger(
                R.integer.dflt_fuel_level_alarm
            ))
        fuelLevelWarning = prefs.getInt(
            getKey(R.string.pref_fuel_level_warning), context.resources.getInteger(
                R.integer.dflt_fuel_level_warning
            ))
        timeout = prefs.getInt(
            getKey(R.string.pref_timeout), context.resources.getInteger(
                R.integer.dflt_conn_timeout
            ))
    }

    /* Delete preferences on object destroy */
    fun deletePrefs() {
        val prefs = context.getSharedPreferences(Companion.PREFS_NAME, 0).edit()
        prefs.remove(getKey(R.string.pref_fuel_level_alarm))
        prefs.remove(getKey(R.string.pref_fuel_level_warning))
        prefs.remove(getKey(R.string.pref_timeout))
        prefs.apply()
    }

    companion object {
        private const val PREFS_NAME = "com.quack.ecoalhelper.eCoalHelperWdgt"
        private const val PREF_PREFIX_KEY = "eCoalWidget_"
    }
}