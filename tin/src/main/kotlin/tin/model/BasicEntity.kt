package tin.model

import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BasicEntity(
        val createdAt: Date = Date()
) {
    @GeneratedValue
    @Id
    val id: Long = 0
}