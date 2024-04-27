package se.kr4u.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

abstract class IToDoViewModel : ViewModel() {
    abstract val allToDos: Flow<List<ToDo>>
    abstract val initialToDos: List<ToDo>
    abstract fun insert(todo: ToDo): Job
    abstract fun delete(todo: ToDo): Job
    abstract suspend fun getToDo(id: Int): ToDo
    abstract fun update(todo: ToDo): Job
}

class ToDoViewModel(private val repo: ToDoRepository): IToDoViewModel() {
    override val allToDos = repo.allToDos
    override val initialToDos: List<ToDo> = listOf()
    override fun insert(todo: ToDo) = viewModelScope.launch {
        repo.insert(todo)
    }

    override fun delete(todo: ToDo) = viewModelScope.launch {
        repo.delete(todo)
    }

    override suspend fun getToDo(id: Int): ToDo {
        return repo.getById(id)
    }

    override fun update(todo: ToDo) = viewModelScope.launch {
        repo.update(todo)
    }
}

class PreviewToDoViewModel() : IToDoViewModel() {
    override val initialToDos = arrayListOf(ToDo(1, "Foo", "Bar"), ToDo(2, "Baz", "Quux"))
    override val allToDos = MutableStateFlow(initialToDos)
    override fun insert(todo: ToDo) = viewModelScope.launch {}
    override fun delete(todo: ToDo) = viewModelScope.launch {}
    override suspend fun getToDo(id: Int): ToDo {
        return initialToDos[id]
    }
    override fun update(todo: ToDo) = viewModelScope.launch {}
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