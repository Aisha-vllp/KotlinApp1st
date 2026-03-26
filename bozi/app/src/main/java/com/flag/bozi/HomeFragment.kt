package com.flag.bozi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flag.bozi.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val maxProgress = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Загружаем прогресс бриллиантов
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val currentProgress = prefs.getInt("diamond_progress", 0)

        // Обновляем прогрессбар и текст
        binding.progressBar.max = maxProgress
        binding.progressBar.progress = currentProgress
        binding.tvProgress.text = "$currentProgress/$maxProgress"

        // Слушатели для категорий
        binding.easyCard.setOnClickListener {
            openQuestionActivity("easy")
        }

        binding.normalCard.setOnClickListener {
            openQuestionActivity("normal")
        }

        binding.hardCard.setOnClickListener {
            openQuestionActivity("hard")
        }

        return binding.root
    }

    private fun openQuestionActivity(difficulty: String) {
        val intent = Intent(requireContext(), QuestionActivity::class.java)
        intent.putExtra("difficulty", difficulty)
        intent.putExtra("level", 1) // если нужен уровень
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

