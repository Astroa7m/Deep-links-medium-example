package com.astroscoding.deeplinkdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.astroscoding.deeplinkdemo.database.User
import com.astroscoding.deeplinkdemo.database.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val db: UserDatabase
) : ViewModel() {

    val users = db.usersDao.getUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getUser(userId: Int?): User?{
        return withContext(Dispatchers.IO){
            db.usersDao.getUser(userId)
        }
    }

    fun addNewUser(newUser: User) {
        viewModelScope.launch(Dispatchers.IO) {
            db.usersDao.insertUser(newUser)
        }
    }

    companion object{
        class MainViewModelFactory (private val db: UserDatabase) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(db) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

}