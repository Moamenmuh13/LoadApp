package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


private sealed class CustomURL(val url: String, val title: String) {
    companion object {
        object URL_GLIDE :
            CustomURL("https://github.com/bumptech/glide/archive/master.zip", "Glide Repo")

        object URL_RETROFIT :
            CustomURL("https://github.com/square/retrofit/archive/master.zip", "Retrofit Repo")

        object URL_LOAD_STATUS_APP : CustomURL(
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
            "Udacity Load status app"
        )

        object URL_EMPTY : CustomURL("", "")
    }
}


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var downloadID: Long = 0
    private var isDownloadComplete: Boolean = false

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var selectedCustomURL: CustomURL
    private val NOTIFICATION_ID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannel()

        setupRadioGroupOptions()
        custom_button.setOnClickListener {
            //Handel the Download Btn
            downloadRepoFromUrl()
        }
    }

    private fun setupRadioGroupOptions() {
        download_option_radio_group.setOnCheckedChangeListener { _, radioBtn ->
            selectedCustomURL = when (radioBtn) {
                R.id.glide_radio_btn -> {
                    CustomURL.Companion.URL_GLIDE
                }
                R.id.loadApp_radio_btn -> {
                    CustomURL.Companion.URL_LOAD_STATUS_APP
                }
                R.id.retrofit_radio_btn -> {
                    CustomURL.Companion.URL_RETROFIT
                }
                else -> {
                    CustomURL.Companion.URL_EMPTY
                }
            }

        }
    }

    private fun downloadRepoFromUrl() {
        if (::selectedCustomURL.isInitialized) {
            download()
        } else {
            Toast.makeText(this, "Please select an item to download", Toast.LENGTH_SHORT).show()
        }
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                isDownloadComplete = true
                custom_button.buttonState = ButtonState.Completed
                createNotification()
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(selectedCustomURL.url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if (cursor.moveToFirst()) {
            when (cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) {
                DownloadManager.STATUS_FAILED -> {
                    isDownloadComplete = false
                    custom_button.buttonState = ButtonState.Clicked
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    isDownloadComplete = true
                    custom_button.buttonState = ButtonState.Clicked
                }
            }
        }
    }


    fun createNotification() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        val detailIntent =
            startingActivityFromNotification()
        pendingIntent(detailIntent)
        val contentPendingIntent = contentPendingIntent(detailIntent)
        notificationAction(contentPendingIntent)
        notificationBuilder(contentPendingIntent)
    }

    private fun startingActivityFromNotification(): Intent {
        return Intent(applicationContext, DetailActivity::class.java).putExtra(
            "title",
            selectedCustomURL.title
        )
            .putExtra("completeStatus", isDownloadComplete)
    }

    private fun notificationAction(contentPendingIntent: PendingIntent?) {
        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            resources.getString(R.string.notification_description),
            contentPendingIntent
        )
    }

    private fun pendingIntent(detailIntent: Intent) {
        pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(detailIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        } as PendingIntent
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun contentPendingIntent(detailIntent: Intent): PendingIntent? {
        return PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun notificationBuilder(contentPendingIntent: PendingIntent?) {
        val builder = NotificationCompat.Builder(
            applicationContext, CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(
                applicationContext
                    .getString(R.string.notification_title)
            )
            .setContentText(applicationContext.getString(R.string.notification_description))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "LoadAppNotificationChannel",
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            Log.d(TAG, "createChannel: ${notificationChannel.description}")

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "NotificationChannelId"
    }
}
