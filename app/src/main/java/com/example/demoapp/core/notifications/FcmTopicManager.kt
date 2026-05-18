package com.example.demoapp.core.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

object FcmTopicManager {

    private const val TAG = "FcmTopicManager"

    private const val MODERATORS_TOPIC = "moderators"

    fun subscribeToModerators() = subscribe(MODERATORS_TOPIC)
    fun unsubscribeFromModerators() = unsubscribe(MODERATORS_TOPIC)

    fun subscribeToUserInbox(userId: String) = subscribe(inboxTopic(userId))
    fun unsubscribeFromUserInbox(userId: String) = unsubscribe(inboxTopic(userId))

    fun subscribeToUserPublications(authorId: String) = subscribe(publicationsTopic(authorId))
    fun unsubscribeFromUserPublications(authorId: String) = unsubscribe(publicationsTopic(authorId))

    private fun inboxTopic(userId: String) = "user_${userId}_inbox"
    private fun publicationsTopic(authorId: String) = "user_${authorId}_publications"

    private fun subscribe(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d(TAG, "Subscribed to $topic")
                else Log.e(TAG, "Failed to subscribe to $topic", task.exception)
            }
    }

    private fun unsubscribe(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d(TAG, "Unsubscribed from $topic")
                else Log.e(TAG, "Failed to unsubscribe from $topic", task.exception)
            }
    }
}
