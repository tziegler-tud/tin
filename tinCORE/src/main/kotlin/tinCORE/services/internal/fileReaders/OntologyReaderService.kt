package tinCORE.services.internal.fileReaders

import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import org.springframework.stereotype.Service
import tinCORE.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinCORE.services.technical.SystemConfigurationService

@Service
class OntologyReaderService @Autowired constructor(
    systemConfigurationService: SystemConfigurationService
) : FileReaderService<FileReaderResult<File>>(
    systemConfigurationService
) {

    override var filePath = systemConfigurationService.getOntologyPath();
    override var inputFileMaxLines : Int = systemConfigurationService.getOntologySizeLimit()


    override fun processFile(file: File, breakOnError: Boolean): FileReaderResult<File> {
        return FileReaderResult<File>(file, this.warnings, this.errors);
    }
}