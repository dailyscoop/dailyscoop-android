package com.recordlab.dailyscoop.ui.dashboard

import android.content.Intent
import android.view.*
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recordlab.dailyscoop.R
import com.recordlab.dailyscoop.data.DiaryData
import com.recordlab.dailyscoop.databinding.FragmentDashboardBinding
import com.recordlab.dailyscoop.databinding.FragmentHomeBinding
import com.recordlab.dailyscoop.network.RetrofitClient
import com.recordlab.dailyscoop.network.RetrofitClient.service
import com.recordlab.dailyscoop.network.enqueue
import com.recordlab.dailyscoop.ui.search.SearchResultActivity
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.security.auth.callback.Callback

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    val diaryData = mutableListOf<DiaryData>()
    private lateinit var sharedPref: SharedPreferences

    lateinit var dashboardListAdapter: DashboardListAdapter
    lateinit var dashboardGridAdapter: DashboardGridAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProviders.of(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root = binding.root
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        setHasOptionsMenu(true) // ??? ??? ?????? ?????? ????????????.

        sharedPref = requireActivity().getSharedPreferences("TOKEN", Context.MODE_PRIVATE)

        // RecyclerView
        val listRecyclerView = root.findViewById<RecyclerView>(R.id.rv_dashboard_list)
        listRecyclerView.layoutManager = LinearLayoutManager(context)
        listRecyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        val gridRecyclerView = root.findViewById<RecyclerView>(R.id.rv_dashboard_grid)
        gridRecyclerView.layoutManager = GridLayoutManager(context, 4)
        
        // observer
        dashboardViewModel.items.observe(viewLifecycleOwner, Observer {
            dashboardListAdapter = DashboardListAdapter(it)
            dashboardGridAdapter = DashboardGridAdapter(it)
            listRecyclerView.adapter = dashboardListAdapter
            gridRecyclerView.adapter = dashboardGridAdapter
        })
        
        // ???????????? ??????
        val layoutBtn = root.findViewById<View>(R.id.iv_nav_gallery_grid)
        layoutBtn.setOnClickListener {
            if (it.isSelected) {
                listRecyclerView.visibility = View.INVISIBLE
                gridRecyclerView.visibility = View.VISIBLE
            } else {
                listRecyclerView.visibility = View.VISIBLE
                gridRecyclerView.visibility = View.INVISIBLE
            }
            it.isSelected = !it.isSelected
        }

        // ?????? ?????? ??????
        val selectedDate : TextView = root.findViewById(R.id.tv_nav_gallery_date)
        var nowYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
        var nowMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"))
        selectedDate.text = "$nowYear.$nowMonth"

        loadData(root, "$nowYear-$nowMonth-01")

        // ???????????? ?????? ??????
        val datePickBtn = root.findViewById<View>(R.id.iv_nav_gallery_calender)
        datePickBtn.setOnClickListener {

            val dialog = AlertDialog.Builder(context).create()
            val edialog : LayoutInflater = LayoutInflater.from(context)
            val mView : View = edialog.inflate(R.layout.custom_dialog_datepicker, null)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

            val year : com.shawnlin.numberpicker.NumberPicker = mView.findViewById(R.id.np_datepicker_year_picker)
            val month : com.shawnlin.numberpicker.NumberPicker = mView.findViewById(R.id.np_datepicker_month_picker)
            val cancel : Button = mView.findViewById(R.id.btn_datepicker_dlg_cancel)
            val save : Button = mView.findViewById(R.id.btn_datepicker_dlg_ok)

            // ?????? ????????? ??????
            year.wrapSelectorWheel = false
            month.wrapSelectorWheel = false

            // editText ?????? ??????
            year.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            month.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

            // ?????????, ????????? ??????
            year.minValue = 2010
            year.maxValue = 2030
            month.minValue = 1
            month.maxValue = 12

            // ????????? ??????
            year.value = nowYear.toInt()
            month.value = nowMonth.toInt()

            // ?????? ?????? ?????????
            cancel.setOnClickListener {
                dialog.dismiss()
                dialog.cancel()
            }

            // ?????? ?????? ?????????
            save.setOnClickListener {
                nowYear = year.value.toString()
                nowMonth = if (month.value < 10) "0${month.value}" else month.value.toString()
                selectedDate.text = "$nowYear.$nowMonth"

                loadData(root, "$nowYear-$nowMonth-01")

                // ?????? ?????? ??? ????????? ????????????
                dialog.dismiss()
                dialog.cancel()
            }

            dialog.setView(mView)
            dialog.create()
            dialog.show()
        }

        val button_sort = root.findViewById<LinearLayout>(R.id.button_nav_sort)
        button_sort.setOnClickListener {
            diaryData.reverse()
            dashboardListAdapter.notifyDataSetChanged()
            dashboardGridAdapter.notifyDataSetChanged()

            val nav_sort_text = root.findViewById<TextView>(R.id.nav_sort_text)
            if (nav_sort_text.text == "?????????") {
                nav_sort_text.text = "????????? ???"
            } else {
                nav_sort_text.text = "?????????"
            }
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(activity, SearchResultActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadData(view: View, date: String) {
        val header = mutableMapOf<String, String?>()

        header["Content-type"] = "application/json; charset=UTF-8"
        header["Authorization"] = sharedPref.getString("token", "token")

        if (header["Authorization"] == "token") {

        } else {
            service.requestGetCalendar(header = header, date = date, type = "monthly", sort = -1).enqueue(
                onSuccess = {
                    when (it.code()) {
                        in 200..299 -> {
                            // ?????? ??????
//                            Log.d("?????? ??????", it.body()!!.data[0].content)
                            diaryData.clear()
                            diaryData.addAll(it.body()!!.data)
                            dashboardViewModel.items.postValue(diaryData)
                        }
                        400 -> {
                            // 400 ??????
                        }
                        else -> {

                        }
                    }
                },
                onError = {
                },
                onFail = {
                }

            )
        }

    }

}