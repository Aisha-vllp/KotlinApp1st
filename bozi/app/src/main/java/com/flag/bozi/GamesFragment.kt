package com.flag.bozi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flag.bozi.databinding.FragmentGamesBinding
import com.flag.bozi.tetris.TetrisActivity

class GamesFragment : Fragment() {

    private var _binding: FragmentGamesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamesBinding.inflate(inflater, container, false)

        // Обработчик нажатия на кнопку btnMultiplication
        binding.btnMultiplication.setOnClickListener {
            val intent = Intent(requireContext(), GameActivity::class.java)
            startActivity(intent)
        }

        binding.btn2048.setOnClickListener {
            val intent = Intent(requireContext(), Game2048Activity::class.java)
            startActivity(intent)
        }

        binding.btntetris.setOnClickListener {
            val intent = Intent(requireContext(), TetrisActivity::class.java)
            startActivity(intent)
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
