package com.recordlab.dailyscoop.ui.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.recordlab.dailyscoop.R
import com.recordlab.dailyscoop.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private val REQ_STORAGE_PERMISSION = 9
    private val REQ_GALLERY = 10

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK){
//            it.data?.data?.let{ uri ->
//                binding.profile.setImageURI(uri)
//            }
            binding.profile.setImageURI(it.data?.data)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val profileViewModel : ProfileViewModel by viewModels()
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val textView: TextView = root.findViewById(R.id.tv_profile_test)
        profileViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        // 프로필 변경(카메라 아이콘) 버튼 클릭
        val profileImageBtnClicked = root.findViewById<View>(R.id.profile2)
        profileImageBtnClicked.setOnClickListener {
            val readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)

            if(readPermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQ_STORAGE_PERMISSION)
            }else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                intent.type = "image/*"
                //this.startActivityForResult(intent, REQ_GALLERY)
                startForResult.launch(intent)
            }
        }

        val friendBtnClicked = root.findViewById<View>(R.id.bg1)
        friendBtnClicked.setOnClickListener {
            val intent = Intent(activity, ProfileFriendActivity::class.java)
            startActivity(intent)
        }

        val accountBtnClicked = root.findViewById<View>(R.id.bg2)
        accountBtnClicked.setOnClickListener {
            val intent = Intent(activity, ProfileAccountActivity::class.java)
            startActivity(intent)
        }

        val noticeBtnClicked = root.findViewById<View>(R.id.bg3)
        noticeBtnClicked.setOnClickListener {
            val intent = Intent(activity, ProfileNoticeActivity::class.java)
            startActivity(intent)
        }

        val lockBtnClicked = root.findViewById<View>(R.id.bg4)
        lockBtnClicked.setOnClickListener {
            val intent = Intent(activity, ProfileLockActivity::class.java)
            startActivity(intent)
        }

        val ddayBtnClicked = root.findViewById<View>(R.id.bg5)
        ddayBtnClicked.setOnClickListener {
            val intent = Intent(activity, ProfileDdayActivity::class.java)
            startActivity(intent)
        }

        val logoutBtnClicked = root.findViewById<View>(R.id.bg6)
        logoutBtnClicked.setOnClickListener{
            //Toast.makeText(getActivity()?.getApplicationContext(),"이것은 log out.", Toast.LENGTH_SHORT).show();

            val builder = AlertDialog.Builder(activity)
            val dialogView = layoutInflater.inflate(R.layout.custom_dialog_withdraw, null)

            builder.setView(dialogView)
                .show()
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}