package tin.services.internal.fileReaders.fileReaderResult

import tin.model.ConjunctiveFormula
import tin.model.ConjunctiveQueryGraphMap
import tin.services.internal.fileReaders.FileReaderWarning

class ConjunctiveQueryFileReaderResult(
    var graphMap: ConjunctiveQueryGraphMap,
    var formula: ConjunctiveFormula?,
    warnings: MutableList<FileReaderWarning>,
    errors: MutableList<FileReaderWarning>,
) : AbstractFileReaderResult(warnings, errors) {
}