package StatsTrackers;

import Algorithms.DijkstraThreshold;
import Application.Settings;
import DataProvider.DataProvider;
import Database.DatabaseGraph;
import ProductAutomatonSpecification.ProductAutomatonConstructor;
import Query.QueryGraph;
import Transducer.TransducerGraph;
import org.javatuples.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsTrackerThreshold implements StatsTracker {
    QueryGraph queryGraph;
    TransducerGraph transducerGraph;
    DatabaseGraph databaseGraph;
    ProductAutomatonConstructor productAutomatonConstructor;
    HashMap<Pair<String, String>, Double> answerMap;

    DijkstraThreshold dijkstraThreshold;
    Double threshold;

    String outputDirectory = Settings.outputFileDirectory;

    public StatsTrackerThreshold(DataProvider dataProvider, Double threshold) {
        this.queryGraph = dataProvider.getQueryGraph();
        this.transducerGraph = dataProvider.getTransducerGraph();
        this.databaseGraph = dataProvider.getDatabaseGraph();

        this.threshold = threshold;

        this.productAutomatonConstructor = new ProductAutomatonConstructor(queryGraph, transducerGraph, databaseGraph);
        this.dijkstraThreshold = new DijkstraThreshold(productAutomatonConstructor, threshold);
        this.answerMap = new HashMap<>();
    }

    @Override
    public void runDijkstra() throws FileNotFoundException {
        PrintStream fileStream = new PrintStream(new FileOutputStream(outputDirectory + "graphs.txt", false));
        PrintStream stdout = System.out;
        System.setOut(fileStream);

        System.out.println("query graph: ");
        queryGraph.printGraph();
        System.out.println("---");

        System.out.println("transducer graph: ");
        transducerGraph.printGraph();
        System.out.println("---");

        System.out.println("database graph: ");
        databaseGraph.printGraph();
        System.out.println("---");

        System.setOut(stdout);
        
        // start of preprocessing
        long startPreprocessing = System.nanoTime();
        productAutomatonConstructor.construct();

        // end of preprocessing
        long elapsedTimeNanoPreprocessing = System.nanoTime() - startPreprocessing; //System.currentTimeMillis() - startPreprocessing;

        // start of Dijkstra
        long start = System.nanoTime(); // System.currentTimeMillis();
        answerMap = dijkstraThreshold.processDijkstraOverAllInitialNodes();

        // end of Dijkstra
        long elapsedTimeNanoDijkstra = System.nanoTime() - start; //System.currentTimeMillis() - start;

        // start of postprocessing
        long startPostProcessing = System.nanoTime();
        answerMap = sortResults(answerMap);

        // end of postprocessing
        long elapsedTimePostProcessing = System.nanoTime() - startPostProcessing;

        // combined total processing
        long elapsedTimeTotalProcessing = System.nanoTime() - startPreprocessing;

        Settings.setPreprocessingTime(elapsedTimeNanoPreprocessing);
        Settings.setDijkstraProcessingTime(elapsedTimeNanoDijkstra);
        Settings.setPostprocessingTime(elapsedTimePostProcessing);
        Settings.setCombinedTime(elapsedTimeTotalProcessing);
        Settings.setNumberOfAnswers(answerMap.size());
        Settings.setNumberOfActualNodes(productAutomatonConstructor.productAutomatonGraph.nodes.size());


        // writeTimeToFile(elapsedTimeNanoDijkstra, elapsedTimeNanoPreprocessing, elapsedTimeTotalNano);

    }

    @Override
    public void writeTimeToFile(long milli, long milliPreprocessing, long milliTotal) {
        File stats = new File(outputDirectory + "computationStats.txt");
        FileWriter out;


        float compTimeMillisPreprocessing = milliPreprocessing;
        float compTimeSecPreprocessing = milliPreprocessing / 1000F;
        float compTimeMinPreprocessing = milliPreprocessing / (60 * 1000F);

        float compTimeMillis = milli;
        float compTimeSec = milli / 1000F;
        float compTimeMin = milli / (60 * 1000F);

        float compTimeMillisTotal = milliTotal;
        float compTimeSecTotal = milliTotal / 1000F;
        float compTimeMinTotal = milliTotal / (60 * 1000F);


        int amountOfNodes = productAutomatonConstructor.productAutomatonGraph.nodes.size();
        try {

            out = new FileWriter(stats, true);

            out.write("amount of actual nodes in the product automaton: " + amountOfNodes + ". \n");
            out.write("note that we used the lazy construction. \n ");
            out.write("\n");
            out.write("some computation time stats. \n");
            out.write("(1) preprocessing (productAutomaton construction \n");
            out.write("   time needed (milliseconds): " + compTimeMillisPreprocessing + " \n");
            out.write("   time needed (seconds): " + compTimeSecPreprocessing + " \n");

            if (compTimeSecPreprocessing > 60.0) {
                out.write("   time needed (minutes): " + compTimeMinPreprocessing + " \n");
            }

            out.write("(2) dijkstra processing \n");
            out.write("   time needed (milliseconds): " + compTimeMillis + " \n");
            out.write("   time needed (seconds): " + compTimeSec + " \n");
            if (compTimeSec > 60.0) {
                out.write("   time needed (minutes): " + compTimeMin + " \n");
            }
            out.write("(3) combined (preprocessing and dijkstra) \n");
            out.write("   time needed (milliseconds): " + compTimeMillisTotal + " \n");
            out.write("   time needed (seconds): " + compTimeSecTotal + " \n");
            if (compTimeSecTotal > 60.0) {
                out.write("   time needed (minutes): " + compTimeMinTotal + " \n");
            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printEndResult() throws FileNotFoundException {

        writeResultToFile();

        System.out.println("------------------");
        System.out.println("end result: ");
        for (Pair pair : answerMap.keySet()) {
            System.out.println("(" + pair.getValue0().toString() + ", " + pair.getValue1().toString() + ") with cost " + answerMap.get(pair));
        }
        System.out.println("computation completed.");

    }

    @Override
    public void writeResultToFile() throws FileNotFoundException {

        File queryAnswers = new File(outputDirectory + "queryResults.txt");
        FileWriter out;
        try {
            out = new FileWriter(queryAnswers, false);
            out.write("query processed. \n");
            out.write("restriction: threshold t = " + threshold + " \n");

            for (Pair pair : answerMap.keySet()) {
                out.write("(" + pair.getValue0().toString() + ", " + pair.getValue1().toString() + ") with cost " + answerMap.get(pair) + "\n");
            }

            out.write("total answers: " + answerMap.size());

            out.close();
            System.out.println("successfully wrote to file.");

        } catch (IOException e) {
            System.out.println("error.");
            e.printStackTrace();
        }

        PrintStream fileStream = new PrintStream(new FileOutputStream(outputDirectory + "graphs.txt", true));
        PrintStream stdout = System.out;
        System.setOut(fileStream);
        System.out.println("product automaton: ");
        productAutomatonConstructor.productAutomatonGraph.printGraph();
        System.setOut(stdout);
    }

    private HashMap<Pair<String, String>, Double> sortResults(HashMap<Pair<String, String>, Double> hm) {

        return hm.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
    }
}
