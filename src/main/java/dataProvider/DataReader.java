package dataProvider;

import application.Settings;
import typeSpecifications.databaseSpecification.DatabaseGraph;
import typeSpecifications.databaseSpecification.DatabaseNode;
import typeSpecifications.querySpecification.QueryGraph;
import typeSpecifications.querySpecification.QueryNode;
import typeSpecifications.transducerSpecification.TransducerGraph;
import typeSpecifications.transducerSpecification.TransducerNode;

import java.io.*;
import java.util.*;

public class DataReader {
    private String inputFilePath;
    private boolean transducerAutoGeneration;
    private DataProvider dataProvider;
    private HashMap<String, Integer> amountOfNodesMap;
    private String outputDirectory;

    public DataReader(String inputFile, boolean transducerAutoGeneration) {
        this.inputFilePath = inputFile;
        this.transducerAutoGeneration = transducerAutoGeneration;
        dataProvider = new DataProvider();
        dataProvider.alphabet = new HashSet<>();
        amountOfNodesMap = new HashMap<>();
        this.outputDirectory = Settings.outputFileDirectory;
    }

    public void readFile() throws Exception {

        try {
            File inputFile = new File(inputFilePath);
            BufferedReader br = new BufferedReader(new FileReader(inputFile));

            //System.out.println(path);
            //InputStream in = getClass().getResourceAsStream("query-3.txt");
            //BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String st;
            String[] words;
            int amountOfNodes;

            boolean queryEdgeFinder = false;
            boolean transducerEdgeFinder = false;
            boolean databaseEdgeFinder = false;

            while ((st = br.readLine()) != null) {

                // start with finding out about the name of the data set
                if (st.contains("name:")) {
                    // System.out.println("Found 'name:' ");
                    words = st.split("name: ");
                    dataProvider.setDataSetIdentifier(words[1].trim());
                    st = br.readLine();
                }

                // read all the data for the queryGraph...
                if (st.contains("queryGraph:")) {
                    // System.out.println("Found 'queryGraph:' ");
                    ArrayList<String> queryGraphData = new ArrayList<>();
                    amountOfNodes = 0;
                    st = br.readLine();
                    if (st.contains("nodes:")) {
                        st = br.readLine();
                    }
                    do {
                        if (!st.contains("edges:") && !queryEdgeFinder) {
                            amountOfNodes++;
                        } else queryEdgeFinder = true;

                        queryGraphData.add(st);
                        st = br.readLine();

                    } while (!st.contains("transducerGraph:"));
                    processQueryGraphData(queryGraphData, amountOfNodes);

                }

                // read all the data for the transducer graph.
                if (st.contains("transducerGraph:")) {

                    if (transducerAutoGeneration) {
                        // System.out.println("Transducer info skipped. Initializing auto generation of transducer ...");
                        createTransducerPreservingClassicalAnswers();

                        do {
                            st = br.readLine();

                        } while (!st.contains("databaseGraph:"));

                        // System.out.println(" ... done. Proceeding with database data.");

                    } else {

                        // System.out.println("Found 'transducerGraph:' ");
                        ArrayList<String> transducerGraphData = new ArrayList<>();
                        amountOfNodes = 0;
                        st = br.readLine();

                        if (st.contains("nodes:")) {
                            st = br.readLine();
                        }
                        do {
                            if (!st.contains("edges:") && !transducerEdgeFinder) {
                                amountOfNodes++;
                            } else transducerEdgeFinder = true;

                            transducerGraphData.add(st);
                            st = br.readLine();

                        } while (!st.contains("databaseGraph:"));
                        processTransducerGraphData(transducerGraphData, amountOfNodes);
                    }
                }

                // read all the data for the database graph.
                if (st.contains("databaseGraph:")) {
                    // System.out.println("Found 'databaseGraph:' ");
                    ArrayList<String> databaseGraphData = new ArrayList<>();
                    amountOfNodes = 0;
                    st = br.readLine();

                    if (st.contains("nodes:")) {
                        st = br.readLine();
                    }
                    do {
                        if (!st.contains("edges:") && !databaseEdgeFinder) {
                            amountOfNodes++;
                        } else databaseEdgeFinder = true;

                        databaseGraphData.add(st);
                        // st = br.readLine();

                    } while ((st = br.readLine()) != null);
                    processDatabaseGraphData(databaseGraphData, amountOfNodes);
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //int maxNodeAmountTotal = Integer.MAX_VALUE;
        int maxNodeAmountTotal = amountOfNodesMap.get("query") * amountOfNodesMap.get("transducer") * amountOfNodesMap.get("database");
        Settings.setNumberOfMaxNodesPossible(maxNodeAmountTotal);

        /* todo: add for debugging if needed
        File stats = new File(outputDirectory + "computationStats.txt");
        FileWriter out;

        try {
            out = new FileWriter(stats, false);
            out.write("max amount of possible nodes in the product automaton: " + maxNodeAmountTotal + ". \n");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } */
    }

    private void processQueryGraphData(ArrayList<String> queryData, int amountOfNodes) {

        HashMap<String, QueryNode> queryNodes = new HashMap<>();  // map containing the QueryNodes
        QueryNode source;
        QueryNode target;
        QueryNode node;
        String label;
        String[] strArray; // working array ...

        amountOfNodesMap.put("query", amountOfNodes);
        // looping over all nodes
        for (int i = 0; i < amountOfNodes; i++) {

            strArray = queryData.get(i).split(",");
            strArray = removeWhiteSpace(strArray);

            node = new QueryNode(strArray[0], Boolean.parseBoolean(strArray[1]), Boolean.parseBoolean(strArray[2]));
            queryNodes.put(strArray[0], node);

        }


        // looping over all edges
        for (int i = amountOfNodes + 1; i < queryData.size(); i++) {
            strArray = queryData.get(i).split(",");
            strArray = removeWhiteSpace(strArray);

            source = queryNodes.get(strArray[0]);
            target = queryNodes.get(strArray[1]);
            label = strArray[2];

            dataProvider.alphabet.add(label);
            dataProvider.queryGraph.addQueryEdge(source, target, label);
        }
    }


    private void processTransducerGraphData(ArrayList<String> transducerData, int amountOfNodes) {
        HashMap<String, TransducerNode> transducerNodes = new HashMap<>();  // map containing the transducerNodes
        TransducerNode source;
        TransducerNode target;
        TransducerNode node;
        String incoming;
        String outgoing;
        int cost;

        amountOfNodesMap.put("transducer", amountOfNodes);
        String[] strArray; // working array ...

        // looping over all nodes
        for (int i = 0; i < amountOfNodes; i++) {
            strArray = transducerData.get(i).split(",");
            strArray = removeWhiteSpace(strArray);

            node = new TransducerNode(strArray[0], Boolean.parseBoolean(strArray[1]), Boolean.parseBoolean(strArray[2]));
            transducerNodes.put(strArray[0], node);
        }

        // looping over all edges
        for (int i = amountOfNodes + 1; i < transducerData.size(); i++) {
            strArray = transducerData.get(i).split(",");
            strArray = removeWhiteSpace(strArray);

            source = transducerNodes.get(strArray[0]);
            target = transducerNodes.get(strArray[1]);

            // replace ε with empty string ""
            if (strArray[2].equals("ε")) {
                incoming = "";
            } else incoming = strArray[2];

            // replace ε with empty string ""
            if (strArray[3].equals("ε")) {
                outgoing = "";
            } else outgoing = strArray[3];

            cost = Integer.parseInt(strArray[4]);

            dataProvider.transducerGraph.addTransducerObjectEdge(source, target, incoming, outgoing, cost);
        }

    }

    private void processDatabaseGraphData(ArrayList<String> databaseData, int amountOfNodes) {
        HashMap<String, DatabaseNode> databaseNodes = new HashMap<>();  // map containing the databaseNodes
        DatabaseNode source;
        DatabaseNode target;
        DatabaseNode node;
        String label;

        String[] strArray; // working array ...
        amountOfNodesMap.put("database", amountOfNodes);
        // looping over all nodes
        for (int i = 0; i < amountOfNodes; i++) {

            strArray = databaseData.get(i).split(",");
            strArray = removeWhiteSpace(strArray);

            node = new DatabaseNode(strArray[0]);
            databaseNodes.put(strArray[0], node);

        }

        // looping over all edges
        for (int i = amountOfNodes + 1; i < databaseData.size(); i++) {
            strArray = databaseData.get(i).split(",");
            strArray = removeWhiteSpace(strArray);

            source = databaseNodes.get(strArray[0]);
            target = databaseNodes.get(strArray[1]);
            label = strArray[2];

            dataProvider.databaseGraph.addDatabaseObjectEdge(source, target, label);
        }
    }

    /**
     * auto-generation of a transducer. this transducer will only preserve classical answers.
     * idea: having one node (initial and final) and n self-loops (alphabet size = n)
     * We need this style since otherwise we don't get classical answers of size > 1.
     * (or we have to make every node initial and final...)
     */
    private void createTransducerPreservingClassicalAnswers() {

        TransducerNode source = new TransducerNode("t0", true, true);

        for (String word : dataProvider.alphabet) {
            // for each element of the alphabet we add the edge (t0, t0, element, element, 0)
            dataProvider.transducerGraph.addTransducerObjectEdge(source, source, word, word, 0);
        }

        amountOfNodesMap.put("transducer", 1);

    }

    private String[] removeWhiteSpace(String[] words) {
        String[] wordsWithoutWhitespace = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            wordsWithoutWhitespace[i] = words[i].trim();
        }
        return wordsWithoutWhitespace;
    }

    public void printData() throws FileNotFoundException {


        try {

            QueryGraph queryGraph = dataProvider.getQueryGraph();
            TransducerGraph transducerGraph = dataProvider.getTransducerGraph();
            DatabaseGraph databaseGraph = dataProvider.getDatabaseGraph();

            PrintStream fileStream = new PrintStream(outputDirectory + "parsedInputData.txt");
            PrintStream stdout = System.out;
            System.setOut(fileStream);


            System.out.println("query graph: ");
            queryGraph.printGraph();
            printSpace();

            System.out.println("transducer graph: ");
            transducerGraph.printGraph();
            printSpace();

            System.out.println("database graph: ");
            databaseGraph.printGraph();
            printSpace();

            System.setOut(stdout);

        } catch (IOException e) {
            System.out.println("error.");
            e.printStackTrace();
        }

    }

    public static void printSpace() {
        System.out.println("---");
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

}
