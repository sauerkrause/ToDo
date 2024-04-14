package se.kr4u.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ToDoViewModel(private val repo: ToDoRepository): ViewModel() {
    val allToDos = repo.allToDos

    fun insert(todo: ToDo) = viewModelScope.launch {
        repo.insert(todo)
    }

    fun delete(todo: ToDo) = viewModelScope.launch {
        repo.delete(todo)
    }

    suspend fun getToDo(id: Int): ToDo {
        return repo.getById(id)
    }

    fun update(todo: ToDo) = viewModelScope.launch {
        repo.update(todo)
    }
}

class ToDoViewModelFactory(private val repo: ToDoRepository) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToDoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToDoViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}