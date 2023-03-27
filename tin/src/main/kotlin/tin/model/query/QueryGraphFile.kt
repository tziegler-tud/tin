package tin.model.query

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.GraphFile
import javax.persistence.Entity

@Entity
class QueryGraphFile(
        contents: String,
        fileName: String,
) : GraphFile(contents, fileName) {
    constructor() : this("", "")


}

interface GraphQueryFileRepository : JpaRepository<QueryGraphFile, Long>