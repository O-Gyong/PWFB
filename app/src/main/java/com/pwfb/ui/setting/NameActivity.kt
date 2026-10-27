package com.pwfb.ui.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.appcompat.app.AppCompatActivity
import com.pwfb.databinding.ActivityNameBinding
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.util.applySystemBarInsetsAsPadding
import com.pwfb.util.enablePwfbEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NameActivity: AppCompatActivity() {

    private lateinit var binding: ActivityNameBinding
    private val viewModel: NameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enablePwfbEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.clName.applySystemBarInsetsAsPadding()

        /**
         * 이름을 입력해야 Next 버튼 활성화
         */
        binding.etName.addTextChangedListener {
            binding.btNext.isEnabled = !binding.etName.text.isNullOrBlank()
        }

        /**
         * 배경 클릭 시 키보드 내리기
         */
        binding.clName.setOnClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.etName.windowToken, 0)
        }

        /**
         * Next 버튼 클릭
         */
        binding.btNext.setOnClickListener {
            viewModel.setName(binding.etName.text.toString())
        }

        viewModel.nameObserve.observe(this) {
            if(it is PwfbResultEntity.Success) {
                val intent = Intent(applicationContext, WeightActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }
        }
    }
}