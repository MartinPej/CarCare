package com.cse3mad.carcare.ui.guides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cse3mad.carcare.databinding.FragmentGuidesBinding
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

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

        // Tyre guide video
        binding.tyreGuideThumbnail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=joBmbh0AGSQ"))
            startActivity(intent)
        }

        // Oil guide video
        binding.oilGuideThumbnail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=L4lc0meYkDY"))
            startActivity(intent)
        }

        // Download tyre PDF
        binding.downloadTyrePdfButton.setOnClickListener {
            downloadPdfFromAssets("tyre_change.pdf", "TyreChangeGuide.pdf")
        }

        // Download oil PDF
        binding.downloadOilPdfButton.setOnClickListener {
            downloadPdfFromAssets("oil_change.pdf", "OilChangeGuide.pdf")
        }

        return root
    }

    private fun downloadPdfFromAssets(assetFileName: String, outputFileName: String) {
        try {
            val inputStream = requireContext().assets.open(assetFileName)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloadsDir, outputFileName)

            FileOutputStream(outFile).use { output ->
                inputStream.copyTo(output)
            }

            Toast.makeText(requireContext(), "Saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to download PDF", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
