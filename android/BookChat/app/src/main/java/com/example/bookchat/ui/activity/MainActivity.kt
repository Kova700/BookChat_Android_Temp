package com.example.bookchat.ui.activity

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.databinding.ActivityMainBinding
import com.example.bookchat.ui.fragment.*
import com.example.bookchat.utils.Constants.TOKEN_PATH
import com.example.bookchat.viewmodel.MainViewModel
import com.example.bookchat.viewmodel.ViewModelFactory
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProvider(this,ViewModelFactory()).get(MainViewModel::class.java)
        with(binding){
            lifecycleOwner =this@MainActivity
            activity = this@MainActivity
            viewModel = mainViewModel
        }
        changeFragment(HomeFragment())
        setBottomNavigation()
    }

    fun setBottomNavigation(){
        //이미 살아있는 Fragment 객체가 있으면 그 상태를 불러오는게 Best (스크롤 상태까지 그대로)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
                when(item.itemId){
                    R.id.home_navi_icon -> { changeFragment(HomeFragment()) }
                    R.id.bookshelf_navi_icon -> { changeFragment(BookShelfFragment()) }
                    R.id.search_navi_icon -> { changeFragment(SearchFragment()) }
                    R.id.chat_navi_icon -> { changeFragment(ChatFragment()) }
                    R.id.mypage_navi_icon -> { changeFragment(MyPageFragment()) }
                }
            true
        }
    }

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, fragment)
            .commit()
    }

    fun clickMenu() {
        with(binding){
//            if(drawerlayout.isDrawerOpen(Gravity.RIGHT)) {
//                drawerlayout.closeDrawer(Gravity.RIGHT)
//                return
//            }
//            drawerlayout.openDrawer(Gravity.RIGHT)
        }
    }
//    fun changePage(activityType: ActivityType) {
//        val targetActivity = when(activityType) {
//            ActivityType.bookShelfActivity -> { BookShelfActivity::class.java }
//            ActivityType.searchActivity -> { SearchActivity::class.java }
//        }
//        val intent = Intent(this, targetActivity)
//        startActivity(intent)
//    }
    fun clickSignOut(){
        val dialog = AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_Dialog)
        dialog.setTitle("정말 로그아웃하시겠습니까?")
            .setPositiveButton("취소",object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                }
            })
            .setNeutralButton("로그아웃",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    deleteToken()
                    finish()
                }
            })
            .setCancelable(true) //백버튼으로 닫히게 설정
            .show()
    }
    fun deleteToken(){
        val token = File(TOKEN_PATH)
        if (token.exists()) token.delete()
    }
    private fun getUserInfo(){
//        binding.profile.clipToOutline = true //프로필 라운딩
//        binding.userModel?.activityInitialization()
    }

}