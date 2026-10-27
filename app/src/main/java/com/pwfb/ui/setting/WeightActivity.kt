package com.pwfb.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.pwfb.R
import androidx.appcompat.app.AppCompatActivity
import com.pwfb.databinding.ActivityWeightBinding
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.util.applySystemBarInsetsAsPadding
import com.pwfb.util.enablePwfbEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WeightActivity: AppCompatActivity() {

    private lateinit var binding: ActivityWeightBinding
    private val viewModel: WeightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enablePwfbEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityWeightBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.clName.applySystemBarInsetsAsPadding()

        /**
         * 체중을 입력해야 Next 버튼 활성화
         */
        binding.etWeight.addTextChangedListener {
            binding.btNext.isEnabled = !binding.etWeight.text.isNullOrBlank()
        }

        /**
         * 배경 클릭 시 키보드 내리기
         */
        binding.clName.setOnClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.etWeight.windowToken, 0)
        }

        /**
         * Next 버튼 클릭
         */
        binding.btNext.setOnClickListener {
            val weight = binding.etWeight.text.toString()
            if((weight.toDoubleOrNull() ?: 0.0) <= 0.0) {
                Toast.makeText(this, R.string.weight_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.setWeight(weight)
        }

        viewModel.weightObserve.observe(this) {
            if(it is PwfbResultEntity.Success) {
                val intent = Intent(applicationContext, DayActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }
        }
    }
}