package com.ticonsys.smsverification

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.ticonsys.base.BaseFragment
import com.ticonsys.smsverification.databinding.FragmentSmsVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class SmsVerificationFragment :
    BaseFragment<SmsVerificationViewModel, FragmentSmsVerificationBinding>(
        R.layout.fragment_sms_verification
    ) {


    private var activityResultLauncher: ActivityResultLauncher<Intent?>? = null

    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent =
                            extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            activityResultLauncher?.launch(consentIntent)
                        } catch (e: ActivityNotFoundException) {
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                    }
                }
            }
        }
    }


    companion object {
        private const val REGEX = "(|^)\\d{4}"
    }

    override val viewModel by viewModels<SmsVerificationViewModel>()

    override fun initializeViewBinding(view: View) = FragmentSmsVerificationBinding.bind(view)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerActivityResult()
        setupSmsVerificationReceiver()
        subscribeObservers()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(
            smsVerificationReceiver
        )
    }

    private fun registerActivityResult() {
        val resultContract = ActivityResultContracts.StartActivityForResult()
        activityResultLauncher = registerForActivityResult(resultContract) {
            val data = it.data ?: return@registerForActivityResult
            val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            viewModel.updateMessage(message)
        }
    }

    private fun setupSmsVerificationReceiver() {
        SmsRetriever.getClient(requireContext()).startSmsUserConsent(null)

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        requireActivity().registerReceiver(
            smsVerificationReceiver,
            intentFilter,
            SmsRetriever.SEND_PERMISSION,
            Handler(Looper.getMainLooper())
        )
    }


    private fun subscribeObservers() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message ?: return@observe
            binding.tvPage.text = parseOtpCode(message)
        }
    }


    private fun parseOtpCode(message: String?): String? {
        return if (message != null) {
            try {
                val pattern = Pattern.compile(REGEX)
                val matcher = pattern.matcher(message)
                if (matcher.find()) {
                    matcher.group(0)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }


}