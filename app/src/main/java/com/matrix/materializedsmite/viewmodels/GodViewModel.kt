package com.matrix.materializedsmite.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.api.models.GodInformation
import com.matrix.api.models.GodSkin
import com.matrix.materializedsmite.repositories.smite.SmiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GodListUiState(
  val gods: List<GodInformation> = listOf()
)

data class GodDetailsUiState(
  val selectedGod: GodInformation? = null,
  val godSkins: List<GodSkin> = listOf()
)

@HiltViewModel
class GodViewModel @Inject constructor(
  private val smiteRepo: SmiteRepository,
) : ViewModel(), CanError {

  private val _godListUiState = mutableStateOf(GodListUiState())
  val godListUiState: State<GodListUiState> = _godListUiState

  private val _godDetailsUiState = mutableStateOf(GodDetailsUiState())
  val godDetailsUiState: State<GodDetailsUiState> = _godDetailsUiState

  //private val _gods = MutableStateFlow<List<GodInformation>>(listOf())
  //val gods: StateFlow<List<GodInformation>> = _gods.asStateFlow()

  private val _selectedGod = MutableStateFlow<GodInformation?>(null)
  //val selectedGod: StateFlow<GodInformation?> = _selectedGod.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val godsApiResult = smiteRepo.getGods()
        _godListUiState.value = GodListUiState(gods = godsApiResult)
        error = null
      } catch (ex: Exception) {
        error = ex
        Log.e("GodViewModel", ex.toString())
      }

    }
    Log.d("GodViewModel", "init")
    _selectedGod.onEach { godInformation ->
      try {
        when(godInformation) {
          null -> {
            _godDetailsUiState.value = GodDetailsUiState(selectedGod = null, godSkins = listOf())
          }
          else -> {
            var godSkins: List<GodSkin>
            withContext(Dispatchers.IO) {
              godSkins =  smiteRepo.getGodSkins(godInformation.id)
            }
            _godDetailsUiState.value = GodDetailsUiState(selectedGod = godInformation, godSkins = godSkins)
          }
        }
        error = null
      } catch (ex: Exception) {
        error = ex
        Log.e("GodViewModel", ex.toString())
      }
    }.launchIn(viewModelScope)
  }

  fun setGod(godInformation: GodInformation) {
    _selectedGod.update { godInformation }
  }

  fun clearSelectedGod() {
    _selectedGod.update { null }
  }

  override var error: Exception? = null
}