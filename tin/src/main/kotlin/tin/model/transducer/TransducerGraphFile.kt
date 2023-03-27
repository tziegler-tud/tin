package tin.model.transducer

import org.springframework.data.jpa.repository.JpaRepository
import tin.model.GraphFile
import javax.persistence.Entity

@Entity
class TransducerGraphFile(
        contents: String,
        fileName: String,
) : GraphFile(contents, fileName) {
    constructor() : this("", "")


}

interface TransducerGraphFileRepository : JpaRepository<TransducerGraphFile, Long>