package tin.services.technical

import org.springframework.stereotype.Service

@Service
class SystemConfigurationService {
    private val projectPath = "C:/dev/tin"

    val uploadPathForQueries = "$projectPath/src/main/resources/input/queries"
    val uploadPathForDatabases = "$projectPath/src/main/resources/input/databases"
}