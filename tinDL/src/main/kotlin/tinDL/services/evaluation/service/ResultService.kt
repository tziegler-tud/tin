package tinDL.services.evaluation.service

import tinDL.services.evaluation.data.RunResult
import java.util.Date
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ResultService {

    var results: MutableList<RunResult> = mutableListOf()

    fun createResult(
        onotlogy: String,
        query: String,
        transducer: String,
    ) : RunResult {
        val result = RunResult(
            identifier = createIdentifier(),
            onotlogy,
            query,
            transducer,
            initiated = Date(),
            finished = Date(),
            isFinished = false,
        );
        results.add(result);
        return result;
    }

    private fun createIdentifier(): Uuid {
        return Uuid.random();
    }


}