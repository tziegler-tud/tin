package tinCORE.data.Task

import tinCORE.data.File.TinFile
import tinCORE.data.Task.Task


class TaskInfoData(val task: Task, dataSourceFile: TinFile?, queryFile: TinFile?, transducerFile: TinFile?) {
    val id: Long = task.id;
    val taskType = task.taskType
    val dataSource = dataSourceFile?.filename
    val query = queryFile?.filename
    val transducer = transducerFile?.filename;
}

