package Realizations.Dictionaries.TolerantDictionaries;

import FileParsingUtils.FileParser;
import FileParsingUtils.FileParserBuilder;
import QuerySystem.Dictionary;
import QuerySystem.QueryResult;
import Realizations.Dictionaries.StrictDictionaries.InvertedIndexDictionary;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static FileParsingUtils.StemmingStringTokenizer.normalize;

public class PrefixIndexDictionary extends InvertedIndexDictionary {
    public PrefixIndexDictionary(List<File> targetFiles) throws IOException {
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
