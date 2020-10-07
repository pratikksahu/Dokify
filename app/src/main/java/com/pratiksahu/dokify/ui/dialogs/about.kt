package com.pratiksahu.dokify.ui.dialogs

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.pratiksahu.dokify.databinding.FragmentAboutBinding
import kotlinx.android.synthetic.main.fragment_about.*


class About : DialogFragment() {

    lateinit var binding: FragmentAboutBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository.movementMethod = LinkMovementMethod.getInstance()
        contributorA.movementMethod = LinkMovementMethod.getInstance()
        contactEmail.movementMethod = LinkMovementMethod.getInstance()

        val manager = requireActivity().packageManager
        val info =
            manager.getPackageInfo(requireActivity().packageName, PackageManager.GET_ACTIVITIES)
        appVersion.text = info.versionName.toString()

//        if (Build.VERSION.SDK_INT >= 28) {
//            appVersion.text = info.longVersionCode.toString()
//        } else {
//            // Use older version
//            appVersion.text = info.versionCode.toString()
//        }
    }

}