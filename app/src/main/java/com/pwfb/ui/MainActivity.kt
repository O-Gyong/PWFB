package com.pwfb.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdRequest
import com.pwfb.R
import androidx.appcompat.app.AppCompatActivity
import com.pwfb.databinding.ActivityMainBinding
import com.pwfb.ui.home.HomeFragment
import com.pwfb.ui.nutrition.NutritionFragment
import com.pwfb.ui.profile.ProfileFragment
import com.pwfb.ui.training.TrainingFragment
import com.pwfb.util.applyMainInsets
import com.pwfb.util.enablePwfbEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    /**
     * 하단 탭 정보. 탭 Fragment는 한 번만 만들어 두고, 전환할 때는 보이기/숨기기
     * (입력 중인 값, 스크롤 상태가 탭을 오가도 유지)
     */
    private class Tab(
        @IdRes val menuId: Int,
        @StringRes val titleRes: Int,
        val tag: String,
        val create: () -> Fragment
    )

    private val tabs = listOf(
        Tab(R.id.item_navi_home, R.string.home, "tab_home") { HomeFragment() },
        Tab(R.id.item_navi_nutrition, R.string.nutrition, "tab_nutrition") { NutritionFragment() },
        Tab(R.id.item_navi_training, R.string.training, "tab_training") { TrainingFragment() },
        Tab(R.id.item_navi_profile, R.string.profile, "tab_profile") { ProfileFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enablePwfbEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.clMain.applyMainInsets(binding.llBottom)

        binding.adView.loadAd(AdRequest.Builder().build())

        val selected = if (savedInstanceState == null) {
            addTabs()
            tabs.first()
        } else {
            // 화면 회전 등으로 복원된 경우: FragmentManager가 Fragment와 숨김 상태를 복원하므로 보이는 탭만 찾는다
            tabs.firstOrNull { supportFragmentManager.findFragmentByTag(it.tag)?.isHidden == false }
                ?: tabs.first()
        }
        binding.mainBnv.selectedItemId = selected.menuId
        binding.tvTitle.setText(selected.titleRes)

        binding.mainBnv.setOnItemSelectedListener { item ->
            tabs.firstOrNull { it.menuId == item.itemId }?.let { showTab(it) }
            true
        }
        // 이미 선택된 탭을 다시 누르면 아무것도 하지 않는다
        binding.mainBnv.setOnItemReselectedListener { }
    }

    private fun addTabs() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            tabs.forEachIndexed { index, tab ->
                val fragment = tab.create()
                add(R.id.main_fcv, fragment, tab.tag)
                if (index != 0) {
                    hide(fragment)
                    // 숨겨진 탭은 STARTED 아래로 내려 화면 갱신(collect)을 멈춘다
                    setMaxLifecycle(fragment, Lifecycle.State.CREATED)
                }
            }
        }
    }

    private fun showTab(target: Tab) {
        binding.tvTitle.setText(target.titleRes)
        hideKeyboard()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // 들어오는 탭은 살짝 떠오르며, 나가는 탭은 빠르게 사라진다
            setCustomAnimations(R.anim.tab_enter, R.anim.tab_exit)
            tabs.forEach { tab ->
                val fragment = supportFragmentManager.findFragmentByTag(tab.tag) ?: return@forEach
                if (tab === target) {
                    show(fragment)
                    setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
                } else {
                    hide(fragment)
                    setMaxLifecycle(fragment, Lifecycle.State.CREATED)
                }
            }
        }
    }

    private fun hideKeyboard() {
        val focused = currentFocus ?: return
        focused.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(focused.windowToken, 0)
    }

    /**
     * 입력칸 바깥을 터치했을 때만 키보드 내리기 (포커스가 빠지면서 입력값이 저장된다)
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused is EditText) {
                val bounds = Rect()
                focused.getGlobalVisibleRect(bounds)
                if (!bounds.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboard()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
