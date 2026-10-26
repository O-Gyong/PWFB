package com.pwfb.ui.profile

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pwfb.R
import com.pwfb.databinding.DialogStrategyBinding


class StrategyDialogFragment: BottomSheetDialogFragment() {
    private var _binding: DialogStrategyBinding? = null
    private val binding get() = _binding!!

    // yes=true, no=false
    private var isFull: Boolean? = null
    private var isDry: Boolean? = null
    private var isDehydration: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStrategyBinding.inflate(inflater, container, false)
        setOnClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutParams = view.layoutParams
        layoutParams?.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            getWindowHeight(),
            resources.displayMetrics
        ).toInt()
        binding.clStrategy.layoutParams = layoutParams
    }

    private fun setOnClickListener() {
        binding.clChooseFull.setOnClickListener {
            binding.clChoose.visibility = View.GONE
            binding.clAmIFull.visibility = View.VISIBLE
        }

        binding.clChooseDry.setOnClickListener {
            binding.clChoose.visibility = View.GONE
            binding.clAmIDry.visibility = View.VISIBLE
        }

        binding.tvFullYes.setOnClickListener {
            isFull = true
            binding.clAmIFull.visibility = View.GONE
            if(isDry == true) {
                binding.tvResult.text = getString(R.string.stabilize_condition)
            } else {
                binding.clAmIDry.visibility = View.VISIBLE
            }
        }

        binding.tvFullNo.setOnClickListener {
            isFull = false
            binding.clAmIFull.visibility = View.GONE
            if(isDry == true) {
                binding.tvResult.text = getString(R.string.stabilize_condition)
            } else {
                binding.clAmIDry.visibility = View.VISIBLE
            }
        }

        binding.tvDryYes.setOnClickListener {
            isDry = true
            binding.clAmIDry.visibility = View.GONE
            if(isFull != null && isDehydration == null) {
                binding.tvResult.text = getString(R.string.stabilize_condition)
            }else {
                binding.clAmIFull.visibility = View.VISIBLE
            }
        }

        binding.tvDryNo.setOnClickListener {
            isDry = false
            binding.clAmIDry.visibility = View.GONE
            binding.clContinueDehydration.visibility = View.VISIBLE
        }

        binding.tvFlatYes.setOnClickListener {
            binding.clAmIFlat.visibility = View.GONE
            binding.tvResult.text = getString(R.string.consider_flat)
        }

        binding.tvFlatNo.setOnClickListener {
            binding.clAmIFlat.visibility = View.GONE
            binding.tvResult.text = getString(R.string.stabilize_condition)
        }

        binding.tvContinue.setOnClickListener {
            isDehydration = true
            binding.clContinueDehydration.visibility = View.GONE
            binding.clAmIDry.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun getWindowHeight(): Float {
        val display = resources.displayMetrics
        return display.heightPixels / display.density * 0.5f
    }
}