package tinDL.model

class ConjunctiveFormula(
    val existentiallyQuantifiedVariables: MutableSet<String>,
    val answerVariables: MutableSet<String>,
    val greekLetter: String,
    val regularPathQuerySourceVariableAssignment: MutableMap<String, String>, // {(queryIdentifier, variableName)}
    val regularPathQueryTargetVariableAssignment: MutableMap<String, String>, // {(queryIdentifier, variableName)}
    val conjunctsTripletSet: Set<ConjunctTriplet>,
) {
    override fun equals(other: Any?): Boolean {
        return this.existentiallyQuantifiedVariables == (other as ConjunctiveFormula).existentiallyQuantifiedVariables &&
                this.answerVariables == other.answerVariables &&
                this.greekLetter == other.greekLetter &&
                this.regularPathQuerySourceVariableAssignment == other.regularPathQuerySourceVariableAssignment &&
                this.regularPathQueryTargetVariableAssignment == other.regularPathQueryTargetVariableAssignment &&
                this.conjunctsTripletSet == other.conjunctsTripletSet
    }

    override fun hashCode(): Int {
        var result = existentiallyQuantifiedVariables.hashCode()
        result = 31 * result + answerVariables.hashCode()
        result = 31 * result + greekLetter.hashCode()
        result = 31 * result + regularPathQuerySourceVariableAssignment.hashCode()
        result = 31 * result + regularPathQueryTargetVariableAssignment.hashCode()
        result = 31 * result + conjunctsTripletSet.hashCode()
        return result
    }
}

class ConjunctTriplet(
    val identifier: String,
    val sourceVariable: String,
    val targetVariable: String,
) {
    override fun equals(other: Any?): Boolean {
        return this.identifier == (other as ConjunctTriplet).identifier &&
                this.sourceVariable == other.sourceVariable &&
                this.targetVariable == other.targetVariable
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + sourceVariable.hashCode()
        result = 31 * result + targetVariable.hashCode()
        return result
    }
}

/**
 * exists(x,y).phi(R1(x,z) and R2(y,z) and R3(z,z))
 * table_R1
 * source target cost
 * x      z      1
 *
 * select * from tables where r1.target = r2.target r1.target = r3.target r1.target = r3.source
 */

/**
 * table_R1: worst case: |databaseNodes|^2
 */

/**
 * matching stage:
 * |R1|x|R2|x|R3|
 *
 * (|databaseNodes|^2) ^ |Anzahl Conjuncts|
 *
 *
 */

/**
 * table_R1
 *  source target cost
 *  x       z      1
 *  y       z      2
 *  x       y      3
 *
 * table_R2
 *  source target cost
 *  y       z      1
 *  x       z      2
 *
 * table_R3
 *  source target cost
 *   y       z      1
 *   z       z      2
 *
 * exists(a,b,c).phi( R1(a,b) and R2(b,c) and R3 (c,c) )
 * R1 * R2 * R3
 * a <- x
 * b <- z
 *
 * b <- y
 * c <- z
 *
 * c <- y
 * c <- z
 *
 *  ---------
 *
 *  Each line in R1_table:
 *      set a->line.source
 *      set b->line.target
 *
 *      Each line R2_table:
 *          set b->line2.source
 *          set c->line2.target
 *
 *          für jede variable v in (a,b,c):
 *              überprüfe R1(v) == R2(v) falls v in R1 und v in R2
 */
