package tin.utils

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

inline fun <reified T : Any, S : Any> JpaRepository<T, S>.findByIdentifier(id: S): T = findByIdOrNull(id)
    ?: throw ResourceNotFoundException("There is no ${T::class.simpleName} with id $id")