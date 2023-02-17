package com.example.bookchat.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookchat.App
import com.example.bookchat.data.IdToken
import com.example.bookchat.data.Token
import com.example.bookchat.data.response.IdTokenDoseNotExistException
import com.example.bookchat.data.response.TokenDoseNotExistException
import com.example.bookchat.utils.Constants.TAG
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

object DataStoreManager {
    private var cachedIdToken : IdToken? = null

    private const val DATASTORE_KEY = "DATASTORE_KEY"
    private const val BOOKCHAT_TOKEN_KEY = "BOOKCHAT_TOKEN_KEY"
    private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY_KEY"

    private val Context.dataStore by preferencesDataStore(DATASTORE_KEY)
    private val bookChatTokenKey = stringPreferencesKey(BOOKCHAT_TOKEN_KEY)
    private val searchHistoryKey = stringPreferencesKey(SEARCH_HISTORY_KEY)

    //북챗 토큰 (추후 암호화 추가)
    suspend fun getBookchatToken(): Token {
        val tokenString = readDataStore().firstOrNull()?.get(bookChatTokenKey)
        if (tokenString.isNullOrBlank()) throw TokenDoseNotExistException()
        val token = Gson().fromJson(tokenString, Token::class.java)
        Log.d(TAG, "DataStoreManager: getBookchatToken() - token : $token")
        return token
    }

    suspend fun saveBookchatToken(token: Token) {
        token.accessToken = "Bearer ${token.accessToken}"
        val tokenString = Gson().toJson(token)
        Log.d(TAG, "DataStoreManager: saveBookchatToken() - called")
        setDataStore(bookChatTokenKey, tokenString)
    }

    fun getIdToken(): IdToken {
        return this.cachedIdToken ?: throw IdTokenDoseNotExistException()
    }

    fun saveIdToken(idToken: IdToken) {
        this.cachedIdToken = idToken
    }

    suspend fun getSearchHistory() :MutableList<String>? {
        val historyString = readDataStore().firstOrNull()?.get(searchHistoryKey)
        if (historyString.isNullOrBlank()) return null
        val historyList = Gson().fromJson(historyString, Array<String>::class.java).toMutableList()
        return historyList
    }

    suspend fun saveSearchHistory(searchKeyWord : String){
        val oldHistoryList = getSearchHistory()
        val newHistoryList =  mutableListOf(searchKeyWord)
        newHistoryList.addAll(oldHistoryList ?: listOf())
        val historyString : String = Gson().toJson(newHistoryList)
        setDataStore(searchHistoryKey,historyString)
    }

    suspend fun clearSearchHistory(){
        removeDataStore(searchHistoryKey)
    }

    suspend fun overWriteHistory(historyList : List<String>){
        val historyString : String = Gson().toJson(historyList)
        setDataStore(searchHistoryKey,historyString)
    }

    suspend fun deleteBookchatToken() {
        removeDataStore(bookChatTokenKey)
    }

    suspend fun <T : Any> removeDataStore(
        preferencesKey: Preferences.Key<T>,
    ) {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
    }

    suspend fun <T : Any> setDataStore(
        preferencesKey: Preferences.Key<T>,
        value: T
    ) {
        App.instance.applicationContext.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    private fun readDataStore(): Flow<Preferences> {
        return App.instance.applicationContext.dataStore.data
            .catch { exception -> if (exception is IOException) emit(emptyPreferences()) }
    }

}