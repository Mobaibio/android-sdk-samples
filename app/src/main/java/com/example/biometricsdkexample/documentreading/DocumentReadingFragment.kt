package com.example.biometricsdkexample.documentreading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import bio.mobai.library.document.api.models.MBDocumentCaptureResult
import bio.mobai.library.document.api.models.MBDocumentType
import com.example.biometricsdkexample.databinding.FragmentDocumentReadingBinding
import kotlinx.coroutines.launch

class DocumentReadingFragment : Fragment() {

    private val viewModel: DocumentReaderViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DocumentReaderViewModel() as T
            }
        }
    }

    private var _binding: FragmentDocumentReadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.startCamera(requireActivity(), binding.container.id)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.document.collect { document ->
                    document?.let {
                        val keys = extractKeys(document = document)
                        val bundle = Bundle().apply {
                            putString("document_number", keys?.first) // Key-Value pair
                            putString("birth_date", keys?.second)
                            putString("expiry_date", keys?.third)
                        }
                      //  findNavController().navigate(R.id.action_documentReadingFragment_to_nfcReaderFragment, bundle)
                    }
                }
            }
        }

    }

    private fun extractKeys(document: MBDocumentCaptureResult): Triple<String, String, String>? {
        val keys = when (document.getDocumentType()) {
                MBDocumentType.TD1 -> {
                    val mrz = document.getDocumentType1MRZ()
                    Triple(
                        first = mrz?.documentNumber?.value ?: "",
                        second = mrz?.dateOfBirth?.date?.value ?: "",
                        third = mrz?.dateOfExpiry?.date?.value ?: ""
                    )
                }
                MBDocumentType.TD2 -> {
                     val mrz = document.getDocumentType2MRZ()
                    Triple(
                        first = mrz?.documentNumber?.value ?: "",
                        second = mrz?.dateOfBirth?.date?.value ?: "",
                        third = mrz?.dateOfExpiry?.date?.value ?: ""
                    )
                }

                MBDocumentType.TD3 -> {
                    val mrz =
                        document.getDocumentType3MRZ()
                    Triple(
                        first = mrz?.passportNumber?.value ?: "",
                        second = mrz?.dateOfBirth?.date?.value ?: "",
                        third = mrz?.dateOfExpiry?.date?.value ?: ""
                    )
                }
            null -> null
        }

        return keys

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}