package com.recordlab.dailyscoop.ui.home.diary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.recordlab.dailyscoop.R
import com.recordlab.dailyscoop.data.ImagePath
import com.recordlab.dailyscoop.databinding.ActivityDiaryWriteBinding
import com.recordlab.dailyscoop.network.RetrofitClient
import com.recordlab.dailyscoop.network.enqueue
import com.recordlab.dailyscoop.network.request.RequestWriteDiary
import com.recordlab.dailyscoop.network.response.ResponseDiaryDetail
import com.recordlab.dailyscoop.ui.diary.DiaryDetailActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.util.*
import kotlin.collections.set


class DiaryWriteActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityDiaryWriteBinding
    private lateinit var backgroundLayout: CoordinatorLayout
    private lateinit var backgroundImage: ImageView
    private lateinit var contentText: EditText
    val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//            binding.ivWriteDiary.setImageURI(result.data?.data)
            if (result.data == null) {
                val message = R.string.no_selected_image
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                binding.ivWriteDiary.visibility = ImageView.GONE
            } else {
                Glide.with(backgroundLayout).load(result.data?.data).into(binding.ivWriteDiary)
                val uri = result.data?.data
                val imgPath = uri?.let { ImagePath().getPath(applicationContext, it) }
//                Log.d(DW_DEBUG_TAG, "????????? ?????? ?????? $imgPath")

                val file = File(imgPath)
//                Log.d(DW_DEBUG_TAG, "?????? : $file")

                val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                service.requestImageUrl( file = body).enqueue(

                    onSuccess = {
                        when (it.code()) {
                            in 200..206 ->  {
                                imageUrl = it.body()?.data
//                                Log.d(DW_DEBUG_TAG, "return image url -> ${imageUrl.toString()}")
                            }
                            in 400..499 -> {
//                                Log.d(DW_DEBUG_TAG, "${it.code()} : ${it.message()}" )
                            }
                        }
                    }, onError = {
//                        Log.d(DW_DEBUG_TAG, "?????? ?????? ??????~ ")
                    }, onFail = {
//                        Log.d(DW_DEBUG_TAG, "??????, ??????!")
                    }
                )


            }

        }
    private val DW_DEBUG_TAG = "DiaryWrite_DEBUG>>"

    private val service = RetrofitClient.service
    private val header = mutableMapOf<String, String?>()

    private lateinit var sharedPref: SharedPreferences

    var writeDate: String? = null
    var diaryContent: String? = null
    var emotions: List<String>? = null
    var theme: String? = "paper_white"
    private var imageUrl: String? = null
    var emotionCnt: Int = 0
    private lateinit var emotionType: List<RadioButton>
    private lateinit var response: ResponseDiaryDetail

    private val content_permission_code = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiaryWriteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent.hasExtra("date")) {
            writeDate = intent.getStringExtra("date")
        } else {
            writeDate = intent.getStringExtra("date")
        }


//        Log.d("????????? ?????? ??????", "$writeDate")
        sharedPref = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        header["Content-type"] = "application/json"
        header["Authorization"] = sharedPref.getString("token", "token")
