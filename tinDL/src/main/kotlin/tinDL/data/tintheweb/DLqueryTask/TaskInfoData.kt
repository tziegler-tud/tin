package tinDL.data.tintheweb.DLqueryTask

import tinDL.data.Task.DlTask.DlTask
import tinDL.model.v2.File.TinFile



class TaskInfoData(val task: DlTask, ontologyFile: TinFile?, queryFile: TinFile?, transducerFile: TinFile?) {
    val id: Long = task.id;
    val ontology = ontologyFile?.filename
    val query = queryFile?.filename
    val transducer = transducerFile?.filename;
}

