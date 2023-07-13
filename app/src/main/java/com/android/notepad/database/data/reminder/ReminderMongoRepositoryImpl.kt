package com.android.notepad.database.data.reminder

import android.content.Context
import android.util.Log
import com.android.notepad.R
import com.android.notepad.database.models.Note
import com.android.notepad.database.models.Reminder
import com.android.notepad.services.notification_service.NotificationData
import com.android.notepad.services.notification_service.NotificationScheduler
import com.android.notepad.services.statistics_service.StatisticsService
import dagger.hilt.android.qualifiers.ApplicationContext
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.LocalDateTime
import java.time.OffsetDateTime

class ReminderMongoRepositoryImpl(val realm: Realm, @ApplicationContext val context: Context) : ReminderMongoRepository {
    override fun getAllReminders(): Flow<List<Reminder>> {
        return realm.query<Reminder>()
            .sort("_id", Sort.DESCENDING)
            .asFlow()
            .map { it.list }
    }

    override fun getReminder(id: ObjectId): Reminder? {
        return realm.query<Reminder>(query = "_id == $0", id).first().find()
    }

    override fun getReminderForNote(noteId: ObjectId): Reminder? {
        return realm.query<Reminder>(query = "noteId == $0", noteId).first().find()
    }

    override suspend fun insertReminder(reminder: Reminder, note: Note) {
        realm.write { copyToRealm(reminder) }
        scheduleNotification(reminder, note)
        StatisticsService().incrementCreatedRemindersCount(context)
    }

    override suspend fun updateReminder(id: ObjectId, updateParams: (Reminder) -> Unit) {
        getReminder(id)?.also { currentReminder ->
            realm.writeBlocking {
                val queriedReminder = findLatest(currentReminder) ?: return@writeBlocking
                queriedReminder.apply {
                    updateParams(this)
                }
                updateNotification(id, queriedReminder)
            }
        }
    }

    override suspend fun updateOrCreateReminderForNotes(
        notes: List<Note>,
        updateParams: (Reminder) -> Unit
    ) {
        realm.writeBlocking {
            for (note in notes) {
                try {
                    var reminder = getReminderForNote(note._id)
                    if (reminder != null) {
                        val latest = findLatest(reminder) ?: continue
                        latest.let(updateParams)
                        reminder = latest
                    } else {
                        reminder = Reminder()
                        reminder.noteId = note._id
                        reminder.let(updateParams)
                        copyToRealm(reminder)
                        StatisticsService().incrementCreatedRemindersCount(context)
                    }
                    scheduleNotification(reminder, note)
                } catch (e: Exception) {
                    Log.d("ReminderMongoRepositoryImpl", "${e.message}")
                }
            }
        }
    }

    override suspend fun deleteReminder(id: ObjectId) {
        realm.write {
            val reminder = getReminder(id) ?: return@write
            try {
                findLatest(reminder)?.let { delete(it) }
                deleteNotification(id)
            } catch (e: Exception) {
                Log.d("ReminderMongoRepositoryImpl", "${e.message}")
            }
        }
    }

    override suspend fun deleteReminder(ids: List<ObjectId>) {
        realm.writeBlocking {
            for (id in ids) {
                val reminder = getReminder(id) ?: continue
                try {
                    findLatest(reminder)
                        ?.let { delete(it) }
                    deleteNotification(id)
                } catch (e: Exception) {
                    Log.d("ReminderMongoRepositoryImpl", "${e.message}")
                }
            }
        }
    }

    override suspend fun deleteReminderForNote(noteId: ObjectId){
        realm.write {
            val reminder = getReminderForNote(noteId) ?: return@write
            try {
                findLatest(reminder)?.let { delete(it) }
                deleteNotification(reminder._id)
            } catch (e: Exception) {
                Log.d("ReminderMongoRepositoryImpl", "${e.message}")
            }
        }
    }

    private fun deleteNotification(reminderId: ObjectId) {
        val notificationScheduler = NotificationScheduler(context)
        notificationScheduler.deleteNotification(reminderId.timestamp)
    }

    private fun updateNotification(
        reminderId: ObjectId,
        queriedReminder: Reminder
    ) {
        // Update notification
        val notificationScheduler = NotificationScheduler(context)
        notificationScheduler.updateNotificationTime(
            reminderId.timestamp,
            queriedReminder.date,
            queriedReminder.time,
            queriedReminder.repeat
        )
    }

    private fun scheduleNotification(
        reminder: Reminder,
        note: Note,
    ) {
        val notificationScheduler = NotificationScheduler(context)
        notificationScheduler.createNotificationChannel(NotificationScheduler.Companion.NotificationChannels.NOTECHANNEL)
        val localDateTime = LocalDateTime.of(reminder.date, reminder.time)
        val notificationData = NotificationData(
            note._id.toHexString(),
            reminder._id.timestamp,
            note.header,
            note.body,
            R.drawable.ic_notification,
            reminder.repeat,
            reminder._id.timestamp,
            localDateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli()
        )
        notificationScheduler.scheduleNotification(notificationData)
    }
}