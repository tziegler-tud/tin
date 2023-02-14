package Application;

import java.math.BigInteger;

public class Settings {

    //public static String inputFileDirectory = "src/main/resources/input/"; // use this for Intellij runs
    //public static String inputFileDirectory = "resources/input/"; // use this for building the .jar
    public static String inputFileDirectory = ""; // use this for building the .jar

    // public static String inputParserResults = "src/main/resources/input/"; // use this for Intellij runs
    //public static String inputParserResults = "resources/inputParserResults/"; // use this for building the .jar
    public static String inputParserResults = "queries/"; // use this for building the .jar


    // public static String outputFileDirectory = "src/main/resources/output/"; // use this for Intellij runs
     //public static String outputFileDirectory = "resources/output/"; // use this for building the .jar
    public static String outputFileDirectory = ""; // use this for building the .jar

    public static int numberOfMaxNodesPossible;
    public static long restrictionToPreventInfiniteRuns;
    public static int numberOfActualNodes;
    public static int maxIterationStepsInDijkstraLoop;

    public static long preprocessingTime;
    public static long dijkstraProcessingTime;
    public static long combinedTime;
    public static long postprocessingTime;

    public static int numberOfAnswers;

    public static Double largestWeight;

    public static long getPreprocessingTime() {
        return preprocessingTime;
    }

    public static void setPreprocessingTime(long preprocessingTime) {
        Settings.preprocessingTime = preprocessingTime;
    }

    public static long getDijkstraProcessingTime() {
        return dijkstraProcessingTime;
    }

    public static void setDijkstraProcessingTime(long dijkstraProcessingTime) {
        Settings.dijkstraProcessingTime = dijkstraProcessingTime;
    }

    public static long getPostprocessingTime() {
        return postprocessingTime;
    }

    public static void setPostprocessingTime(long postprocessingTime) {
        Settings.postprocessingTime = postprocessingTime;
    }

    public static long getCombinedTime() {
        return combinedTime;
    }

    public static void setCombinedTime(long combinedTime) {
        Settings.combinedTime = combinedTime;
    }

    public static int getNumberOfAnswers() {
        return numberOfAnswers;
    }

    public static void setNumberOfAnswers(int numberOfAnswers) {
        Settings.numberOfAnswers = numberOfAnswers;
    }

    public static int getNumberOfMaxNodesPossible() {
        return numberOfMaxNodesPossible;
    }

    public static void setNumberOfMaxNodesPossible(int numberOfMaxNodesPossible) {
        Settings.numberOfMaxNodesPossible = numberOfMaxNodesPossible;
        Settings.restrictionToPreventInfiniteRuns = (long) numberOfMaxNodesPossible * numberOfMaxNodesPossible * numberOfMaxNodesPossible;
    }

    public static long getRestrictionToPreventInfiniteRuns() {
        return restrictionToPreventInfiniteRuns;
    }

    public static int getNumberOfActualNodes() {
        return numberOfActualNodes;
    }

    public static void setNumberOfActualNodes(int numberOfActualNodes) {
        Settings.numberOfActualNodes = numberOfActualNodes;
    }

    public static Double getLargestWeight() {
        return largestWeight;
    }

    public static void setLargestWeight(Double largestWeight) {
        Settings.largestWeight = largestWeight;
    }

    public static int getMaxIterationStepsInDijkstraLoop() {
        return maxIterationStepsInDijkstraLoop;
    }

    public static void setMaxIterationStepsInDijkstraLoop(int maxIterationStepsInDijkstraLoop) {
        Settings.maxIterationStepsInDijkstraLoop = maxIterationStepsInDijkstraLoop;
    }
}
