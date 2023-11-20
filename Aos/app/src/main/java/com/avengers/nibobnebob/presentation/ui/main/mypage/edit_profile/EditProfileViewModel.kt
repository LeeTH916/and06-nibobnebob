package com.avengers.nibobnebob.presentation.ui.main.mypage.edit_profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avengers.nibobnebob.data.model.ApiState
import com.avengers.nibobnebob.data.model.request.MyPageEditInfoRequest
import com.avengers.nibobnebob.data.repository.MyPageEditRepository
import com.avengers.nibobnebob.presentation.ui.main.mypage.Validation
import com.avengers.nibobnebob.presentation.ui.main.mypage.mapper.toUiMyPageEditInfoData
import com.avengers.nibobnebob.presentation.util.LocationArray
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

data class EditProfileUiState(
    val nickName: InputState = InputState(),
    val email: String = "",
    val provider: String = "",
    val birth: InputState = InputState(),
    val location: InputState = InputState()
)


data class InputState(
    val helperText: Validation = Validation.NONE,
    val isValid: Boolean = true,
    val isChanged: Boolean = false,
)

sealed class EditProfileUiEvent {
    data object EditProfileDone : EditProfileUiEvent()
}


@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val myPageEditRepository: MyPageEditRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditProfileUiEvent>(replay = 0)
    val event: SharedFlow<EditProfileUiEvent> = _events.asSharedFlow()

    private var originalNickName: String = ""
    private var originalBirth: String = ""
    private var originalLocation: String = ""
    private var originalIsMale: Boolean = true

    val locationList = LocationArray.LOCATION_ARRAY

    val nickState = MutableStateFlow("")
    val birthState = MutableStateFlow("")
    val locationPositionState = MutableStateFlow(0)


    init {
        observeNickName()
        observeLocation()
        observeBirth()
        getOriginalData()
    }

    private fun getOriginalData() {
        myPageEditRepository.getMyPageEditInfo().onEach {

            when (it) {
                is ApiState.Success -> {
                    it.data.toUiMyPageEditInfoData().apply {
                        Log.d("TEST", "$this")
                        _uiState.update { state ->
                            state.copy(
                                email = email,
                                provider = provider
                            )
                        }
                        nickState.emit(nickName)
                        originalNickName = nickName
                        locationPositionState.emit(locationList.indexOf(location))
                        originalLocation = location
                        birthState.emit(birth)
                        originalBirth = birth
                        originalIsMale = gender

                    }
                }

                is ApiState.Error -> Log.d("TEST", "${it.message}")
                is ApiState.Exception -> Log.d("TEST", "${it.e}")
            }
        }.launchIn(viewModelScope)
    }

    private fun observeNickName() {
        nickState.onEach { nick ->
            _uiState.update { state ->
                state.copy(
                    nickName = InputState(
                        helperText = Validation.NONE,
                        isValid = originalNickName == nick,
                        isChanged = originalNickName != nick
                    )
                )
            }
        }.launchIn(viewModelScope)

    }

    fun checkNickValidation() {
        myPageEditRepository.getCheckNickname(nickState.value).onEach {
            when(it){
                is ApiState.Success -> {
                    if (it.data.data.isExist){
                        Log.d("TEST", "닉네임 중복")
                    } else{
                        _uiState.value = uiState.value.copy(
                            nickName = InputState(
                                helperText = Validation.VALID_NICK,
                                isValid = true,
                                isChanged = originalNickName != nickState.value
                            )
                        )
                    }
                }
                else -> Log.d("TEST", "검증 실패")
            }
        }.launchIn(viewModelScope)

    }

    private fun observeLocation() {
        locationPositionState.onEach { position ->
            _uiState.update { state ->
                state.copy(
                    location = InputState(
                        isValid = (position != 0),
                        isChanged = locationList.indexOf(originalLocation) != position
                    )
                )
            }
        }.launchIn(viewModelScope)


    }

    fun setBirth(birthData: String) {
        birthState.value = birthData
    }


    private fun observeBirth() {
        birthState.onEach { birth ->
            val validData = birth.matches(BIRTH_REGEX)
            _uiState.update { state ->
                state.copy(
                    birth = InputState(
                        helperText = if (!validData && birth.isNotEmpty()) Validation.INVALID_DATE else Validation.VALID_DATE,
                        isValid = validData,
                        isChanged = (originalBirth != birth)
                    )
                )
            }
        }.launchIn(viewModelScope)
    }


    fun doneEditProfile() {

        myPageEditRepository.putMyPageEditInfo(
            MyPageEditInfoRequest(
                nickName = nickState.value,
                email = uiState.value.email,
                provider = uiState.value.provider,
                birthdate = birthState.value,
                region = locationList[locationPositionState.value],
                isMale = originalIsMale,
                password = "1234"
            )
        ).onEach {
            when(it){
                is ApiState.Success -> _events.emit(EditProfileUiEvent.EditProfileDone)
                else -> Log.d("TEST", "수정 실패")
            }
        }.launchIn(viewModelScope)
    }


    companion object {
        val BIRTH_REGEX = Regex("""^\d{4}/\d{2}/\d{2}${'$'}""")
    }
}