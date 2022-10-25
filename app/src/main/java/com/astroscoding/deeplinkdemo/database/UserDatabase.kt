package com.astroscoding.deeplinkdemo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.astroscoding.deeplinkdemo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {

    abstract val usersDao: UsersDao


    companion object {
        private var INSTANCE: UserDatabase? = null

        fun getUserDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = Room
                    .databaseBuilder(
                        context,
                        UserDatabase::class.java,
                        "users_database"
                    )
                    .addCallback(PopulateCallback(context))
                    .build()
                INSTANCE!!
            }

        }

    }

    class PopulateCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val usersDao = getUserDatabase(context).usersDao
            CoroutineScope(SupervisorJob()).launch {
                usersDao.insertUser(User(0, "Astro", context.getString(R.string.dump_text), 2018, true))
                usersDao.insertUser(User(0, "Abdul-Dijk", context.getString(R.string.dump_text), 2018, true))
                usersDao.insertUser(User(0, "Dexter", context.getString(R.string.dump_text), 2018, true))
                usersDao.insertUser(User(0, "Connor", context.getString(R.string.dump_text), 2018, true))
                usersDao.insertUser(User(0, "Kruger", context.getString(R.string.dump_text), 2019, true))
            }
        }
    }

}