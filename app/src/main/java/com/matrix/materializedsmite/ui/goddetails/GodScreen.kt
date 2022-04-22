package com.matrix.materializedsmite.ui

import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.matrix.materializedsmite.viewmodels.SmiteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GodScreen(
  smiteAppViewModel: SmiteViewModel,
  modifier: Modifier = Modifier
) {
  val selectedGod = smiteAppViewModel.selectedGod.value
  val swipeState = rememberSwipeableState(initialValue = 0)
  val scrollState = rememberScrollState()
  //Log.d("Scroll state: ", scrollState.value.toString())
  val heightInPx: Float = with(LocalDensity.current) {
    LocalConfiguration.current.screenHeightDp.dp.toPx()
  }
  val heightInDp: Dp = LocalConfiguration.current.screenHeightDp.dp
  val anchors = mapOf(heightInPx to 0, 0f to 1)
  //Log.d("SCREEN HEIGHT: ", LocalConfiguration.current.screenHeightDp.toString())
  //Log.d("SWIPE STATE: ", with(LocalDensity.current) { swipeState.offset.value }.toString())
  //Log.d("Progress fraction: ", swipeState.progress.fraction.toString())
  val connection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
      ): Offset {
        val delta = available.y
        return if (delta < 0) {
          Offset(x = 0f, y = swipeState.performDrag(delta))
        } else {
          Offset.Zero
        }
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
      ): Offset {
        return Offset(x = 0f, y = swipeState.performDrag(available.y))
      }

      override suspend fun onPreFling(available: Velocity): Velocity {
        return if (available.y < 0 && scrollState.value == 0) {
          swipeState.performFling(available.y)
          available
        } else {
          Velocity.Zero
        }
      }

      override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
      ): Velocity {
        swipeState.performFling(velocity = available.y)
        return super.onPostFling(consumed, available)
      }
    }
  }
  if (selectedGod != null) {
    Box(
      contentAlignment = Alignment.BottomCenter,
      modifier = modifier
        .fillMaxSize()
        .swipeable(
          state = swipeState,
          anchors = anchors,
          thresholds = { _, _ -> FractionalThreshold(0.2f) },
          orientation = Orientation.Vertical
        )
        .nestedScroll(connection),
    ) {
      GodScreenBackground(
        selectedGod = selectedGod,
        // Subtract height to "start" at 0 and then move upwards -Y
        offset = swipeState.offset.value - heightInPx,
        modifier = Modifier.matchParentSize()
      )
      GodDetails(
        smiteAppViewModel,
        scrollState = scrollState,
        modifier = Modifier
          .matchParentSize()
          .offset(x = 0.dp, y = with(LocalDensity.current) { (swipeState.offset.value).toDp() })
      )
    }

  }
}

//@Preview
//@Composable
//fun Preview() {
//  val viewModel = SmiteAppViewModel()
//  viewModel.setGod(GodInformation())
//  GodDetails(smiteAppViewModel = SmiteAppViewModel())
//}