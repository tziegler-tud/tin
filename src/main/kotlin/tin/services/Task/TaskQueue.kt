package tin.services.Task

import java.util.*

class TaskQueue {
    private val queue: LinkedList<Task> = LinkedList()

    fun add(task: Task) : Boolean {
        return queue.add(task);
    }

    fun getNext() : Task? {
        return queue.poll();
    }

    fun isEmpty(): Boolean {
        return queue.isEmpty();
    }
}