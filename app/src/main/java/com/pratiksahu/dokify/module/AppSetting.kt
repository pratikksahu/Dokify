package com.pratiksahu.dokify.module

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.pratiksahu.dokify.MainActivityViewModel
import com.pratiksahu.dokify.databinding.FragmentAppSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_app_setting.*
import javax.inject.Inject

@AndroidEntryPoint
class AppSetting : DialogFragment() {

    @Inject
    lateinit var mainActivityViewModel: MainActivityViewModel

    private val navController by lazy { NavHostFragment.findNavController(this) }

    lateinit var binding: FragmentAppSettingBinding
    private var compression = "40"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setObservers()
    }

    fun setObservers() {
        mainActivityViewModel.compression.observe(viewLifecycleOwner, Observer {
            compression = it
            compressionValue.setText(compression)
        })
    }

    fun setListeners() {
        compressionValue.addTextChangedListener {
            var result = it.toString()
            if (result.isNotBlank() || result.isNotEmpty()) {
                if (result.toInt() > 100)
                    compression = (100).toString()
                else if (result.toInt() < 20)
                    compression = (20).toString()
                else
                    compression = result
            } else {
                compression = (40).toString()
            }
        }

        saveButton.setOnClickListener {
            mainActivityViewModel.setCompression(compression)
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

}