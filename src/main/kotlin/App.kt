package org.example
import java.io.File

// immutable task list
val tasks = mutableListOf<Triple<String, Boolean, String>>() // Adding notes as third element
var lastAction: Pair<String, Triple<String, Boolean, String>?>? = null // for additional undo function

fun main() {
    loadTasks()
    println("Welcome to the best one Kotlin ToDo list app")
    var input: String?

    while (true) {
        displayTasks()
        showTaskStatistics() // Display task statistics
        println("Your action, amigo?")
        println("a - add task / c - complete task / r - remove completed tasks / u - undo last / q - quit")

        input = readlnOrNull()?.lowercase()
        when (input) {
            "a" -> addTask()
            "c" -> markTaskComplete()
            "r" -> removeCompletedTasks()
            "u" -> undoLastAction()
            "q" -> {
                saveTasks()
                println("Gladly deserve an A+ for sure, right? Haha, end!")
                break
            }
            else -> println("Invalid input")
        }
    }
}

fun displayTasks() {
    println("tasks:")
    if (tasks.isEmpty()) {
        println("No tasks available.")
    } else {
        tasks.forEachIndexed { index, task ->
            val status = if (task.second) "[COMPLETE]" else ""
            val note = if (task.third.isNotBlank()) " (note: ${task.third})" else ""
            println("${index + 1}. $status ${task.first}$note")
        }
    }
}

fun addTask() {
    println("enter task description :")
    val description = readlnOrNull()
    if (description.isNullOrBlank()) {
        println("Task gotta have description")
        return
    }

    println("enter notes for this task (can skip)>")
    val note = readlnOrNull() ?: ""

    val newTask = Triple(description, false, note)
    tasks.add(newTask)
    lastAction = "add" to newTask
    println("task added!")
}

fun markTaskComplete() {
    println("enter the number of the task to complete >")
    val taskNumber = readlnOrNull()?.toIntOrNull()
    if (taskNumber == null || taskNumber !in 1..tasks.size) {
        println("invalid number")
    } else {
        val task = tasks[taskNumber - 1]
        tasks[taskNumber - 1] = task.copy(second = true)
        lastAction = "complete" to task
        println("Task completed!")
    }
}

fun removeCompletedTasks() {
    val completedTasks = tasks.filter { it.second }
    tasks.removeAll(completedTasks)
    lastAction = "remove" to null
    println("completed tasks removed!")
}

// experimental undo function, undoing your last action
fun undoLastAction() {
    if (lastAction == null) {
        println("No action to undo!")
        return
    }

    when (lastAction!!.first) {
        "add" -> tasks.remove(lastAction!!.second)
        "complete" -> {
            val task = lastAction!!.second!!
            val index = tasks.indexOfFirst { it == task }
            if (index != -1) {
                // Assume tasks are stored as a mutable list and you want to mark it as incomplete
                tasks[index] = task // This should mark the task as incomplete
            } else {
                println("Task not found to undo completion!")
            }
        }
        "remove" -> println("Cannot undo removal of multiple tasks!")
    }
    lastAction = null
    println("Last action undone!")
}


// just a counter
fun showTaskStatistics() {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.second }
    val incompleteTasks = totalTasks - completedTasks

    println("Completed: $completedTasks | Incomplete: $incompleteTasks")
}

fun loadTasks() {
    val file = File("todo.txt")
    if (file.exists()) {
        file.forEachLine {
            val (description, status, note) = it.split("|")
            tasks.add(Triple(description, status == "complete", note))
        }
    }
}

fun saveTasks() {
    val file = File("todo.txt")
    file.writeText("")
    tasks.forEach { (description, isComplete, note) ->
        val status = if (isComplete) "complete" else "incomplete"
        file.appendText("$description|$status|$note\n")
    }
}


