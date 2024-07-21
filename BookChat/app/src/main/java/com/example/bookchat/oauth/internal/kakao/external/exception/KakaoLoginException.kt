package com.example.bookchat.oauth.internal.kakao.external.exception

import java.io.IOException

class KakaoLoginClientCancelException : IOException()
class KakaoLoginFailException(errorBody: String?) : IOException(errorBody)