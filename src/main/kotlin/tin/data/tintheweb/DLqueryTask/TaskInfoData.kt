package tin.data.tintheweb.DLqueryTask

import tin.model.v2.File.TinFile
import tin.model.v2.Tasks.Task


class TaskInfoData(val task: Task, ontologyFile: TinFile?, queryFile: TinFile?, transducerFile: TinFile?) {
    val id: Long = task.id;
    val ontology = ontologyFile?.filename
    val query = queryFile?.filename
    val transducer = transducerFile?.filename;
}

