package tin.services.technical

import org.springframework.stereotype.Service

@Service
class SystemConfigurationServiceTest {
    private val projectPath = "C:/dev/tin"

    val uploadPathForQueries = "$projectPath/src/test/resources/input/queries/"
    val uploadPathForDatabases = "$projectPath/src/test/resources/input/databases/"
    val uploadPathForTransducers = "$projectPath/src/test/resources/input/transducers/"
}