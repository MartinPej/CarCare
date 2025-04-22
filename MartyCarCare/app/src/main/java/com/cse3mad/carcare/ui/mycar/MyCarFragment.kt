package com.cse3mad.carcare.ui.mycar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cse3mad.carcare.databinding.FragmentMyCarBinding

class MyCarFragment : Fragment() {

    private var _binding: FragmentMyCarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myCarViewModel =
            ViewModelProvider(this).get(MyCarViewModel::class.java)

        _binding = FragmentMyCarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyCar
        myCarViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 