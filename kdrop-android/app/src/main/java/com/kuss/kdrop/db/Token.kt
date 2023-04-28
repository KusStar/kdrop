package com.kuss.kdrop.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "tokens")
data class Token(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val token: String,
)

@Dao
interface TokenDao {
    @Query("SELECT * FROM tokens")
    fun getAll(): List<Token>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: Token)
}
