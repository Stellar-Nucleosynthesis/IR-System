import FileParser.*;
import QuerySystem.QueryResult;

import java.io.*;
import java.util.*;

public class BWordIndexDictionary extends InvertedIndexDictionary {
    @Override
    public void analyze(String fileName) throws IOException {
        FileParser br = FileParserBuilder.getFileParser(new File(fileName));
        int fileID = fileNames.size();
        fileNames.add(fileName);
        String line = br.readLine();
        String prevWord = null;
        while (line != null) {
            for(String word : tokenize(line)) {
                dictionary.putIfAbsent(word, new HashSet<>());
                dictionary.get(word).add(fileID);
                if(prevWord != null) {
                    dictionary.putIfAbsent(prevWord + " " + word, new HashSet<>());
                    dictionary.get(prevWord + " " + word).add(fileID);
                }
                prevWord = word;
            }
            line = br.readLine();
        }
    }

    @Override
    public QueryResult findPhrase(String phrase) {
        List<String> words = tokenize(phrase);
        if(words.isEmpty()) return new InvIndQueryResult(null);
        if(!dictionary.containsKey(words.getFirst())) return new InvIndQueryResult(null);
        InvIndQueryResult res = new InvIndQueryResult(null);
        for(int fileID : dictionary.get(words.getFirst())) {
            boolean allIn = true;
            for (String word : words) {
                if (!dictionary.containsKey(word) || !dictionary.get(word).contains(fileID)) {
                    allIn = false;
                    break;
                }
            }
            if(!allIn) continue;
            String prevWord = words.getFirst();
            for(int i = 1; i < words.size(); i++) {
                String bWord = prevWord + " " + words.get(i);
                if(!dictionary.containsKey(bWord)) break;
                if(i == words.size() - 1) res.postings.add(fileID);
                prevWord = words.get(i);
            }
        }
        return res;
    }
}