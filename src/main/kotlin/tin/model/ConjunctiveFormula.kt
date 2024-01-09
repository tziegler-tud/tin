package tin.model

class ConjunctiveFormula(
    existentialQuantifiedVariables: MutableSet<String>,
    helperVariables: MutableSet<String>,
    regularPathQuerySourceVariableAssignment: MutableMap<String, String>, // {(z, {(r1.target, r2.target, r3.source, r3.target)})}
    regularPathQueryTargetVariableAssignment: MutableMap<String, String>
)

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
