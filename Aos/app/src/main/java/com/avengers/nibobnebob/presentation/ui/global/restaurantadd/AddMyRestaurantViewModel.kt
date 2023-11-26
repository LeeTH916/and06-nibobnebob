package com.avengers.nibobnebob.presentation.ui.global.restaurantadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMyRestaurantUiState(
    val visitWithCar: Boolean = false,
    val parkingSpace: Int = 0,
    val traffic: Int = 0,
    val taste: Int = 0,
    val service: Int = 0,
    val toilet: Int = 0
)

sealed class AddMyRestaurantEvents {
    data object NavigateToBack : AddMyRestaurantEvents()
}

@HiltViewModel
class AddMyRestaurantViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AddMyRestaurantUiState())
    val uiState: StateFlow<AddMyRestaurantUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddMyRestaurantEvents>()
    val events: SharedFlow<AddMyRestaurantEvents> = _events.asSharedFlow()

    val comment = MutableStateFlow("")
    val isDataReady = MutableStateFlow(false)

    init {
        observeComment()
    }

    private fun observeComment() {
        comment.onEach {
            isDataReady.value = it.length >= 20
        }.launchIn(viewModelScope)
    }

    fun setIsVisitWithCar(visitWithCar: Boolean) {
        _uiState.update { state ->
            state.copy(visitWithCar = visitWithCar)
        }
    }

    fun sliderStateChange(
        estimateItem: EstimateItem,
        value: Int
    ) {
        when (estimateItem) {
            EstimateItem.PARKING -> {
                _uiState.update { state ->
                    state.copy(parkingSpace = value)
                }
            }

            EstimateItem.TRAFFIC -> {
                _uiState.update { state ->
                    state.copy(traffic = value)
                }
            }

            EstimateItem.TASTE -> {
                _uiState.update { state ->
                    state.copy(taste = value)
                }
            }

            EstimateItem.SERVICE -> {
                _uiState.update { state ->
                    state.copy(service = value)
                }
            }

            EstimateItem.TOILET -> {
                _uiState.update { state ->
                    state.copy(toilet = value)
                }
            }
        }
    }

    fun addRestaurant() {
        viewModelScope.launch {

            // todo 통신로직


            // todo 등록 성공시 뒤로가기
            navigateToBack()
        }
    }

    fun navigateToBack() {
        viewModelScope.launch {
            _events.emit(AddMyRestaurantEvents.NavigateToBack)
        }
    }
}

enum class EstimateItem() {
    PARKING,
    TRAFFIC,
    TASTE,
    SERVICE,
    TOILET,
}