package com.kuss.kdrop.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "servers")
data class Server(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
    val selected: Boolean? = false
)

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers")
    fun getAll(): List<Server>

    @Query("SELECT * FROM servers WHERE selected = 1")
    fun getSelected(): Server

    @Query("UPDATE servers SET selected = 0")
    fun clearSelection()

    @Query("UPDATE servers SET selected = 1 WHERE id = :id")
    fun select(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: Server)
}