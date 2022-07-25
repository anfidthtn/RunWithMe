package com.ssafy.runwithme.view.login.join

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.ssafy.runwithme.R
import com.ssafy.runwithme.base.BaseFragment
import com.ssafy.runwithme.databinding.FragmentUserJoinBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserJoinFragment : BaseFragment<FragmentUserJoinBinding>(R.layout.fragment_user_join) {
    override fun init() {
        initSpinner() // 키와 몸무게 스피너 값 넣기
    }

    private fun initSpinner(){
        val heightList = Array(131) { i -> i + 120 }
        val weightList = Array(231) { i -> i + 20 }

        binding.spinnerHeight.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, heightList)
        binding.spinnerHeight.setSelection(30) // 초기 값 설정
        binding.spinnerHeight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                showToast((position + 120).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        binding.spinnerWeight.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, weightList)
        binding.spinnerWeight.setSelection(30) // 초기 값 설정
        binding.spinnerWeight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                showToast((position + 20).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

}