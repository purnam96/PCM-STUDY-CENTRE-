package com.example.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

/**
 * BroadcastReceiver triggered by AlarmManager when a scheduled study session reminder fires.
 * Constructs and delivers a heads-up system push notification.
 */
class StudyNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != StudyNotificationHelper.ACTION_STUDY_REMINDER) return

        val sessionId = intent.getStringExtra(StudyNotificationHelper.EXTRA_SESSION_ID) ?: return
        val subject = intent.getStringExtra(StudyNotificationHelper.EXTRA_SUBJECT) ?: "Physics"
        val chapter = intent.getStringExtra(StudyNotificationHelper.EXTRA_CHAPTER) ?: "Chapter Revision"
        val topic = intent.getStringExtra(StudyNotificationHelper.EXTRA_TOPIC) ?: ""
        val duration = intent.getIntExtra(StudyNotificationHelper.EXTRA_DURATION, 45)

        Log.d(TAG, "Study reminder alarm fired for session $sessionId: $chapter ($subject)")

        StudyNotificationHelper.createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            sessionId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subjectEmoji = if (subject.contains("PHYSICS", ignoreCase = true)) "⚡" else "🧪"
        val title = "$subjectEmoji $subject Reminder: $chapter"
        val body = if (topic.isNotBlank()) {
            "Ready for '$topic'? Your $duration-minute study session is starting now. Tap to begin!"
        } else {
            "Time to review $chapter! Your $duration-minute study session is scheduled now."
        }

        val notification = NotificationCompat.Builder(context, StudyNotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pcm_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(sessionId.hashCode(), notification)
            Log.d(TAG, "Delivered notification for session $sessionId")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Notifications permission missing", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification", e)
        }
    }

    companion object {
        private const val TAG = "StudyNotifReceiver"
    }
}
