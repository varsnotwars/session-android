package org.session.libsession.messaging.utilities

import android.content.Context
import android.text.SpannableString
import org.session.libsession.R
import org.session.libsession.messaging.MessagingConfiguration
import org.session.libsession.messaging.messages.control.ExpirationTimerUpdate
import org.session.libsession.utilities.ExpirationUtil
import org.session.libsignal.service.api.messages.SignalServiceGroup

object UpdateMessageBuilder {

    fun buildGroupUpdateMessage(context: Context, groupInfo: SignalServiceGroup, sender: String? = null, isOutgoing: Boolean = false): String {
        val updateType = groupInfo.type
        var message: String = ""
        if (!isOutgoing && sender == null) return message
        val senderName: String? = if (!isOutgoing) {
            MessagingConfiguration.shared.storage.getDisplayNameForRecipient(sender!!) ?: sender
        } else { sender }

        when (updateType) {
            SignalServiceGroup.Type.NEW_GROUP -> {
                message = if (isOutgoing) {
                    context.getString(R.string.MessageRecord_you_created_a_new_group)
                } else {
                    context.getString(R.string.MessageRecord_s_added_you_to_the_group, senderName)
                }
            }
            SignalServiceGroup.Type.NAME_CHANGE -> {
                message = if (isOutgoing) {
                    context.getString(R.string.MessageRecord_you_renamed_the_group_to_s, groupInfo.name.get())
                } else {
                    context.getString(R.string.MessageRecord_s_renamed_the_group_to_s, senderName, groupInfo.name.get())
                }
            }
            SignalServiceGroup.Type.MEMBER_ADDED -> {
                val members = groupInfo.members.get().joinToString(", ") {
                    MessagingConfiguration.shared.storage.getDisplayNameForRecipient(it) ?: it
                }
                message = if (isOutgoing) {
                    context.getString(R.string.MessageRecord_you_added_s_to_the_group, members)
                } else {
                    context.getString(R.string.MessageRecord_s_added_s_to_the_group, senderName, members)
                }
            }
            SignalServiceGroup.Type.MEMBER_REMOVED -> {
                val storage = MessagingConfiguration.shared.storage
                val userPublicKey = storage.getUserPublicKey()!!
                // 1st case: you are part of the removed members
                message = if (userPublicKey in groupInfo.members.get()) {
                    if (isOutgoing) {
                        context.getString(R.string.MessageRecord_left_group)
                    } else {
                        context.getString(R.string.MessageRecord_you_were_removed_from_the_group)
                    }
                } else {
                    // 2nd case: you are not part of the removed members
                    val members = groupInfo.members.get().joinToString(", ") {
                        storage.getDisplayNameForRecipient(it) ?: it
                    }
                    if (isOutgoing) {
                        context.getString(R.string.MessageRecord_you_removed_s_from_the_group, members)
                    } else {
                        context.getString(R.string.MessageRecord_s_removed_s_from_the_group, senderName, members)
                    }
                }
            }
            SignalServiceGroup.Type.QUIT -> {
                message = if (isOutgoing) {
                    context.getString(R.string.MessageRecord_left_group)
                } else {
                    context.getString(R.string.ConversationItem_group_action_left, senderName)
                }
            }
            else -> {
                message = context.getString(R.string.MessageRecord_s_updated_group, senderName)
            }
        }
        return message
    }

    fun buildExpirationTimerMessage(context: Context, duration: Int, sender: String? = null, isOutgoing: Boolean = false): String {
        val seconds = (duration!! / 1000)
        if (!isOutgoing && sender == null) return ""
        val senderName: String? = if (!isOutgoing) {
            MessagingConfiguration.shared.storage.getDisplayNameForRecipient(sender!!) ?: sender
        } else { sender }
        return if (seconds <= 0) {
            if (isOutgoing) context.getString(R.string.MessageRecord_you_disabled_disappearing_messages)
            else context.getString(R.string.MessageRecord_s_disabled_disappearing_messages, senderName)
        } else {
            val time = ExpirationUtil.getExpirationDisplayValue(context, seconds)
            if (isOutgoing)context.getString(R.string.MessageRecord_you_set_disappearing_message_time_to_s, time)
            else context.getString(R.string.MessageRecord_s_set_disappearing_message_time_to_s, senderName, time)
        }
    }

    fun buildDataExtractionMessage(): String {
        return ""
    }

}