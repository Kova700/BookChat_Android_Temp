package com.kova700.bookchat.core.fcm.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kova700.bookchat.core.fcm.chat.ChatNotificationWorker
import com.kova700.bookchat.core.fcm.forced_logout.ForcedLogoutWorker
import com.kova700.bookchat.core.fcm.renew_fcm_token.RenewFcmTokenWorker
import com.kova700.bookchat.core.fcm.service.model.FcmMessage
import com.kova700.bookchat.core.fcm.service.model.PushType
import com.kova700.bookchat.util.Constants.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		Log.d(TAG, "FCMService: onNewToken() - token :$token")
		startRenewFcmTokenWorker(token)
	}

	override fun onMessageReceived(message: RemoteMessage) {
		super.onMessageReceived(message)
		handleMessage(message)
	}

	private fun handleMessage(message: RemoteMessage) {
		val messageBody = message.data["body"]
		val hashMap = Json.decodeFromString<HashMap<String, String>>(messageBody ?: return)
		when (hashMap["pushType"]) {
			PushType.LOGIN.toString() -> startForcedLogoutWorker()
			PushType.CHAT.toString() -> handleChatMessage(Json.decodeFromString<FcmMessage>(messageBody))
		}
	}

	private fun handleChatMessage(fcmMessage: FcmMessage) {
		startChatNotificationWorker(
			channelId = fcmMessage.body.channelId,
			chatId = fcmMessage.body.chatId
		)
	}

	private fun startChatNotificationWorker(channelId: Long, chatId: Long) {
		ChatNotificationWorker.start(
			context = applicationContext,
			channelId = channelId,
			chatId = chatId
		)
	}

	private fun startRenewFcmTokenWorker(fcmTokenString: String) {
		RenewFcmTokenWorker.start(
			context = applicationContext,
			fcmTokenString = fcmTokenString
		)
	}

	private fun startForcedLogoutWorker() {
		ForcedLogoutWorker.start(applicationContext)
	}

}