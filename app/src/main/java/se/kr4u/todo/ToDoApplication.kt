package se.kr4u.todo

import android.app.Application

class ToDoApplication: Application() {
    val database by lazy { ToDoDatabase.getDatabase(this) }
    val repository by lazy { ToDoRepository(database.getToDoDao()) }
}