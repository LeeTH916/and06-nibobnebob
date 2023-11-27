package com.avengers.nibobnebob.presentation.ui.main.home.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.avengers.nibobnebob.R
import com.avengers.nibobnebob.databinding.FragmentRestaurantSearchBinding
import com.avengers.nibobnebob.presentation.base.BaseFragment
import com.avengers.nibobnebob.presentation.ui.adjustKeyboard
import com.avengers.nibobnebob.presentation.ui.main.MainViewModel
import com.avengers.nibobnebob.presentation.ui.main.home.adapter.HomeSearchAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class RestaurantSearchFragment :
    BaseFragment<FragmentRestaurantSearchBinding>(R.layout.fragment_restaurant_search) {
    private val viewModel: RestaurantSearchViewModel by viewModels()
    override val parentViewModel: MainViewModel by activityViewModels()
    private val adapter = HomeSearchAdapter { item ->
        viewModel.onClickSearchItem(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        collectEvent()
        setFocus()
        clearFocus(view)

    }

    private fun initView() {
        binding.vm = viewModel
        binding.rvSearch.adapter = adapter

        repeatOnStarted {
            viewModel.uiState.collectLatest {
                adapter.setResultList(it.searchList, it.searchKeyword)
            }
        }

    }

    private fun collectEvent() {
        repeatOnStarted {
            viewModel.events.collect {
                when (it) {
                    is RestaurantSearchEvent.OnClickResultItem -> {
                        findNavController().toHome()
                        parentViewModel.markSearchRestaurant(it.item)
                    }

                    is RestaurantSearchEvent.NavigateToHome -> findNavController().toHome()
                }

            }
        }
    }

    private fun NavController.toHome() {
        val action =
            RestaurantSearchFragmentDirections.actionRestaurantSearchFragmentToHomeFragment()
        navigate(action)
    }

    private fun setFocus() {
        binding.tietInputSearch.requestFocus()
        requireActivity().adjustKeyboard(binding.tietInputSearch.findFocus(), true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clearFocus(view: View) {
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.tietInputSearch.clearFocus()
                requireContext().adjustKeyboard(view, false)
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
    }
}