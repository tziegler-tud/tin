package tinLIB.services.internal.fileReaders

import tinLIB.services.internal.fileReaders.fileReaderResult.FileReaderResult
import tinLIB.services.technical.SystemConfigurationService
import java.io.File
import org.springframework.stereotype.Service

@Service
class OntologyReaderService(
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