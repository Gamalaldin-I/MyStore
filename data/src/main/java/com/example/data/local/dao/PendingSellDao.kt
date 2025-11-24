package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.entities.PendingSellActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSellAction(action: PendingSellActionEntity)
    @Update
    suspend fun updatePendingSellAction(action: PendingSellActionEntity)

    @Query("SELECT * FROM pending_sell_operations")
     fun getAllPendingSellActions(): Flow<List<PendingSellActionEntity>>

     @Query("DELETE FROM pending_sell_operations WHERE  status = 'Approved'")
     suspend fun deleteAllApprovedActions()


}