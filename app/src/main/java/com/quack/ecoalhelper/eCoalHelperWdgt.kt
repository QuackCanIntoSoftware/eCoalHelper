package com.quack.ecoalhelper

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


/**
 * Implementation of App Widget functionality.
* App Widget Configuration implemented in [eCoalHelperWdgtConfigureActivity]
 */
class eCoalHelperWdgt : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }


    /*
    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
    */


override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            eCoalPrefs(context, appWidgetId).deletePrefs()
        }
    }
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

internal fun setConnectionStatus(view: RemoteViews, connectionStatus: ConnectionStates){
    view.setTextViewText(R.id.ConnectionState, connectionStatus.toString())
    //if (connectionStatus == ConnectionStates.Connected){
    //    var textViewTitle: TextView = findViewById(R.id.ConnectionState)
    //    TextViewCompat.setTextAppearance(textViewTitle, R.style.Widget_ECoalHelper_AppWidget_NormalText)
    //}
}

private fun sendWarningNotification(context: Context, id: Int, title: String, desc: String) {
    val builder =
        NotificationCompat.Builder(context, context.getString(R.string.notif_warn_channel_id))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(desc))

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(id, builder.build())
    }
}

private fun sendAlarmNotification(context: Context, id: Int, title: String, desc: String){
    val builder = NotificationCompat.Builder(context, context.getString(R.string.notif_alarm_channel_id))
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(title)
        .setContentText(desc)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(desc))


    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(id, builder.build())
    }
}

private fun checkFuelLevel(context: Context, record: eCoalRecord, prefs: eCoalPrefs){
    /* Verify fuel level */
    /* Alarm level */
    if (record.fuelLevel < prefs.fuelLevelAlarm) {
        sendAlarmNotification(context,
            context.resources.getInteger(R.integer.notif_id_fuel_level_alarm),
            context.getString(R.string.fuel_low_level_title),
            String.format(context.getString(R.string.fuel_low_level_desc),
                record.fuelLevel,
                record.fuelDepletionTime,
                record.fuelDepletionTime,
                record.fuelDepletionTime,
                record.fuelDepletionTime,
                record.fuelDepletionTime
            )
        )
    } else if (record.fuelLevel < prefs.fuelLevelWarning) {
        /* Warning level */
        sendWarningNotification(context,
            context.resources.getInteger(R.integer.notif_id_fuel_level_warning),
            context.getString(R.string.fuel_low_level_title),
            String.format(context.getString(R.string.fuel_low_level_desc),
                record.fuelLevel,
                record.fuelDepletionTime,
                record.fuelDepletionTime,
                record.fuelDepletionTime,
                record.fuelDepletionTime,
                record.fuelDepletionTime
            )
        )
    }
}

private fun checkAlarmStatus(context: Context, record: eCoalRecord){
    if (record.mode == eCoalModes.Alarm) {
        sendAlarmNotification(
            context,
            context.resources.getInteger(R.integer.notif_id_mode_sts_alarm),
            context.getString(R.string.mode_alarm_title),
            context.getString(R.string.mode_alarm_description)
        )
    }
    else if (record.mode == eCoalModes.Manual) {
        sendWarningNotification(
            context,
            context.resources.getInteger(R.integer.notif_id_mode_sts_warning),
            context.getString(R.string.mode_manual_title),
            context.getString(R.string.mode_manual_description)
        )
    }
}

private fun checkConnection(context: Context, record: eCoalRecord, conSts: ConnectionStates, prefs: eCoalPrefs) {

    val ts = LocalDateTime.parse(record.timestamp.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
    val now = LocalDateTime.now() //.format(DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm"))
    val hoursElapsed = ts.until(now, ChronoUnit.HOURS)


    if ((hoursElapsed >= prefs.timeout) || (conSts != ConnectionStates.Connected)) {
        sendWarningNotification(
            context,
            context.resources.getInteger(R.integer.notif_id_coms_sts_warning),
            context.getString(R.string.connection_timeout_title),
            context.getString(R.string.connection_timeout_description).format(hoursElapsed)
        )
    }
    else {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            cancel(context.resources.getInteger(R.integer.notif_id_coms_sts_warning))
        }
    }

}

private fun processNotifications(context: Context, record: eCoalRecord, conSts: ConnectionStates, appWidgetId: Int) {
    val prefs = eCoalPrefs(context, appWidgetId)
    checkFuelLevel(context, record, prefs)
    checkAlarmStatus(context, record)
    checkConnection(context, record, conSts, prefs)
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.e_coal_helper_wdgt)

    val adr = AlwaysDataReader()

    val resultLast: ResultSet? = adr.readLastState()

    if (resultLast != null){
        val lastRecord = eCoalDecoder(resultLast).getLatestRecord()
        setConnectionStatus(views, adr.connectionStatus)
        views.setTextViewText(R.id.FuelLevel, lastRecord.fuelLevel.toString())
        views.setTextViewText(R.id.LastRead, String.format(context.getString(R.string.widget_timestamp),
            lastRecord.timestamp,
            lastRecord.timestamp,
            lastRecord.timestamp,
            lastRecord.timestamp,
            lastRecord.timestamp)
        )

        views.setTextViewText(R.id.LoadTime, String.format(context.getString(R.string.widget_timestamp),
            lastRecord.nextFuelTime,
            lastRecord.nextFuelTime,
            lastRecord.nextFuelTime,
            lastRecord.nextFuelTime,
            lastRecord.nextFuelTime)
        )
        views.setTextViewText(R.id.State, lastRecord.mode.toString())

        processNotifications(context, lastRecord, adr.connectionStatus, appWidgetId)


    }
    else {
        Log.wtf("UpdateAppWidget", "Last results are null!!")
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}