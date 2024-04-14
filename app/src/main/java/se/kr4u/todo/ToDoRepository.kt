package se.kr4u.todo

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ToDoRepository(private val todoDao: ToDoDao) {
    val allToDos: Flow<List<ToDo>> = todoDao.getAll()

    @WorkerThread
    suspend fun insert(todo: ToDo) {
        todoDao.insert(todo)
    }

    @WorkerThread
    suspend fun getById(id: Int): ToDo {
        return todoDao.loadById(id)
    }

    @WorkerThread
    suspend fun delete(todo: ToDo) {
        todoDao.delete(todo)
    }

    @WorkerThread
    suspend fun update(todo: ToDo) {
        todoDao.update(todo)
    }
}