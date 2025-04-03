package tinDL.data.tintheweb.DLqueryTask

import tinDL.model.v2.Tasks.Task
import tinDL.model.v2.File.TinFile



class TaskInfoData(val task: Task, ontologyFile: TinFile?, queryFile: TinFile?, transducerFile: TinFile?) {
    val id: Long = task.id;
    val ontology = ontologyFile?.filename
    val query = queryFile?.filename
    val transducer = transducerFile?.filename;
}

