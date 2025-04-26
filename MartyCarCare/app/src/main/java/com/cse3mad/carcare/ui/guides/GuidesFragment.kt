package com.cse3mad.carcare.ui.guides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cse3mad.carcare.databinding.FragmentGuidesBinding

class GuidesFragment : Fragment() {

    private var _binding: FragmentGuidesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val guidesViewModel =
            ViewModelProvider(this).get(GuidesViewModel::class.java)

        _binding = FragmentGuidesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGuides
        guidesViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 