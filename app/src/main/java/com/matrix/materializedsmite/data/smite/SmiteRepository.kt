package com.matrix.materializedsmite.data.smite

import com.matrix.materializedsmite.data.models.GodInformation
import com.matrix.materializedsmite.data.models.GodSkin
import kotlinx.coroutines.flow.Flow

interface SmiteRepository {
  suspend fun getGods(): List<GodInformation>
  suspend fun getGodSkins(godId: Int): List<GodSkin>
}