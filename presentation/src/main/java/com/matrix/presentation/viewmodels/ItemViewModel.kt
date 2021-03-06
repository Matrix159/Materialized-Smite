package com.matrix.presentation.viewmodels

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.domain.contracts.SmiteRepository
import com.matrix.domain.models.Item
import com.matrix.presentation.cache.Cache
import com.matrix.presentation.utils.ItemNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class ItemListCache(private val sharedPreferences: SharedPreferences) :
  Cache<String, List<Item>> {
  override suspend fun getAsync(key: String): List<Item> {
    return withContext(Dispatchers.IO) {
      sharedPreferences.getString(key, null)?.let {
        Json.decodeFromString<List<Item>>(it)
      } ?: listOf()
    }
  }

  override suspend fun setAsync(key: String, value: List<Item>) {
    return withContext(Dispatchers.IO) {
      sharedPreferences.edit().let {
        it.putString(key, Json.encodeToString(value))
        it.apply()
      }
    }
  }
}

const val ITEM_LIST_CACHE_KEY = "item_list_cache"
const val ITEM_STATE = "ItemViewModel_Item"

data class ItemUiState(
  val items: List<Item>,
  val itemIdMap: Map<Long, Item>,
  val errorMessages: List<String>
)

@HiltViewModel
class ItemViewModel @Inject constructor(
  private val smiteRepo: SmiteRepository,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel(), CanError {

//  private val itemListCache = ItemListCache(
//    SmiteApplication.instance.getSharedPreferences(
//      ITEM_LIST_CACHE_KEY,
//      Context.MODE_PRIVATE
//    )
//  )
  private val _items = MutableStateFlow<List<Item>>(listOf())
  val items: StateFlow<List<Item>> = _items

  private val _itemIdMap: MutableState<Map<Long, Item>?> = mutableStateOf(null)
  val itemIdMap: State<Map<Long, Item>?> = _itemIdMap

  private val _baseItemTreeNodes: MutableState<List<ItemNode>> = mutableStateOf(listOf())
  val baseItemTreeNodes: State<List<ItemNode>> = _baseItemTreeNodes

  private val _selectedItem = MutableStateFlow<Item?>(null) // TODO: Bring back saved state after clean rework
  val selectedItem: StateFlow<Item?> = _selectedItem

  suspend fun loadItems() {
    Timber.d("loadItems in ItemViewModel")
    viewModelScope.launch {
      try {
        val itemList = withContext(Dispatchers.IO) {
//          itemListCache.getAsync(ITEM_LIST_CACHE_KEY).ifEmpty {
//            val newResults = smiteRepo.getItems()
//            itemListCache.setAsync(ITEM_LIST_CACHE_KEY, newResults)
//            newResults
            smiteRepo.getItems()
        }
        _items.value = itemList.filter { it.activeFlag == "y" }
        error = null
      } catch (ex: Exception) {
        error = ex
        Timber.e(ex.toString())
      }

      // Set up the item tree's via a node structure
      items.onEach { itemList ->
        _itemIdMap.value = itemList.associateBy { item -> item.itemID }
        val itemsGroupedByTier = itemList.groupBy { it.itemTier }
        val baseNodes = mutableListOf<ItemNode>()
        itemList.filter { item -> item.itemTier == 1L}.forEach {
          baseNodes.add(ItemNode(it))
        }
        baseNodes.forEach { node ->
          node.findChildren(itemsGroupedByTier)
        }
        _baseItemTreeNodes.value = baseNodes
        //Timber.d(_baseItemTreeNodes.value.joinToString(", "))
      }.collect()
    }
  }

  fun setItem(item: Item?) {
    _selectedItem.value = item
    // TODO: Bring back saved state after clean arch rework
    //savedStateHandle[ITEM_STATE] = item
  }

  override var error: Exception? = null
}
