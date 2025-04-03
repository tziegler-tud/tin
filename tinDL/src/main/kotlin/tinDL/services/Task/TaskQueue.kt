package tinDL.services.Task

import tinDL.model.v2.Tasks.Task
import java.util.*

class TaskQueue {
    private val queue: LinkedList<Long> = LinkedList()

    fun add(task: Task) {
        queue.add(task.id);
    }
    fun add(id: Long) : Boolean {
        return queue.add(id);
    }

    fun remove(id: Long): Boolean {
        return queue.remove(id);
    }

    fun getNext() : Long? {
        return queue.poll();
    }

    fun isEmpty(): Boolean {
        return queue.isEmpty();
    }

    fun toList(): List<Long> {
        return queue.toList();
    }
}