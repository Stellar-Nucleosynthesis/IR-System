package realizations.query_engines.tolerant_query_engines;

import query_system.QueryResult;
import realizations.query_engines.strict_query_engines.InvertedIndexQueryEngine;
import utils.arbitrary.JokerQueryFilter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static utils.file_parsing_utils.StemmingStringTokenizer.normalize;

public class PrefixIndexQueryEngine extends InvertedIndexQueryEngine {
    public PrefixIndexQueryEngine(List<File> targetFiles) throws IOException {
        super(targetFiles);
    }

    @Override
    public QueryResult findWord(String word) {
        if(!word.contains("*")){
            return super.findWord(word);
        }
        word = normalize(word);
        String prefix = "";
        for(char ch : word.toCharArray()){
            if(ch == '*') break;
            prefix += ch;
        }
        NavigableMap<String, HashSet<Integer>> higherKeys = dictionary.tailMap(prefix, true);
        if(higherKeys == null) return new InvIndQueryResult(null);
        Set<Integer> files = new TreeSet<>();
        for(Map.Entry<String, HashSet<Integer>> entry : higherKeys.entrySet()) {
            if(JokerQueryFilter.matches(entry.getKey(), word)){
                files.addAll(entry.getValue());
            }
        }
        return new InvIndQueryResult(new LinkedList<>(files));
    }
}
