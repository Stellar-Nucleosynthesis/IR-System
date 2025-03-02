package QuerySystem;

import java.io.FileNotFoundException;
import java.io.IOException;

public class QuerySystem{
    public QuerySystem(Dictionary dictionary, QueryParser parser) {
        this.dictionary = dictionary;
        this.parser = parser;
    }

    Dictionary dictionary;

    QueryParser parser;

    String currentFile;

    boolean dictionaryChanged = false;

    public void analyze(String fileName) throws IOException{
        dictionary.analyze(fileName);
        dictionaryChanged = true;
    }

    public void save() throws IOException{
        if(currentFile == null){
            throw new FileNotFoundException("File name is null");
        }
        saveAs(currentFile);
    }

    public void saveAs(String fileName) throws IOException{
        if(fileName == null)
            throw new FileNotFoundException("File name is null");
        if(fileName.equals(currentFile) && !dictionaryChanged)
            return;
        currentFile = fileName;
        dictionary.saveAs(fileName);
        dictionaryChanged = false;
    }

    public void loadFrom(String fileName) throws IOException{
        dictionary.loadFrom(fileName);
    }

    public String[] query(String query){
        QueryResult result = parser.parse(dictionary, query);
        assert result != null;
        return result.value();
    }
}
