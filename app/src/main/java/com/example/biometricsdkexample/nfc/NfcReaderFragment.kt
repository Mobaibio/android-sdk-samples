package com.example.biometricsdkexample.nfc

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
import androidx.navigation.fragment.findNavController
import com.example.biometricsdkexample.MainActivity
import com.example.biometricsdkexample.R
import com.example.biometricsdkexample.databinding.FragmentNfcReaderBinding
import com.mobai.library.nfc_reading.MBNfcKeyFactory
import com.mobai.library.nfc_reading.MBNfcTravelDocumentReader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NfcReaderFragment : Fragment() {

    private var _binding: FragmentNfcReaderBinding? = null
    private val binding get() = _binding!!

    // Initialize SDK Reader lazily to ensure Context is available
    private val nfcReaderSDK by lazy {
        MBNfcTravelDocumentReader(
            context = requireContext().applicationContext,
            masterList = readRawResource(R.raw.masterlist_no_pol_2025_03_04_de_2025_04_16),
            license = readRawResource(R.raw.iengine),
            timeoutMillis = 5000,
            isDebugInfoEnabled = true
        )
    }

    // Inject the custom Factory to provide NfcReaderImpl to the ViewModel
    private val viewModel: NfcReaderViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val impl = NfcReaderImpl(requireContext().applicationContext, nfcReaderSDK)
                return NfcReaderViewModel(impl) as T
            }
        }
    }

    private var docNumber = ""
    private var birthDate = ""
    private var expiryDate = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNfcReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve the data using the same keys
          docNumber = arguments?.getString("document_number") ?: "FHC002594"
          birthDate = arguments?.getString("birth_date") ?: "560423"
          expiryDate = arguments?.getString("expiry_date") ?: "250612"

        // Observe StateFlow using lifecycle-aware collector
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUi(state)
                }
            }
        }

        binding.btnEnableNfc.setOnClickListener {
            viewModel.disableNfcReading(requireActivity())
            (activity as? MainActivity)?.onBackPressed()
        }
    }

    /**
     * Updates UI components based on the current ScanningState.
     */
    private suspend fun renderUi(state: ScanningState) {
        when (state) {
            is ScanningState.Idle -> {
                binding.tvScanningStatus.text = "Hold document against the back of the phone"
                binding.progress.visibility = View.INVISIBLE
            }
            is ScanningState.Access -> {
                binding.tvScanningStatus.text = "Connecting to chip..."
                binding.progress.visibility = View.VISIBLE
                binding.progress.isIndeterminate = true
            }
            is ScanningState.Scanning -> {
                binding.tvScanningStatus.text = "Reading Data: ${state.progress}%"
                binding.progress.isIndeterminate = false
                binding.progress.progress = state.progress
            }
            is ScanningState.Verifying -> {
                binding.tvScanningStatus.text = "Authenticating Document..."
                binding.progress.isIndeterminate = true
            }
            is ScanningState.Error -> {
                binding.tvScanningStatus.text = "Error: ${state.error}"
                binding.progress.visibility = View.INVISIBLE
            }
            is ScanningState.Finished -> {
                binding.tvScanningStatus.text = "Scan Complete!"
                binding.progress.visibility = View.INVISIBLE

               delay(1000)
                // Ensure the fragment is still attached before popping
                findNavController().navigate(R.id.action_nfcReaderFragment_to_homeFragment)
            }
        }
    }

    private fun readRawResource(id: Int): ByteArray =
        resources.openRawResource(id).use { it.readBytes() }

    override fun onResume() {
        super.onResume()
        // Provide the BAC keys (Document #, Expiry, DOB)
        val keys = MBNfcKeyFactory(docNumber, expiryDate, birthDate)
        viewModel.enableNfcReading(requireActivity(), keys)
    }

    override fun onPause() {
        super.onPause()
        viewModel.disableNfcReading(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}