package tinDL.services.internal.fileReaders.fileReaderResult

import tinDL.model.ConjunctiveFormula
import tinDL.model.ConjunctiveQueryGraphMap
import tinDL.services.internal.fileReaders.FileReaderWarning

class ConjunctiveQueryFileReaderResult(
    var graphMap: ConjunctiveQueryGraphMap,
    var formula: ConjunctiveFormula,
    warnings: MutableList<FileReaderWarning>,
    errors: MutableList<FileReaderWarning>,
) : AbstractFileReaderResult(warnings, errors) {

    override fun equals(other: Any?): Boolean {
        return this.graphMap == (other as ConjunctiveQueryFileReaderResult).graphMap
                && this.formula == other.formula
    }

    override fun hashCode(): Int {
        var result = graphMap.hashCode()
        result = 31 * result + formula.hashCode()
        return result
    }
}