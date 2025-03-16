package Realizations.Dictionaries.TolerantDictionaries;

import FileParsingUtils.FileParser;
import FileParsingUtils.FileParserBuilder;
import QuerySystem.QueryResult;
import Realizations.Dictionaries.StrictDictionaries.InvertedIndexDictionary;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static FileParsingUtils.StemmingStringTokenizer.normalize;
import static FileParsingUtils.StemmingStringTokenizer.tokenize;

public class PermutedIndexDictionary extends InvertedIndexDictionary {
    public PermutedIndexDictionary(List<File> targetFiles) throws IOException {
        super(targetFiles);
    }

    @Override
    protected void analyze(File file) throws IOException {
        FileParser br = FileParserBuilder.getFileParser(file);
        int fileID = fileNames.size();
        fileNames.add(file.getAbsolutePath());
        String line = br.readLine();
        while (line != null) {
            for(String word : tokenize(line)) {
                word += " ";
                for(int i = 0; i < word.length(); i++) {
                    dictionary.putIfAbsent(shiftString(word, i), new HashSet<>());
                    dictionary.get(shiftString(word, i)).add(fileID);
                }
            }
            line = br.readLine();
        }
        br.close();
    }

    public static String shiftString(String word, int n) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        int len = word.length();
        n = ((n % len) + len) % len;
        return word.substring(len - n) + word.substring(0, len - n);
    }

    @Override
    public QueryResult findWord(String word) {
        word = normalize(word);
        word += ' ';
        if(!word.contains("*")){
            return super.findWord(word);
        }
        while(word.charAt(word.length()-1) != '*') {
            word = shiftString(word, 1);
        }
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
