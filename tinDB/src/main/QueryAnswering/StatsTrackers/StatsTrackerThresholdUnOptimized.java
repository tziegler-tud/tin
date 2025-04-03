package StatsTrackers;

import Algorithms.DijkstraClassic;
import Application.Settings;
import DataProvider.DataProvider;
import Database.DatabaseGraph;
import ProductAutomatonSpecification.ProductAutomatonConstructor;
import Query.QueryGraph;
import Transducer.TransducerGraph;
import org.javatuples.Pair;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class StatsTrackerThresholdUnOptimized implements StatsTracker {

    QueryGraph queryGraph;
    TransducerGraph transducerGraph;
    DatabaseGraph databaseGraph;
    ProductAutomatonConstructor productAutomatonConstructor;
    HashMap<Pair<String, String>, Double> answerMap;

    DijkstraClassic dijkstraClassic;
    Double threshold;

    String outputDirectory = Settings.outputFileDirectory;

    public StatsTrackerThresholdUnOptimized(DataProvider dataProvider, Double threshold) {
        this.queryGraph = dataProvider.getQueryGraph();
        this.transducerGraph = dataProvider.getTransducerGraph();
        this.databaseGraph = dataProvider.getDatabaseGraph();

        this.threshold = threshold;
        this.productAutomatonConstructor = new ProductAutomatonConstructor(queryGraph, transducerGraph, databaseGraph);
        this.dijkstraClassic = new DijkstraClassic(productAutomatonConstructor);
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
        System.out.println("start of preprocessing");
        productAutomatonConstructor.construct();

        // end of preprocessing
        System.out.println("end of preprocessing");
        long elapsedTimeNanoPreprocessing = System.nanoTime() - startPreprocessing; //System.currentTimeMillis() - startPreprocessing;

        // start of Dijkstra
        System.out.println("start of dijkstra");
        long start = System.nanoTime(); // System.currentTimeMillis();

        answerMap = dijkstraClassic.processDijkstraOverAllInitialNodes();

        // end of Dijkstra
        System.out.println("end of dijkstra");
        long elapsedTimeNanoDijkstra = System.nanoTime() - start; //System.currentTimeMillis() - start;

        // start of postprocessing
        System.out.println("start of postprocessing");
        long startPostProcessing = System.nanoTime();

        answerMap = sortAndThreshold(answerMap, threshold); // obtain only the results inside the threshold

        // end of postprocessing
        System.out.println("end of postprocessing");
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
        // writeResultToFile();

    }

    @Override
    public void writeTimeToFile(long milli, long milliPreprocessing, long milliTotal) {

    }

    @Override
    public void printEndResult() throws FileNotFoundException {

    }

    @Override
    public void writeResultToFile() throws FileNotFoundException {

    }

    private HashMap<Pair<String, String>, Double> sortAndThreshold(HashMap<Pair<String, String>, Double> hm, Double threshold) {

        HashMap<Pair<String, String>, Double> temp = hm.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));

        temp.entrySet().removeIf(entry -> entry.getValue() >= threshold);

        return temp;
    }
}
