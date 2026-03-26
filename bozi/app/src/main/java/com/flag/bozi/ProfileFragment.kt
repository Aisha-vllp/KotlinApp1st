package com.flag.bozi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flag.bozi.databinding.FragmentProfileBinding
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка настроек
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        // Обновление серии дней подряд
        updateSeries()

        // Подсветка текущего дня недели
        highlightCurrentDay()

        // Загрузка прогресса всех игр
        loadGameProgress()
    }

    private fun updateSeries() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)
        val lastDate = prefs.getLong("last_login", 0L)
        var series = prefs.getInt("series_days", 0)

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        if (lastDate != 0L) {
            val lastLogin = Calendar.getInstance().apply { timeInMillis = lastDate }

            series = if (isSameDay(lastLogin, yesterday)) {
                series + 1
            } else if (!isSameDay(lastLogin, today)) {
                1
            } else {
                series
            }
        } else {
            series = 1
        }

        prefs.edit().apply {
            putLong("last_login", today.timeInMillis)
            putInt("series_days", series)
            apply()
        }

        binding.seriesCount.text = "$series дней подряд"
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun highlightCurrentDay() {
        val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Воскресенье = 1
        val days = listOf(
            binding.dayMon, binding.dayTue, binding.dayWed,
            binding.dayThu, binding.dayFri, binding.daySat, binding.daySun
        )

        days.forEachIndexed { index, textView ->
            if (index == (todayIndex + 5) % 7) { // Преобразуем так, чтобы Пн = 0
                textView.setBackgroundResource(R.drawable.bg_selected_day)
                textView.setTextColor(resources.getColor(android.R.color.white))
            } else {
                textView.setBackgroundResource(R.drawable.bg_day_default)
                textView.setTextColor(resources.getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun loadGameProgress() {
        val prefs = requireContext().getSharedPreferences("profile_prefs", 0)

        // Миллионер прогресс
        val millionerScore = prefs.getInt("millioner_score", 0)
        binding.millionerProgress.text = "$millionerScore / 1 000 000 ₽"

        // Логика / Tetris
        val tetrisScore = prefs.getInt("tetris_score", 0)
        binding.logicProgress.text = "$tetrisScore / 100 000 очков"

        // 2048
        val game2048Best = prefs.getInt("game2048_best", 0)
        binding.focusProgress.text = "$game2048Best / 2048"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
