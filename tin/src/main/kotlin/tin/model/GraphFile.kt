package tin.model

import javax.persistence.Column

abstract class GraphFile(

        @Column(nullable = false, length = 100000)
        val contents: String,

        @Column(nullable = false)
        val fileName: String
) : BasicEntity()