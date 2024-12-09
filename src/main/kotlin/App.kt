package org.example
import java.io.File

// immutable tasks
var tasks: List<Triple<String, Boolean, String>> = listOf()
sealed class LastAction {
    data class Add(val task: Triple<String, Boolean, String>) : LastAction()
    data class Complete(val task: Triple<String, Boolean, String>) : LastAction()
    data class Remove(val removedTasks: List<Triple<String, Boolean, String>>) : LastAction() // Store removed tasks
    object None : LastAction() // Default state, no last action
}


var lastAction: LastAction = LastAction.None // Initialize with no action
fun main() {
    tasks = loadTasks() // load tasks from file
    println("welcome to the best kotlin ToDo app")

    while (true) {
        displayTasks() // UI layer
        showTaskStatistics() // stats display in UI
        println("Choose action: a - add / c - complete / r - remove / u - undo / q - quit")
        when (readlnOrNull()?.lowercase()) {
            "a" -> addTask()
            "c" -> markTaskComplete()
            "r" -> removeCompletedTasks()
            "u" -> undoLastAction()
            "q" -> {
                saveTasks(tasks)
                println("goodbye!")
                break
            }
            else -> println("Invalid input")
        }
    }
}

// UI Layer

fun displayTasks() {
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

fun showTaskStatistics() {
    val (completed, incomplete) = tasks.partition { it.second }
    println("Completed: ${completed.size} | Incomplete: ${incomplete.size}")
}

// Logic Layer

fun addTask() {
    println("task description:")
    val description = readlnOrNull()?.takeIf { it.isNotBlank() } ?: run {
        println("Task description required")
        return
    }

    println("enter notes (optional):")
    val note = readlnOrNull() ?: ""
    val newTask = Triple(description, false, note)

    tasks = tasks + newTask // immutable update
    lastAction = LastAction.Add(newTask)
    println("task added!")
}

fun markTaskComplete() {
    println("number tasks to mark complete:")
    val taskIndex = validateTaskNumber() ?: return
    val task = tasks[taskIndex]

    tasks = tasks.mapIndexed { index, t ->
        if (index == taskIndex) t.copy(second = true) else t
    }
    lastAction = LastAction.Complete(task)
    println("task marked as complete!")
}

fun removeCompletedTasks() {
    val completedTasks = tasks.filter { it.second }
    if (completedTasks.isEmpty()) {
        println("No completed tasks to remove!")
        return
    }

    tasks = tasks.filterNot { it.second } // Immutable update
    lastAction = LastAction.Remove(completedTasks) // Store removed tasks for undo
    println("Completed tasks removed!")
}

fun undoLastAction() {
    when (lastAction) {
        is LastAction.Add -> {
            tasks = tasks - (lastAction as LastAction.Add).task
            println("Last added task removed!")
        }
        is LastAction.Complete -> {
            val undoneTask = (lastAction as LastAction.Complete).task
            tasks = tasks.map { if (it == undoneTask) undoneTask.copy(second = false) else it }
            println("Task completion undone!")
        }
        is LastAction.Remove -> {
            val removedTasks = (lastAction as LastAction.Remove).removedTasks
            tasks = tasks + removedTasks // Re-add removed tasks
            println("Removed tasks restored!")
        }
        LastAction.None -> {
            println("No action to undo")
        }
    }
    lastAction = LastAction.None
}

// data access layer

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

// utilities

fun validateTaskNumber(): Int? {
    val taskNumber = readlnOrNull()?.toIntOrNull()
    return if (taskNumber != null && taskNumber in 1..tasks.size) {
        taskNumber - 1
    } else {
        println("Invalid task number")
        null
    }
}