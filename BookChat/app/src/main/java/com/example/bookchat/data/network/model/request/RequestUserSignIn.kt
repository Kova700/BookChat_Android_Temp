package com.example.bookchat.data.network.model.request

import com.example.bookchat.data.network.model.OAuth2ProviderNetwork
import com.google.gson.annotations.SerializedName

data class RequestUserSignIn(
	@SerializedName("fcmToken")
	val fcmToken: String,
	@SerializedName("deviceToken")
	val deviceToken: String,
	@SerializedName("approveChangingDevice")
	val approveChangingDevice: Boolean,
	@SerializedName("oauth2Provider")
	val oauth2Provider: com.example.bookchat.data.network.model.OAuth2ProviderNetwork
)