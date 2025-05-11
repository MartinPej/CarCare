package com.cse3mad.carcare.ui.guides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cse3mad.carcare.databinding.FragmentGuidesBinding
import android.content.Intent
import android.net.Uri
import com.cse3mad.carcare.R

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

        val imageView = root.findViewById<ImageView>(R.id.tyreGuideThumbnail)
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=joBmbh0AGSQ"))
            startActivity(intent)
        }

        val oilImageView = root.findViewById<ImageView>(R.id.oilGuideThumbnail)
        oilImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=L4lc0meYkDY"))
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}