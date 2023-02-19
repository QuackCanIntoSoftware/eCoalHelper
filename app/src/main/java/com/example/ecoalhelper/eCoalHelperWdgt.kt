package com.example.ecoalhelper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.TextViewCompat
import java.sql.ResultSet

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
            deleteTitlePref(context, appWidgetId)
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

private fun createNotificationChannel(context: Context) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "kurwa"//R.string.alarm_channel_name
        val descriptionText =  "Mac" //R.string.alarm_channel_description
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("eCoalChannel", name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}


internal fun processNotifications(context: Context, record: eCoalRecord) {
    // createNotificationChannel(context)

    val builder = NotificationCompat.Builder(context, context.getString(R.string.notif_alarm_channel_id))
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(context.getString(R.string.notif_alarm_channel_name))
        .setContentText("Alarm")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val builder2 = NotificationCompat.Builder(context, context.getString(R.string.notif_warn_channel_id))
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(context.getString(R.string.notif_warn_channel_name))
        .setContentText("Warning")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        notify(1, builder.build())
        notify(2, builder2.build())
    }

}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.e_coal_helper_wdgt)

    val adr = AlwaysDataReader()

    val resultLast: ResultSet? = adr.readLastState()

    if (resultLast != null){
        val lastRecord = eCoalDecoder(resultLast).getLatestRecord()
        // views.setTextViewText(R.id.appwidget_text, lastRecord.timestamp.toString())
        setConnectionStatus(views, adr.connectionStatus)
        views.setTextViewText(R.id.FuelLevel, lastRecord.fuelLevel.toString())
        views.setTextViewText(R.id.LastRead, lastRecord.timestamp.toString())
        views.setTextViewText(R.id.LoadTime, lastRecord.nextFuelTime.toString())
        views.setTextViewText(R.id.State, lastRecord.mode.toString())

        processNotifications(context, lastRecord)


    }
    else {
        Log.wtf("UpdateAppWidget", "Last results are null!!")
    }



    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}