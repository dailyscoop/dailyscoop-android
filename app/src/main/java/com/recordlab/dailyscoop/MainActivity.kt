package com.recordlab.dailyscoop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.recordlab.dailyscoop.databinding.ActivityMainBinding
import com.recordlab.dailyscoop.ui.auth.SignInActivity
import com.recordlab.dailyscoop.ui.profile.SignOutDialogInterface
import com.recordlab.dailyscoop.ui.profile.lock.AppLock
import com.recordlab.dailyscoop.ui.profile.lock.AppLockConst
import com.recordlab.dailyscoop.ui.profile.lock.AppPasswordActivity

class MainActivity : AppCompatActivity(), SignOutDialogInterface {
    var lock = true
    private val finishIntervalTime : Long = 3000
    private var backPressedTime : Long = 0
    lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val navView = binding.navView

        init()

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_history,
                R.id.navigation_profile
            )
        )
        val toolbar = binding.tbMain
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowCustomEnabled(true)  // custom?????? ??????
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        toolbar.elevation = 2F // ?????? ?????? ????????????.

        setupActionBarWithNavController(navController, appBarConfiguration)
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth
        val currentUser = auth.currentUser

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AppLockConst.UNLOCK_PASSWORD ->
                if (resultCode == Activity.RESULT_OK) {
                    lock = false
                }
        }
    }

    override fun onStart() {
        super.onStart()
        if (lock && AppLock(this).isPassLockSet()) {
            val intent = Intent(this, AppPasswordActivity::class.java).apply {
                putExtra(AppLockConst.type, AppLockConst.UNLOCK_PASSWORD)
            }
            startActivityForResult(intent, AppLockConst.UNLOCK_PASSWORD)
        }
    }

//    override fun onPause() {
//        super.onPause()
//        if (AppLock(this).isPassLockSet()) {
//            lock = true
//        }
//    }

    private fun init() {
        lock = if (AppLock(this).isPassLockSet()) true else false
    }

    // ???????????? ??????????????? ?????? ?????? ??????
    override fun okBtnClicked() {
        auth = Firebase.auth
        val currentUser = auth.currentUser

        Toast.makeText(this, "???????????? ???????????????", Toast.LENGTH_SHORT).show()

        // ?????? ??????
        val pref = getSharedPreferences("TOKEN", 0)
        val edit = pref.edit() // ????????????(??????, ??????)
        if (pref.getString("social", "false") == "true") {
            if (currentUser != null) {
                Firebase.auth.signOut()
                mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.unlink(user.providerId)
                    edit.remove("social")
                    Log.d("???????????? ??????", "${user?.providerId}")
                }
            }
        }
        edit.remove("token") // key, value
        edit.clear()
        edit.apply() // ?????? ??????
        edit.commit()
        //val to = pref.getString("token","??????")
        //Toast.makeText(this, "?????? : $to", Toast.LENGTH_SHORT).show()

        // ????????? ??????????????? ??????
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        if (0 <= intervalTime && finishIntervalTime >= intervalTime) {
            super.onBackPressed();
        }
        else {
            backPressedTime = tempTime;
            val message = R.string.app_backbutton_finish
            Toast.makeText(getApplicationContext(), "'??????'????????? ?????? ??? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
        }
    }

}