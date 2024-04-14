package se.kr4u.todo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ToDo(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    var title: String,
    var details: String,
)