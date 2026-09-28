package com.example.data.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

object TeacherDatabaseProvider {
    private val instances = mutableMapOf<String, TeacherDatabase>()

    @OptIn(ExperimentalForeignApi::class)
    private fun documentsPath(): String {
        val url = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        return requireNotNull(url?.path)
    }

    fun getDatabase(userId: String? = null): TeacherDatabase {
        val name = teacherDatabaseName(userId)
        return instances.getOrPut(name) {
            Room.databaseBuilder<TeacherDatabase>(name = "${documentsPath()}/$name")
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.Default)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
