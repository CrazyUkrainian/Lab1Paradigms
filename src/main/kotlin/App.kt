package org.example
import java.io.File

// Application state class (ensure encapsulation, for immutabilty)
// each function takes the current state and returns a new state instead of mutating global variables
data class TodoAppState(
    val tasks: List<Triple<String, Boolean, String>> = listOf(),
    val lastAction: LastAction = LastAction.None
)

sealed class LastAction {
    data class Add(val task: Triple<String, Boolean, String>) : LastAction()
    data class Complete(val task: Triple<String, Boolean, String>) : LastAction()
    data class Remove(val removedTasks: List<Triple<String, Boolean, String>>) : LastAction()
    object None : LastAction()
}

fun main() {
    var state = TodoAppState(tasks = loadTasks()) // Initialize with tasks from file
// all functions now receive state and work on it, returning an updated state
    println("welcome to the best kotlin ToDo app")
    while (true) {
        displayTasks(state.tasks)
        showTaskStatistics(state.tasks)
        println("Choose action: a - add / c - complete / r - remove / u - undo / q - quit")

        state = when (readlnOrNull()?.lowercase()) {
            "a" -> addTask(state)
            "c" -> markTaskComplete(state)
            "r" -> removeCompletedTasks(state)
            "u" -> undoLastAction(state)
            "q" -> {
                saveTasks(state.tasks)
                println("goodbye!")
                break
            }
            else -> {
                println("Invalid input")
                state
            }
        }
    }
}

// UI Layer

fun displayTasks(tasks: List<Triple<String, Boolean, String>>) {
    println("tasks:")
    if (tasks.isEmpty()) {
        println("No tasks available.")
    } else {
        tasks.forEachIndexed { index, (description, isComplete, note) ->
            val status = if (isComplete) "[COMPLETE]" else ""
            val notes = if (note.isNotBlank()) " (note: $note)" else ""
            println("${index + 1}. $status $description$notes")
        }
    }
}

fun showTaskStatistics(tasks: List<Triple<String, Boolean, String>>) {
    val (completed, incomplete) = tasks.partition { it.second }
    println("Completed: ${completed.size} | Incomplete: ${incomplete.size}")
}

// Logic Layer

fun addTask(state: TodoAppState): TodoAppState {
    println("task description:")
    val description = readlnOrNull()?.takeIf { it.isNotBlank() } ?: run {
        println("Task description required")
        return state
    }

    println("enter notes (optional):")
    val note = readlnOrNull() ?: ""
    val newTask = Triple(description, false, note)

    return state.copy(
        tasks = state.tasks + newTask,
        lastAction = LastAction.Add(newTask)
    ).also { println("task added!") }
}

fun markTaskComplete(state: TodoAppState): TodoAppState {
    println("number tasks to mark complete:")
    val taskIndex = validateTaskNumber(state.tasks) ?: return state
    val task = state.tasks[taskIndex]

    val updatedTasks = state.tasks.mapIndexed { index, t ->
        if (index == taskIndex) t.copy(second = true) else t
    }

    return state.copy(
        tasks = updatedTasks,
        lastAction = LastAction.Complete(task)
    ).also { println("task marked as complete!") }
}

fun removeCompletedTasks(state: TodoAppState): TodoAppState {
    val completedTasks = state.tasks.filter { it.second }
    if (completedTasks.isEmpty()) {
        println("No completed tasks to remove!")
        return state
    }

    val updatedTasks = state.tasks.filterNot { it.second }

    return state.copy(
        tasks = updatedTasks,
        lastAction = LastAction.Remove(completedTasks)
    ).also { println("Completed tasks removed!") }
}
// function reconstructs the state by modifying only the relevant fields
fun undoLastAction(state: TodoAppState): TodoAppState {
    return when (val action = state.lastAction) {
        is LastAction.Add -> {
            state.copy(
                tasks = state.tasks - action.task,
                lastAction = LastAction.None
            ).also { println("Last added task removed!") }
        }
        is LastAction.Complete -> {
            val undoneTask = action.task
            val updatedTasks = state.tasks.map {
                if (it == undoneTask) undoneTask.copy(second = false) else it
            }
            state.copy(
                tasks = updatedTasks,
                lastAction = LastAction.None
            ).also { println("Task completion undone!") }
        }
        is LastAction.Remove -> {
            state.copy(
                tasks = state.tasks + action.removedTasks,
                lastAction = LastAction.None
            ).also { println("Removed tasks restored!") }
        }
        LastAction.None -> {
            println("No action to undo")
            state
        }
    }
}

// Data Access Layer

fun loadTasks(): List<Triple<String, Boolean, String>> {
    val file = File("todo.txt")
    return if (file.exists()) {
        file.readLines().map { line ->
            val (description, status, note) = line.split("|")
            Triple(description, status == "complete", note)
        }
    } else {
        emptyList()
    }
}

fun saveTasks(tasks: List<Triple<String, Boolean, String>>) {
    File("todo.txt").writeText(
        tasks.joinToString("\n") { (desc, isComplete, note) ->
            "$desc|${if (isComplete) "complete" else "incomplete"}|$note"
        }
    )
}

// Utilities
// functions like it is stateless and do not mutate any input or global state. They operate solely on the provided arguments
fun validateTaskNumber(tasks: List<Triple<String, Boolean, String>>): Int? {
    val taskNumber = readlnOrNull()?.toIntOrNull()
    return if (taskNumber != null && taskNumber in 1..tasks.size) {
        taskNumber - 1
    } else {
        println("Invalid task number")
        null
    }
}