//        Log.d(DW_DEBUG_TAG, "?????? ??? : ${header["Authorization"]}")
        val image = findViewById<ImageView>(R.id.iv_write_diary)
        val toolbar = binding.tbDiaryWrite.toolbar
        toolbar.background.alpha = 0
        // ????????? ????????? ???????????? ???????????? ???.
        // val token = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)

        backgroundLayout = binding.clDiaryWrite
        backgroundImage = binding.ivBackground
        contentText = binding.etWriteDiary

        if (intent.hasExtra("response")) {
            response = intent.getSerializableExtra("response") as ResponseDiaryDetail

            emotionCnt = response.emotions?.size!!
            for (emotion in response.emotions!!) {
                if (emotion == "angry") {
                    binding.emotionAngry.isSelected = true
                } else if (emotion == "anxious") {
                    binding.emotionAnxious.isSelected = true
                } else if (emotion == "relax") {
                    binding.emotionRelax.isSelected = true
                } else if (emotion == "fun") {
                    binding.emotionFun.isSelected = true
                } else if (emotion == "joy") {
                    binding.emotionJoy.isSelected = true
                } else if (emotion == "sound") {
                    binding.emotionSound.isSelected = true
                } else if (emotion == "excitement") {
                    binding.emotionExcitement.isSelected = true
                } else if (emotion == "bored") {
                    binding.emotionBored.isSelected = true
                } else if (emotion == "sad") {
                    binding.emotionSad.isSelected = true
                } else if (emotion == "tired") {
                    binding.emotionTired.isSelected = true
                } else if (emotion == "nervous") {
                    binding.emotionNervous.isSelected = true
                } else if (emotion == "happy") {
                    binding.emotionHappy.isSelected = true
                }
            }

            contentText.setText(response.content)

            if(response.image != "default"){
                Glide.with(backgroundLayout).load(response.image).into(binding.ivWriteDiary)
                imageUrl = response.image
                binding.ivWriteDiary.visibility = ImageView.VISIBLE
            }
        }

        image.setOnClickListener(this)
        binding.chipPaperWhite.setOnClickListener(this)
        binding.chipPaperIvory.setOnClickListener(this)
        binding.chipPaperBlack.setOnClickListener(this)
        binding.chipWindow.setOnClickListener(this)
        binding.chipSkyDay.setOnClickListener(this)
        binding.chipSkyNight.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        emotionType = mutableListOf(
            binding.emotionAngry,
            binding.emotionRelax,
            binding.emotionFun,
            binding.emotionAnxious,
            binding.emotionJoy,
            binding.emotionSound,
            binding.emotionExcitement,
            binding.emotionBored,
            binding.emotionSad,
            binding.emotionTired,
            binding.emotionNervous,
            binding.emotionHappy
        )

        binding.emotionAngry.setOnClickListener {
            if( binding.emotionAngry.isSelected){
                emotionCnt--
                (!binding.emotionAngry.isSelected).also {
                    binding.emotionAngry.isSelected = it
                }
            }else {
                if(binding.emotionAngry.isEnabled && emotionCnt < 3){
                    emotionCnt++
                    (!binding.emotionAngry.isSelected).also {
                        binding.emotionAngry.isSelected = it
                    }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionRelax.setOnClickListener {
            if (binding.emotionRelax.isSelected) { // ?????? ??????.
                emotionCnt--
                (!binding.emotionRelax.isSelected).also { binding.emotionRelax.isSelected = it }
            } else { // ?????? ????????????.
                if (binding.emotionRelax.isEnabled && emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionRelax.isSelected).also { binding.emotionRelax.isSelected = it }
                } else {
                    emotionWarning()
                }
            }

            saveButtonCheck()
        }

        binding.emotionFun.setOnClickListener {
            if (binding.emotionFun.isSelected) {
                emotionCnt--
                (!binding.emotionFun.isSelected).also { binding.emotionFun.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionFun.isSelected).also { binding.emotionFun.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionJoy.setOnClickListener {
            if (binding.emotionJoy.isSelected) {
                emotionCnt--
                (!binding.emotionJoy.isSelected).also { binding.emotionJoy.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionJoy.isSelected).also { binding.emotionJoy.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionSound.setOnClickListener {
            if (binding.emotionSound.isSelected) {
                emotionCnt--
                (!binding.emotionSound.isSelected).also { binding.emotionSound.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionSound.isSelected).also { binding.emotionSound.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionExcitement.setOnClickListener {
            if (binding.emotionExcitement.isSelected) {
                emotionCnt--
                (!binding.emotionExcitement.isSelected).also {
                    binding.emotionExcitement.isSelected = it
                }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionExcitement.isSelected).also {
                        binding.emotionExcitement.isSelected = it
                    }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionBored.setOnClickListener {
            if (binding.emotionBored.isSelected) {
                emotionCnt--
                (!binding.emotionBored.isSelected).also { binding.emotionBored.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionBored.isSelected).also { binding.emotionBored.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionSad.setOnClickListener {
            if (binding.emotionSad.isSelected) {
                emotionCnt--
                (!binding.emotionSad.isSelected).also { binding.emotionSad.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionSad.isSelected).also { binding.emotionSad.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionNervous.setOnClickListener {
            if (binding.emotionNervous.isSelected) {
                emotionCnt--
                (!binding.emotionNervous.isSelected).also { binding.emotionNervous.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionNervous.isSelected).also {
                        binding.emotionNervous.isSelected = it
                    }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionTired.setOnClickListener {
            if (binding.emotionTired.isSelected) {
                emotionCnt--
                (!binding.emotionTired.isSelected).also { binding.emotionTired.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionTired.isSelected).also { binding.emotionTired.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionAnxious.setOnClickListener {
            if (binding.emotionAnxious.isSelected) {
                emotionCnt--
                (!binding.emotionAnxious.isSelected).also { binding.emotionAnxious.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionAnxious.isSelected).also {
                        binding.emotionAnxious.isSelected = it
                    }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        binding.emotionHappy.setOnClickListener {
            if (binding.emotionHappy.isSelected) {
                emotionCnt--
                (!binding.emotionHappy.isSelected).also { binding.emotionHappy.isSelected = it }
            } else {
                if (emotionCnt < 3) {
                    emotionCnt++
                    (!binding.emotionHappy.isSelected).also { binding.emotionHappy.isSelected = it }
                } else {
                    emotionWarning()
                }
            }
            saveButtonCheck()
        }

        setSupportActionBar(toolbar)

        val getActionBar = supportActionBar
        if (getActionBar != null) {
            getActionBar.setDisplayShowCustomEnabled(true)  // custom?????? ??????
            getActionBar.setDisplayShowTitleEnabled(false)
            getActionBar.setDisplayHomeAsUpEnabled(true)
            getActionBar.setDisplayShowHomeEnabled(true)
            getActionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_black_24)
        }
        //???????????? ?????? ??? ??????????????? ????????? ????????? ????????????.????????? ???????????? ??????.

        binding.tbDiaryWrite.toolbarId.text = writeDate
        binding.tbDiaryWrite.toolbarId.setTextColor(Color.argb(0xCC, 0x30, 0x30, 0x30))
        if (intent.hasExtra("response")) {
            when (response.theme) {
                "paper_white" -> {
                    Glide.with(backgroundLayout).load(R.drawable.theme_paper_white).into(backgroundImage)
                    theme = "paper_white"
                    setTextColor(0)
                }
                "paper_ivory" -> {
                    Glide.with(backgroundLayout).load(R.drawable.theme_paper_ivory).into(backgroundImage)
                    theme = "paper_ivory"
                    setTextColor(0)
                }
                "paper_dark" -> {
                    Glide.with(backgroundLayout).load(R.drawable.theme_paper_dark).into(backgroundImage)
                    theme = "paper_dark"
                    setTextColor(1)
                }
                "sky_day" -> {
                    Glide.with(backgroundLayout).load(R.drawable.theme_sky_day_bright).into(backgroundImage)
                    setTextColor(0)
                    theme = "sky_day"
                }
                "sky_night" -> {
                    Glide.with(backgroundLayout).load(R.drawable.theme_sky_night).into(backgroundImage)
                    setTextColor(1)
                    theme = "sky_night"
                }
                "window" -> {
                    Glide.with(backgroundLayout).load(R.drawable.theme_window).into(backgroundImage)
                    setTextColor(1)
                    theme = "window"
                }
            }
        } else {
            Glide.with(backgroundLayout).load(R.drawable.theme_paper_white).into(backgroundImage)
        }

        binding.btnSave.isEnabled = false

        binding.ivButtonGallery.setOnClickListener(this)

        postDiary()

    }

    private fun saveButtonCheck() {
        binding.btnSave.isClickable = true
        if (emotionCnt > 0 && binding.etWriteDiary.length() > 0){
            // ?????? ?????? ???????????????.
            binding.btnSave.isEnabled = true

        } else {
            // ?????? ?????? ????????????
            binding.btnSave.isEnabled = false
        }
    }

    private fun activateEmotionButton( status :Boolean) {
        binding.emotionAngry.isActivated = status
        binding.emotionAnxious.isActivated = status
        binding.emotionBored.isActivated = status
        binding.emotionFun.isActivated = status
        binding.emotionHappy.isActivated = status
        binding.emotionJoy.isActivated = status
        binding.emotionSad.isActivated = status
        binding.emotionSad.isActivated = status
        binding.emotionSound.isActivated = status
        binding.emotionTired.isActivated = status
        binding.emotionRelax.isActivated = status
        binding.emotionNervous.isActivated = status
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_button_gallery -> {
                val readPermission = ContextCompat.checkSelfPermission(
                    this@DiaryWriteActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                if (readPermission == PackageManager.PERMISSION_GRANTED) { // ?????? ?????? ?????? ??????,
                   selectPhoto()
                } else {
                    ActivityCompat.requestPermissions(
                        this@DiaryWriteActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        content_permission_code
                    )

                }
            }
            R.id.iv_write_diary -> {

            }
            R.id.chip_paper_white -> {
                Glide.with(backgroundLayout).load(R.drawable.theme_paper_white).into(backgroundImage)
                theme = "paper_white"
                setTextColor(0)
            }
            R.id.chip_paper_ivory -> {
                Glide.with(backgroundLayout).load(R.drawable.theme_paper_ivory).into(backgroundImage)
                theme = "paper_ivory"
                setTextColor(0)
            }
            R.id.chip_paper_black -> {
                Glide.with(backgroundLayout).load(R.drawable.theme_paper_dark).into(backgroundImage)
                theme = "paper_dark"
                setTextColor(1)
            }
            R.id.chip_window -> {
                Glide.with(backgroundLayout).load(R.drawable.theme_window).into(backgroundImage)
                setTextColor(1)
                theme = "window"
            }
            R.id.chip_sky_day -> {
                Glide.with(backgroundLayout).load(R.drawable.theme_sky_day_bright).into(backgroundImage)
                setTextColor(0)
                theme = "sky_day"
            }
            R.id.chip_sky_night -> {
                Glide.with(backgroundLayout).load(R.drawable.theme_sky_night).into(backgroundImage)
                setTextColor(1)
                theme = "sky_night"
            }
            R.id.btn_save -> {
                // ???????????? ?????? ?????????
                if (binding.btnSave.isEnabled) { // ??????????, ????????? ?????? ??? ??????, ?????? ?????? ??????, ?????? (?????? paper_white)
                    service.requestWriteDiary(
                        header = header,
                        diary = RequestWriteDiary(
                            content = binding.etWriteDiary.text.toString(),
                            image = imageUrl?: "default",
                            emotions = getEmotionAsList(),
                            theme = theme!!,
                            date = writeDate
                        )
                    ).enqueue(
                        onSuccess = {
                            when (it.code()) {
                                in 200..206 -> {
//                                    Log.d(DW_DEBUG_TAG, "???????????? ${it.code()}")
                                    // ?????? ?????? ?????? ?????? ??????????????? ?????????.
                                    val intent = Intent(this, DiaryDetailActivity::class.java)
                                    intent.putExtra("diaryDate", writeDate)
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(intent)
                                    finish()
                                }
                                401 -> {
//                                    Log.d(DW_DEBUG_TAG, "?????? ??????.")
                                }
                                in 400..499 -> {
//                                    Log.d(DW_DEBUG_TAG, "?????? ?????? : ?????? ${it.code()} ????????? : ${it.message()}")
                                }
                                in 500.. 599 -> {
//                                    Log.d(DW_DEBUG_TAG, "?????? ??????")
                                }
                            }

                        }, onFail = {

                        }, onError = {

                        }
                    )

                } else {
                    if (emotionCnt > 0){
                        Toast.makeText(applicationContext, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            // ?????? ???????????? ???
            content_permission_code -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectPhoto()
                } else {
                    val message = R.string.require_permission_message
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun selectPhoto(){
        val intent =
            Intent(Intent.ACTION_PICK) // Intent.ACTION_PICK ?????? CONTENT_TYPE, image/*?????? ???????????? ????????? ??????.
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.type = "image/*"
//                intent.action = Intent.ACTION_GET_CONTENT // ??? ????????? ?????? ?????? ???????????? ?????? ???????????? ??????.
        getContent.launch(intent)
        binding.ivWriteDiary.visibility = ImageView.VISIBLE
    }

    fun postDiary() {
        binding.etWriteDiary.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                saveButtonCheck()
            }
        })
    }

    fun emotionWarning() {
        val message = getString(R.string.toast_emotion_limit_3)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun getEmotionAsList(): List<String> {
        val selectedEmo = arrayListOf<String>()
        for (emo in emotionType) {
            if (emo.isSelected) {
                val st = StringTokenizer(emo.contentDescription.toString(), "_")
//                Log.d(DW_DEBUG_TAG, "id >> ${emo.id.toString()}")
                st.nextToken()
                selectedEmo.add(st.nextToken())
            }
        }
        return selectedEmo
    }


    fun setTextColor(mode: Int) {
        when (mode) {
            0 -> {
                val darkColor: Int = Color.argb(0xCC, 0x30, 0x30, 0x30)
                colorMode(darkColor)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_black_24)
            }
            1 -> { // ?????? ?????????
                val brightColor = Color.argb(0xCC, 0xDB, 0xDB, 0xDB)
                colorMode(brightColor)
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            }
        }
    }

    fun colorMode(color: Int) {
        binding.tbDiaryWrite.toolbarId.setTextColor(color)
        contentText.setTextColor(color)
        binding.emotionAngry.setTextColor(color)
        binding.emotionAnxious.setTextColor(color)
        binding.emotionBored.setTextColor(color)
        binding.emotionExcitement.setTextColor(color)
        binding.emotionFun.setTextColor(color)
        binding.emotionSad.setTextColor(color)
        binding.emotionHappy.setTextColor(color)
        binding.emotionJoy.setTextColor(color)
        binding.emotionTired.setTextColor(color)
        binding.emotionSad.setTextColor(color)
        binding.emotionSound.setTextColor(color)
        binding.emotionRelax.setTextColor(color)
        binding.emotionNervous.setTextColor(color)
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_save, menu)
        return true
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}