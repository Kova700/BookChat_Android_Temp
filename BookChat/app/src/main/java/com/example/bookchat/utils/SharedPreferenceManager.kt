package com.example.bookchat.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.bookchat.App
import com.example.bookchat.utils.Constants.SEARCH_HISTORY_PREF
import com.example.bookchat.utils.Constants.SEARCH_HISTORY_PREF_KEY
import com.example.bookchat.utils.Constants.TOKEN_PREF_KEY
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

/*삭제 예정*/
object SharedPreferenceManager {

    //EncryptedSharedPreferences 인스턴스 생성
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    val tokenPref = EncryptedSharedPreferences.create(
        "encryptedShared",
        masterKeyAlias,
        App.instance,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    //SharedPreferences 인스턴스 생성
    private var historyPref :SharedPreferences =
        App.instance.getSharedPreferences(SEARCH_HISTORY_PREF, Context.MODE_PRIVATE)

    fun saveToken(token :String){
        val editor = tokenPref.edit()
        editor.putString(TOKEN_PREF_KEY,"Bearer $token")
        editor.apply()
    }

    fun getToken() :String?{
        return  tokenPref.getString(TOKEN_PREF_KEY,"")
    }

    fun isTokenEmpty() :Boolean{
        return tokenPref.getString(TOKEN_PREF_KEY,"").isNullOrEmpty()
    }

    fun getSearchHistory() :ArrayList<String>{
        val searchHistoryListString = historyPref.getString(SEARCH_HISTORY_PREF_KEY,"") ?: ""
        var searchHistoryList = ArrayList<String>()

        if (searchHistoryListString.isNotEmpty()){
            // 문자열 -> 배열로 변환
            searchHistoryList = Gson().
            fromJson(searchHistoryListString, Array<String>::class.java).
            toMutableList() as ArrayList<String>
        }
        return searchHistoryList.toCollection(ArrayList())
    }

    fun setSearchHistory(searchKeyWord : String){
        val searchHistoryList = getSearchHistory() //저장되어있는 검색기록 불러옴
        val temp = ArrayList<String>()
        temp.add(searchKeyWord)
        temp.addAll(searchHistoryList)

        // 배열 -> 문자열(Json)으로 변환
        val newHistoryString : String = Gson().toJson(temp)

        val editor = historyPref.edit()
        editor.putString(SEARCH_HISTORY_PREF_KEY,newHistoryString)
        editor.apply()
    }

    fun clearSearchHistory(){

        val editor = historyPref.edit()
        editor.clear()
        editor.apply()
    }

    fun overWriteHistory(searchHistoryList : ArrayList<String>){
        val sublist = ArrayList<String>()
        searchHistoryList.forEach {
            if (it.isNotEmpty()) sublist.add(it)
        }

        // 배열 -> 문자열(Json)으로 변환
        val searchHistoryListString : String = Gson().toJson(sublist)

        val editor = historyPref.edit()
        editor.putString(SEARCH_HISTORY_PREF_KEY,searchHistoryListString)
        editor.apply()
    }
}