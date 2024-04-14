package se.kr4u.todo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo")
    fun getAll(): Flow<List<ToDo>>

    @Query("SELECT * FROM todo WHERE uid = :toDoId")
    suspend fun loadById(toDoId: Int): ToDo

    @Insert
    suspend fun insert(vararg todos: ToDo)

    @Delete
    suspend fun delete(vararg todos: ToDo)

    @Update
    suspend fun update(vararg todos: ToDo)
}