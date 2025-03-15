package QuerySystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class QuerySystem{
    public QuerySystem(Dictionary dictionary, QueryParser parser) {
        this.dictionary = dictionary;
        this.parser = parser;
    }

    private final Dictionary dictionary;

    private final QueryParser parser;

    public String[] query(String query){
        QueryResult result = parser.parse(dictionary, query);
        assert result != null;
        return result.value();
    }
}
