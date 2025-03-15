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

    private File currentFile;

    boolean dictionaryChanged = false;

    public void save() throws IOException{
        if(currentFile == null){
            throw new FileNotFoundException("File name is null");
        }
        saveAs(currentFile);
    }

    public void saveAs(File file) throws IOException{
        if(file == null)
            throw new FileNotFoundException("File name is null");
        if(file.equals(currentFile) && !dictionaryChanged)
            return;
        currentFile = file;
        dictionary.saveAs(file);
        dictionaryChanged = false;
    }

    public void loadFrom(File file) throws IOException{
        dictionary.loadFrom(file);
    }

    public String[] query(String query){
        QueryResult result = parser.parse(dictionary, query);
        assert result != null;
        return result.value();
    }
}
