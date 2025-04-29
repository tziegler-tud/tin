package tinDB.services.tintheweb

import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import tinDB.data.tintheweb.queryTask.ComputationPropertiesData
import tinDB.model.v1.queryTask.ComputationProperties
import tinDB.model.v1.queryTask.ComputationPropertiesRepository

@Service
class ComputationPropertiesService(
    private val computationPropertiesRepository: ComputationPropertiesRepository
) {
    fun createComputationProperties(@RequestBody data: ComputationPropertiesData): ComputationProperties {
        val newComputationProperties = ComputationProperties(
            topKValue = data.topKValue,
            computationModeEnum = data.computationModeEnum,
            thresholdValue = data.thresholdValue,
            generateTransducer = data.generateTransducer,
            transducerGeneration = data.transducerGeneration,
            name = data.name
        )

        return computationPropertiesRepository.save(newComputationProperties)
    }

    fun getAllComputationProperties(): List<ComputationProperties> {
        return computationPropertiesRepository.findAll()
    }
}