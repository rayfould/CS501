package com.example.q1_to_doapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.q1_to_doapp.ui.theme.Q1ToDoAppTheme
import androidx.room.*
import androidx.room.Room
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val isCompleted: Boolean = false,
    val edited: Boolean = false
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = :completed")
    fun getTasksByCompletion(completed: Boolean): List<Task>

    @Insert
    fun insert(task: Task)

    @Update
    fun update(task: Task)

    @Delete
    fun delete(task: Task)
}

@Database(entities = [Task::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Q1ToDoAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ToDoApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun ToDoApp(modifier: Modifier = Modifier) {
    // Build the Room db
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(context, TaskDatabase::class.java, "tasks.db")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
    val taskDao = db.taskDao()

    // State for the text box
    var newTaskText by remember { mutableStateOf("") }
    // State for filter
    var filterOption by remember { mutableStateOf("All") }
    // State for the list of tasks
    val tasks = remember { mutableStateListOf<Task>() }
    // State for tracking if a task is being edited
    var editingTask by remember { mutableStateOf<Task?>(null) }

    // Refresh the tasks  from the db
    fun refreshTasks() {
        tasks.clear()
        when (filterOption) {
            "Completed" -> tasks.addAll(taskDao.getTasksByCompletion(true))
            "In Progress" -> tasks.addAll(taskDao.getTasksByCompletion(false))
            else -> tasks.addAll(taskDao.getAllTasks())
        }
    }

    // Refresh tasks on app load or filter change
    LaunchedEffect(filterOption) {
        refreshTasks()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Row for showing filter option and toggle button.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Filter: $filterOption")
            Button(onClick = {
                // Toggle filter between All, Completed, and In Progress.
                filterOption = when (filterOption) {
                    "All" -> "Completed"
                    "Completed" -> "In Progress"
                    else -> "All"
                }
                refreshTasks()
            }) {
                Text("Toggle Filter")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Row for making or editing a task
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                placeholder = { Text("Enter task") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (editingTask != null) {
                // If editing, show save button
                Button(onClick = {
                    if (newTaskText.isNotBlank()) {
                        // Update the task with the new description and set edited to true.
                        val updatedTask = editingTask!!.copy(description = newTaskText, edited = true)
                        taskDao.update(updatedTask)
                        refreshTasks()
                        newTaskText = ""
                        editingTask = null
                    }
                }) {
                    Text("Save")
                }
            } else {
                // If making new task, show Add button
                Button(onClick = {
                    if (newTaskText.isNotBlank()) {
                        val task = Task(description = newTaskText)
                        taskDao.insert(task)
                        refreshTasks()
                        newTaskText = ""
                    }
                }) {
                    Text("Add Task")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Display the list of tasks
        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { checked ->
                            // Update the task status
                            val updatedTask = task.copy(isCompleted = checked)
                            taskDao.update(updatedTask)
                            refreshTasks()
                        }
                    )
                    Text(
                        text = task.description + if (task.edited) " [edited]" else "",
                        modifier = Modifier.weight(1f)
                    )
                    // Edit button
                    Button(onClick = {
                        newTaskText = task.description
                        editingTask = task
                    }) {
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    // Delete button
                    Button(onClick = {
                        taskDao.delete(task)
                        if (editingTask?.id == task.id) {
                            editingTask = null
                            newTaskText = ""
                        }
                        refreshTasks()
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
