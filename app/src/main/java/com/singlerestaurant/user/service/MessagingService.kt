package com.singlerestaurant.user.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.*
import com.singlerestaurant.user.utils.Constants

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

import org.json.JSONException
import org.json.JSONObject


class MessagingService : FirebaseMessagingService() {
    private val REQUEST_CODE = 1
    private var NOTIFICATION_ID = 6578

    @SuppressLint("WrongThread")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            Log.e("getData", Gson().toJson(remoteMessage.toString()))
            val `object` = JSONObject(Gson().toJson(remoteMessage.notification))
            Log.e("isPeram", `object`.toString())
            Log.d("NotificationData",Gson().toJson(remoteMessage.data))

            val title = `object`.getString("title")
            val message = `object`.getString("body")

            val data = remoteMessage.data
            if (data["type"].toString() == "promotion") {
                if (!data["category_id"].isNullOrEmpty()) {
                    val intent = Intent(this, ActSubCategoryProducts::class.java)
                    intent.putExtra("category_id", data["category_id"].toString())
                    intent.putExtra("category_name", data["category_name"].toString())
                    intent.putExtra("type", data["type"].toString())
                    intent.putExtra(Constants.FromNotification,true)
                    showNotifications(getString(R.string.app_name), title, message, intent)

                }else if (!data["item_id"].isNullOrEmpty()) {
                    val intent = Intent(this, ActFoodDetails::class.java)
                    intent.putExtra("foodItemId", data["item_id"].toString())
                    intent.putExtra("type", data["type"].toString())

                    intent.putExtra(Constants.FromNotification,true)

                    showNotifications(getString(R.string.app_name), title, message, intent)
                }else {
                    val intent = Intent(this, DashboardActivity::class.java)

                    showNotifications(getString(R.string.app_name), title, message, intent)
                }
            } else if (data["type"].toString() == "order") {
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("order_id", data["order_id"].toString())
                intent.putExtra("type", data["type"].toString())

                showNotifications(getString(R.string.app_name), title, message, intent)
            }else if(data["type"].toString()=="wallet")
            {
                val intent = Intent(this, ActTransactionHistory::class.java)
                intent.putExtra("type", data["type"].toString())

                intent.putExtra(Constants.FromNotification,true)

                showNotifications(getString(R.string.app_name), title, message, intent)
            }else
            {
                val intent = Intent(this, DashboardActivity::class.java)
                showNotifications(getString(R.string.app_name), title, message, intent)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    private fun showNotifications(title: String, title1: String, message: String, intent: Intent) {
        val channelId = "channel-01"
        val channelName = "Channel Name"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(channelId, channelName, importance)
            manager.createNotificationChannel(mChannel)
            val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(getIcon())
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentTitle(title1)
                .setContentText(message)
            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addNextIntent(intent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            mBuilder.setContentIntent(resultPendingIntent)
            manager.notify(NOTIFICATION_ID, mBuilder.build())
        } else {
            val pendingIntent = PendingIntent.getActivity(
                this, REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentText(message)
                .setContentTitle(title1)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setSmallIcon(getIcon())
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        }
        NOTIFICATION_ID++
    }

    private fun getIcon(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) R.drawable.ic_small_notification else R.drawable.ic_small_notification
    }
}