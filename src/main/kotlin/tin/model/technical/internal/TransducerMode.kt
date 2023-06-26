package tin.model.technical.internal

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class TransducerMode() {


    @GeneratedValue
    @Id
    val id: Long = 0
}

interface TransducerModeRepository : JpaRepository<TransducerMode, Long>