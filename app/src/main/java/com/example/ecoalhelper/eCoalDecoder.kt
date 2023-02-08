package com.example.ecoalhelper

import java.sql.ResultSet
import android.util.Log
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

enum class eCoalModes (mode: Int){
    Manual(0),
    Auto(1),
    Alarm(2);

    companion object {
        fun fromInt(mode: Int) = values().first { mode == mode }
    }
}

class eCoalRecord (private val ts: Timestamp, val mode: eCoalModes, val fuel: Int, val nextFuelTime: Timestamp) {

    init {
    }

    var timestamp = Timestamp(0)
        get() = this.ts

    //var mode = eCoalModes.Alarm
    //    get() = this.mode

    var fuelLevel = 0
        get() = this.fuel

    //var nextFuelTime = Timestamp(0)
    //    get() = this.nextFuelTime


}

class eCoalDecoder (input: ResultSet) {
    private val TAG = "eCoalDecoder"
    private var set = input
    init {
        Log.d(TAG, "Initializing eCoalDecoder")
    }

    fun getLatestRecord(): eCoalRecord{
        set.last()
        var ts = set.getTimestamp("ts")
        var mode: eCoalModes = eCoalModes.values()[set.getInt("tryb_auto_state")]
        var fuel_level: Int = set.getInt("fuel_level")
        var next_fuel_time: Timestamp = set.getTimestamp("next_fuel_time")

        var record = eCoalRecord(ts, mode, fuel_level, next_fuel_time)

        return record
    }

}