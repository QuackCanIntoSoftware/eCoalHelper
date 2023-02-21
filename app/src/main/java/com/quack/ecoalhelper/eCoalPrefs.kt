package com.quack.ecoalhelper

class eCoalPrefs (val fuelLevelWarning: Int, val fuelLevelAlarm: Int, val timeout: Int){

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
        var retVal: Boolean = false

        if (!(validateLimits())) return false

        if (!(validateCoherence())) return false

        return true
    }
}