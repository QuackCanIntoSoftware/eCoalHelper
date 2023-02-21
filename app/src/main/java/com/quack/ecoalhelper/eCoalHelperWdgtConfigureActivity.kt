package com.quack.ecoalhelper

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.makeText
import com.quack.ecoalhelper.databinding.ECoalHelperWdgtConfigureBinding

/**
 * The configuration screen for the [eCoalHelperWdgt] AppWidget.
 */
class eCoalHelperWdgtConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var appWidgetText: EditText
    private lateinit var txFuelLevelAlarm: EditText
    private lateinit var txFuelLevelWarning: EditText
    private lateinit var txTimeout: EditText

    internal var onClickListener = View.OnClickListener {
        val context = this@eCoalHelperWdgtConfigureActivity

        /* When the button is clicked, get value, validate and save */

        val prefs = eCoalPrefs(
            txFuelLevelWarning.text.toString().toInt(),
            txFuelLevelAlarm.text.toString().toInt(),
            txTimeout.text.toString().toInt()
        )

        if (prefs.validate())
        {
            /* If preferences are valid, save and close */
            savePrefs(context, appWidgetId, prefs)

            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAppWidget(context, appWidgetManager, appWidgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
        else
        {
            /* pop out information */
            val toast = makeText(context, context.getString(R.string.toast_invalid_parameters), Toast.LENGTH_LONG)
            toast.show()
        }

    }

    private fun createNotificationChannels(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val warnChannel = NotificationChannel(
                context.getString(R.string.notif_warn_channel_id),      // ID
                context.getString(R.string.notif_warn_channel_name),    // name
                NotificationManager.IMPORTANCE_DEFAULT                  // importance
            ).apply {
                    description = context.getString(R.string.notif_warn_channel_desc)
            }

            val alarmChannel = NotificationChannel(
                context.getString(R.string.notif_alarm_channel_id),      // ID
                context.getString(R.string.notif_alarm_channel_name),    // name
                NotificationManager.IMPORTANCE_HIGH                      // importance
            ).apply {
                    description = context.getString(R.string.notif_alarm_channel_desc)
            }


            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannels(listOf(warnChannel, alarmChannel))
        }
    }

    private lateinit var binding: ECoalHelperWdgtConfigureBinding

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        binding = ECoalHelperWdgtConfigureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* Bind EditText fields to variables */
        txFuelLevelAlarm = binding.txFuelLevelAlarm
        txFuelLevelWarning = binding.txFuelLevelWarning
        txTimeout = binding.txTimeout
        binding.addButton.setOnClickListener(onClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        /* Load preferences */
        val prefs = loadPrefs(this@eCoalHelperWdgtConfigureActivity, appWidgetId)
        txTimeout.setText(prefs.timeout.toString())
        txFuelLevelAlarm.setText(prefs.fuelLevelAlarm.toString())
        txFuelLevelWarning.setText(prefs.fuelLevelWarning.toString())

        createNotificationChannels(applicationContext)
    }

    private fun prefsKey(pref: String): String {
        return PREF_PREFIX_KEY + appWidgetId
    }
}

private const val PREFS_NAME = "com.quack.ecoalhelper.eCoalHelperWdgt"
private const val PREF_PREFIX_KEY = "appwidget_"


/* Save preferences with prefix and widget id to have separate setting for every created widget */
internal fun savePrefs(context: Context, appWidgetId: Int, input: eCoalPrefs) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putInt(PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_fuel_level_alarm), input.fuelLevelAlarm)
    prefs.putInt(PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_fuel_level_warning), input.fuelLevelWarning)
    prefs.putInt(PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_timeout), input.timeout)
    prefs.apply()
}

/* Read the prefix from the SharedPreferences object for this widget. */
/* If there is no preference saved, get the default from a resource */
internal fun loadPrefs(context: Context, appWidgetId: Int): eCoalPrefs {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val fuelAlarm = prefs.getInt(
        PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_fuel_level_alarm), context.resources.getInteger(
            R.integer.dflt_fuel_level_alarm
        ))
    val fuelWarn = prefs.getInt(
        PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_fuel_level_warning), context.resources.getInteger(
            R.integer.dflt_fuel_level_warning
        ))
    val timeout = prefs.getInt(
        PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_timeout), context.resources.getInteger(
            R.integer.dflt_conn_timeout
        ))
    return eCoalPrefs(fuelWarn, fuelAlarm, timeout)
}

/* Delete preferences on object destroy */
internal fun deletePrefs(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_fuel_level_alarm))
    prefs.remove(PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_fuel_level_warning))
    prefs.remove(PREF_PREFIX_KEY + appWidgetId + context.getString(R.string.pref_timeout))
    prefs.apply()
}