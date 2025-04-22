package com.cse3mad.carcare.ui.mechanic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cse3mad.carcare.databinding.FragmentMechanicBinding

class MechanicFragment : Fragment() {

    private var _binding: FragmentMechanicBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mechanicViewModel =
            ViewModelProvider(this).get(MechanicViewModel::class.java)

        _binding = FragmentMechanicBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMechanic
        mechanicViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 