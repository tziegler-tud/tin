package tin.model.database

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.GraphFile
import javax.persistence.Entity

@Entity
class DatabaseGraphFile(
        contents: String,
        fileName: String,
) : GraphFile(contents, fileName) {
    constructor() : this("", "")


}

interface DatabaseGraphFileRepository : JpaRepository<DatabaseGraphFile, Long>