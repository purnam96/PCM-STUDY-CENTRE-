package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.ScheduledStudySession
import com.example.model.Subject

/**
 * Helper to manage Notification Channels, AlarmManager scheduling,
 * and push notifications for student study review sessions.
 */
object StudyNotificationHelper {

    const val CHANNEL_ID = "study_reminders_channel"
    const val CHANNEL_NAME = "Study Session Reminders"
    const val CHANNEL_DESCRIPTION = "Alerts for upcoming scheduled BOSEM Physics & Chemistry revision sessions"

    const val ACTION_STUDY_REMINDER = "com.example.ACTION_STUDY_REMINDER"
    const val EXTRA_SESSION_ID = "extra_session_id"
    const val EXTRA_SUBJECT = "extra_subject"
    const val EXTRA_CHAPTER = "extra_chapter"
    const val EXTRA_TOPIC = "extra_topic"
    const val EXTRA_DURATION = "extra_duration"

    private const val TAG = "StudyNotificationHelper"

    /**
     * Initializes the high-priority notification channel.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Schedules a push notification alarm for an upcoming study session.
     */
    fun scheduleSessionNotification(context: Context, session: ScheduledStudySession) {
        createNotificationChannel(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Compute alarm trigger time (scheduled time minus lead reminder minutes)
        val leadTimeMillis = session.reminderMinutesBefore * 60 * 1000L
        val triggerAtMillis = session.scheduledEpochMillis - leadTimeMillis

        // If the calculated reminder is already in the past, do not schedule
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Trigger time is in the past for session ${session.id}, skipping schedule.")
            return
        }

        val intent = Intent(context, StudyNotificationReceiver::class.java).apply {
            action = ACTION_STUDY_REMINDER
            putExtra(EXTRA_SESSION_ID, session.id)
            putExtra(EXTRA_SUBJECT, session.subject.name)
            putExtra(EXTRA_CHAPTER, session.chapterName)
            putExtra(EXTRA_TOPIC, session.topicTitle)
            putExtra(EXTRA_DURATION, session.durationMinutes)
        }

        val requestCode = session.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for ${session.chapterName} at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback alarm schedule: ${ex.message}")
            }
        }
    }

    /**
     * Cancels an existing scheduled notification for a session.
     */
    fun cancelSessionNotification(context: Context, sessionId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyNotificationReceiver::class.java).apply {
            action = ACTION_STUDY_REMINDER
        }
        val requestCode = sessionId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled notification for session $sessionId")
        }
    }

    /**
     * Directly triggers an immediate push notification for a session.
     * Allows students and testers to instantly verify push notifications.
     */
    fun triggerImmediateNotification(context: Context, session: ScheduledStudySession) {
        createNotificationChannel(context)

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingTapIntent = PendingIntent.getActivity(
            context,
            session.id.hashCode() + 1,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subjectEmoji = if (session.subject == Subject.PHYSICS) "⚡" else "🧪"
        val title = "$subjectEmoji ${session.subject.displayName} Review: ${session.chapterName}"
        val body = if (session.topicTitle.isNotBlank()) {
            "Time to study '${session.topicTitle}' (${session.durationMinutes} min session). Tap to begin!"
        } else {
            "Your ${session.durationMinutes}-minute study session is ready. Tap to start reviewing!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pcm_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingTapIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(session.id.hashCode(), notification)
            Log.d(TAG, "Immediate study notification posted for session ${session.id}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission denied: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification: ${e.message}")
        }
    }
}
