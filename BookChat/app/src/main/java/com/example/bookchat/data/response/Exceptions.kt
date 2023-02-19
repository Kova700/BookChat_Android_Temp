package com.example.bookchat.data.response

import java.io.IOException

class BadRequestException(errorBody :String?) : IOException(errorBody)
class ResponseBodyEmptyException(errorBody :String?) : IOException(errorBody)
class ForbiddenException(errorBody :String?) : IOException(errorBody)
class NeedToSignUpException(errorBody :String?) : IOException(errorBody)
class NetworkIsNotConnectedException : IOException("network is not connected.")
class IdTokenDoseNotExistException : IOException("Saved IdToken does not exist")
class TokenDoseNotExistException : IOException("Saved BookChatToken does not exist")
class NickNameDuplicateException(errorBody :String?) : IOException(errorBody)
class KakaoLoginFailException(errorBody :String?) : IOException(errorBody)
class KakaoLoginUserCancelException(errorBody :String?) : IOException(errorBody)
class NeedToGoogleLoginException : IOException("need to google login")
class TokenRenewalFailException : IOException("TokenRenewal request is fail")
class BookReportDoseNotExistException : IOException("BookReport does not exist")