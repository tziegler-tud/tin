package tin.services.evaluation.data

import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RunResult @OptIn(ExperimentalUuidApi::class) constructor(
    private val identifier: Uuid,
    private val ontology: String,
    private val query: String,
    private val transducer: String,
    private val initiated: Date,
    private val finished: Date,
    private val isFinished: Boolean,
){

}