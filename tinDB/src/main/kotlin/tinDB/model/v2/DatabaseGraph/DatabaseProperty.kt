package tinDB.model.v2.DatabaseGraph

class DatabaseProperty(val label: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DatabaseProperty) return false
        return other.label === label;
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    override fun toString(): String {
        return label
    }
}