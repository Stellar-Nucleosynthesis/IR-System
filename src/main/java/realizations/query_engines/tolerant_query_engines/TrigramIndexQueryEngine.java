package realizations.query_engines.tolerant_query_engines;

import query_system.QueryResult;
import realizations.query_engines.strict_query_engines.InvertedIndexQueryEngine;
import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static utils.file_parsing_utils.StemmingStringTokenizer.normalize;
import static utils.file_parsing_utils.StemmingStringTokenizer.tokenize;


public class TrigramIndexQueryEngine extends InvertedIndexQueryEngine {
    public TrigramIndexQueryEngine(List<File> targetFiles) throws IOException {
        super(targetFiles);
    }

    @Override
    protected void analyze(File file) throws IOException {
        FileFormatParser br = FileFormatParserFactory.getFileParser(file);
        int fileID = fileNames.size();
        fileNames.add(file.getAbsolutePath());
        String line = br.readLine();
        while (line != null) {
            for(String word : tokenize(line)) {
                word += "  ";
                String trigram = "   ";
                for(int i = 1; i < word.length(); i++) {
                    trigram = String.valueOf(trigram.charAt(1)) + trigram.charAt(2) + word.charAt(i);
                    dictionary.putIfAbsent(trigram, new HashSet<>());
                    dictionary.get(trigram).add(fileID);
                }
            }
            line = br.readLine();
        }
        br.close();
    }

    private List<String> getTrigrams(String word){
        List<String> trigrams = new ArrayList<>();
        word += "  ";
        String trigram = "   ";
        for(int i = 1; i < word.length(); i++) {
            if(word.charAt(i) == '*'){
                i += 3;
                if(i >= word.length()) break;
                trigram = String.valueOf(word.charAt(i - 2)) + word.charAt(i - 1) + word.charAt(i);
            } else {
                trigram = String.valueOf(trigram.charAt(1)) + trigram.charAt(2) + word.charAt(i);
            }
            trigrams.add(trigram);
        }
        return trigrams;
    }

    @Override
    public QueryResult findWord(String word) {
        word = normalize(word);
        List<String> trigrams = getTrigrams(word);
        List<Set<Integer>> fileIDs = new ArrayList<>();
        for(String trigram : trigrams){
            if(!dictionary.containsKey(trigram)) continue;
            fileIDs.add(dictionary.get(trigram));
        }
        if(fileIDs.isEmpty()) return new InvIndQueryResult(null);
        System.out.println(fileIDs.size());
        Set<Integer> base = fileIDs.removeFirst();
        for(Set<Integer> fileIDList : fileIDs){
            base.retainAll(fileIDList);
        }
        return new InvIndQueryResult(new LinkedList<>(base));
    }
}
