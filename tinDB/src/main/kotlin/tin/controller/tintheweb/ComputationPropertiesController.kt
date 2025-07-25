package tin.controller.tintheweb

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import tin.data.tintheweb.queryTask.ComputationPropertiesData
import tin.services.tintheweb.ComputationPropertiesService

@RestController
class ComputationPropertiesController(
    private val computationPropertiesService: ComputationPropertiesService
) {

    @PostMapping("computation-properties/create")
    fun createComputationPropertiesEntity(@RequestBody data: ComputationPropertiesData): ComputationPropertiesData {
        return ComputationPropertiesData(computationPropertiesService.createComputationProperties(data))
    }

    @GetMapping("computation-properties/get-all")
    fun getComputationPropertiesEntities(): List<ComputationPropertiesData> {
        return computationPropertiesService.getAllComputationProperties().map(::ComputationPropertiesData)
    }
}