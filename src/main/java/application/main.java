package application;

import dataProvider.DataProvider;
import dataProvider.DataReader;


class main implements Runnable {

   // @CommandLine.Option(names = {"-o", "--output"}, arity = "0..1", description = "specifies the output directory")
    private String outputDirectory;

   // @CommandLine.Option(names = {"-i", "--input"}, arity = "1", description = "specifies the input file")
    private String inputFile;

  //  @CommandLine.Option(names = {"-m", "--mode"}, arity = "1..2", split = " ", description = "specifies the computation mode. split values with space.")
    private String[] computationMode; // possible 2 values: compMode, doubleValue. e.g. "topK 50"

  //  @CommandLine.Option(names = {"--generateTransducer"}, description = "when set, a trivial transducer is generated.")
    private boolean generateTransducer;

    DataReader dataReader;
    SearchHandler searchHandler;

    public void initializeWithInputParams() throws Exception {

        Settings.setOutputFileDirectory(outputDirectory);

        if (generateTransducer) {
            dataReader = new DataReader(inputFile, true);
        } else {
            dataReader = new DataReader(inputFile, false);
        }


        searchHandler = new SearchHandler(dataReader.getDataProvider());

        dataReader.readFile();
        dataReader.printData();

        DataProvider dataProvider = dataReader.getDataProvider();

        switch (computationMode[0].toLowerCase()) {
            case "classic":
                searchHandler.searchAllAnswers(dataProvider);
                break;
            case "topkoptimized":
                searchHandler.searchTopKAnswersOptimized(dataProvider, Integer.parseInt(computationMode[1]));
                break;
            case "topk":
                searchHandler.searchTopKAnswersNaive(dataProvider, Integer.parseInt(computationMode[1]));
                break;
            case "thresholdoptimized":
                searchHandler.searchThresholdAnswersOptimized(dataProvider, Double.parseDouble(computationMode[1]));
                break;
            case "threshold":
                searchHandler.searchThresholdAnswersNaive(dataProvider, Double.parseDouble(computationMode[1]));
                break;
            case "thresholdlw":
                searchHandler.searchLargestWeight(dataProvider);
                break;
            default:
                System.out.println("invalid input. restart and enter a valid input. Check ReadMe for more info.");
                break;
        }

    }

    /**
     * Application.Application.main function which is executed.
     * We then delegate the task further down to the Application.Application.SearchHandler.
     * If the user chooses a top k search, we also ask for the k value.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        //ascii_art();
        //new CommandLine(new main()).execute(args);

    }


    @Override
    public void run() {
        try {
            this.initializeWithInputParams();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void ascii_art() {
        System.out.println("┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐\n" +
                "|  _____  _                     __                |\n" +
                "| /__   \\| |__    ___   ___    / /   ___    __ _  |\n" +
                "|   / /\\/| '_ \\  / _ \\ / _ \\  / /   / _ \\  / _` | |\n" +
                "|  / /   | | | ||  __/| (_) |/ /___| (_) || (_| | |\n" +
                "|  \\/    |_| |_| \\___| \\___/ \\____/ \\___/  \\__, | |\n" +
                "|                                          |___/  |\n" +
                "└ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘");


    }
}
