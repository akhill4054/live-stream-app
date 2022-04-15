package com.example.livestream.home.ui.dialoges

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.livestream.databinding.BottomSheetLiveStreamSearchFiltersBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchFiltersBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetLiveStreamSearchFiltersBinding? = null
    private val binding get() = _binding!!

    private lateinit var interactions: Interactions

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            interactions = context as Interactions
        } catch (e: Exception) {
            throw IllegalStateException("Parent activity must implement ${Interactions::class.qualifiedName}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetLiveStreamSearchFiltersBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup view here.
        with(binding) {
            cbIsLive.isChecked = requireArguments().getBoolean(ARG_IS_LIVE_FILTER_STATUS, false)
            cbIsPopular.isChecked =
                requireArguments().getBoolean(ARG_IS_POPULAR_FILTER_STATUS, false)

            cbIsLive.setOnCheckedChangeListener { _, isChecked ->
                interactions.onIsLiveFilterChecked(isChecked)
            }
            cbIsPopular.setOnCheckedChangeListener { _, isChecked ->
                interactions.onIsPopularFilterChecked(isChecked)
            }
            btnClose.setOnClickListener {
                dismissAllowingStateLoss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface Interactions {
        fun onIsLiveFilterChecked(isChecked: Boolean)
        fun onIsPopularFilterChecked(isChecked: Boolean)
    }

    companion object {
        private const val ARG_IS_LIVE_FILTER_STATUS = "arg_is_live_filter_status"
        private const val ARG_IS_POPULAR_FILTER_STATUS = "arg_is_popular_filter_status"

        fun getInstance(isLiveFilterStatus: Boolean?, isPopularFilterStatus: Boolean?) =
            SearchFiltersBottomSheet().apply {
                arguments = bundleOf(
                    ARG_IS_LIVE_FILTER_STATUS to isLiveFilterStatus,
                    ARG_IS_POPULAR_FILTER_STATUS to isPopularFilterStatus,
                )
            }
    }
}
