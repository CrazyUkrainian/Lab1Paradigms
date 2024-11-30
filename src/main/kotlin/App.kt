package org.example
import java.io.File

// immutable tasks
var tasks: List<Triple<String, Boolean, String>> = listOf()
var lastAction: Pair<String, Triple<String, Boolean, String>?>? = null // to track undo

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
    lastAction = "add" to newTask
    println("task added!")
}

fun markTaskComplete() {
    println("number tasks to mark complete:")
    val taskIndex = validateTaskNumber() ?: return
    val task = tasks[taskIndex]

    tasks = tasks.mapIndexed { index, t ->
        if (index == taskIndex) t.copy(second = true) else t
    }
    lastAction = "complete" to task
    println("task marked as complete!")
}

fun removeCompletedTasks() {
    val completedTasks = tasks.filter { it.second }
    if (completedTasks.isEmpty()) {
        println("No completed tasks to remove!")
        return
    }

    tasks = tasks.filterNot { it.second } // immutable update
    lastAction = "remove" to null
    println("completed tasks removed!")
}

fun undoLastAction() {
    if (lastAction == null) {
        println("No action to undo")
        return
    }

    when (lastAction!!.first) {
        "add" -> {
            tasks = tasks - lastAction!!.second!!
        }
        "complete" -> {
            lastAction!!.second?.let { undoneTask ->
                tasks = tasks.map { if (it == undoneTask) undoneTask.copy(second = false) else it }
            }
        }
        "remove" -> println("Undo for removal is not supported")
    }
    lastAction = null
    println("Last action undone!")
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