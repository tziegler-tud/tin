package tinDB.model.v1.utils

class PairOfStrings(
    val first: String,
    val second: String,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PairOfStrings) return false
        return first == other.first && second == other.second
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + second.hashCode()
        return result
    }

    override fun toString(): String {
        return "$first -> $second"
    }

    companion object {
        fun fromString(string: String): PairOfStrings {
            val parts = string.split(" -> ")
            return PairOfStrings(parts[0], parts[1])
        }
    }


}