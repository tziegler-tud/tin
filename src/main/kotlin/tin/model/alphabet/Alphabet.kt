package tin.model.alphabet

/**
 * We choose to implement our own class for alphabets to facilitate its usage in our code.
 * However, we have to preserve upper complexity bounds for alphabet operations, or else we will violate our theoretical runtime
 * By keeping two sets of concept names in storage, we do violate space complexity, but in a way that should not affect our results as long as they are > O(1)
 */
class Alphabet {

    companion object {
        /**
         * transforms a concept name into the target representation. Ususally, this will be "<conceptName>?"
         * needs to run in O(1)!
         */
        fun transformConceptName(conceptName: String): String {
            return "$conceptName?"
        }
        fun transformConceptNames(conceptNames: HashSet<String>) : HashSet<String> {
            return conceptNames.mapTo(HashSet<String>()){transformConceptName(it)}
        }
    }

    private var roleNames: HashSet<String>
    private var conceptNames: HashSet<String>
    private var transformedConceptNames: HashSet<String>

    constructor(){
        this.roleNames = HashSet();
        this.conceptNames = HashSet();
        this.transformedConceptNames = HashSet();
    }
    constructor(alphabet: Alphabet){
        this.roleNames = alphabet.getRoleNames();
        this.conceptNames = alphabet.getConceptNames();
        this.transformedConceptNames = alphabet.getTransformedConceptNames();
    }

    public fun addRoleName(roleName: String) {
        roleNames.add(roleName)
    }

    public fun addAlphabet(alphabet: Alphabet){
        this.roleNames.plus(alphabet.getRoleNames());
        this.conceptNames.plus(alphabet.getConceptNames())
        this.transformedConceptNames.plus(alphabet.transformedConceptNames);
    }

    public fun addConceptName(conceptName: String) {
        conceptNames.add(conceptName)
        transformedConceptNames.add(transformConceptName(conceptName));
    }

    public fun includes(identifier: String): Boolean {
        //this is an alphabet lookup, needs to be in O(1)!
        return if (this.roleNames.contains(identifier)) true
        else this.transformedConceptNames.contains(identifier);
    }

    /**
     * Runs in O(n) in data complexity.
     * this should work for now, but violates upper complexity bounds. Be careful when using and keep in mind.
     * TODO: Determine implementation requirements and then migrate to an optimal alphabet representation
     */
    public fun getAlphabetSuboptimal(): Set<String> {
        return this.roleNames.plus(this.conceptNamesToAssertions())
    }

    /**
     * Runs in O(1), but requires O(n) space for the alphabet. Keep in mind.
     */
    public fun getAlphabet(): Set<String> {
        return this. roleNames.plus(this.transformedConceptNames);
    }

    public fun getRoleNames(): HashSet<String> {
        return this.roleNames;
    }

    public fun getConceptNames(): HashSet<String> {
        return this.conceptNames;
    }

    public fun getTransformedConceptNames(): HashSet<String> {
        return this.transformedConceptNames;
    }
    private fun conceptNamesToAssertions() : HashSet<String> {
        return transformConceptNames(this.conceptNames);
    }

    override fun equals(other: Any?): Boolean {
        return (other is Alphabet)
                && this.roleNames == other.getRoleNames()
                && this.conceptNames == other.getConceptNames()
    }
    override fun hashCode(): Int {
        return this.roleNames.hashCode() + this.conceptNames.hashCode()
    }
}