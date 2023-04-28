package com.kuss.kdrop.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, Server::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serverDao(): ServerDao
}