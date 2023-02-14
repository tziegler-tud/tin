package DataProvider;

import Database.DatabaseGraph;
import Query.QueryGraph;
import Transducer.TransducerGraph;

import java.util.HashSet;
import java.util.Set;

// the dataProvider holds the three data graphs.

public class DataProvider {

    public QueryGraph queryGraph = new QueryGraph();
    public TransducerGraph transducerGraph = new TransducerGraph();
    public DatabaseGraph databaseGraph = new DatabaseGraph();
    public Set<String> alphabet;

    public String getDataSetIdentifier() {
        return dataSetIdentifier;
    }

    public void setDataSetIdentifier(String dataSetIdentifier) {
        this.dataSetIdentifier = dataSetIdentifier;
    }

    public String dataSetIdentifier;


    public DataProvider() {
        QueryGraph queryGraph = new QueryGraph();
        TransducerGraph transducerGraph = new TransducerGraph();
        DatabaseGraph databaseGraph = new DatabaseGraph();
        alphabet = new HashSet<>();
    }

    public QueryGraph getQueryGraph() {
        return queryGraph;
    }

    public TransducerGraph getTransducerGraph() {
        return transducerGraph;
    }

    public DatabaseGraph getDatabaseGraph() {
        return databaseGraph;
    }


}


