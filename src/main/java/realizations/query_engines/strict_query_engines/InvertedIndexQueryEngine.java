package realizations.query_engines.strict_query_engines;

import query_system.QueryEngine;
import query_system.QueryResult;
import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static utils.file_parsing_utils.StemmingStringTokenizer.normalize;
import static utils.file_parsing_utils.StemmingStringTokenizer.tokenize;

public class InvertedIndexQueryEngine implements QueryEngine {
    public InvertedIndexQueryEngine(List<File> targetFiles) throws IOException {
        fileNames = new LinkedList<>();
        dictionary = new TreeMap<>();
        for (File targetFile : targetFiles) {
            analyze(targetFile);
        }
    }

    protected List<String> fileNames;

    protected TreeMap<String, HashSet<Integer>> dictionary;

    protected void analyze(File file) throws IOException {
        FileFormatParser br = FileFormatParserFactory.getFileParser(file);
        int fileID = fileNames.size();
        fileNames.add(file.getAbsolutePath());
        String line = br.readLine();
        while (line != null) {
            for(String word : tokenize(line)) {
                dictionary.putIfAbsent(word, new HashSet<>());
                dictionary.get(word).add(fileID);
            }
            line = br.readLine();
        }
        br.close();
    }

    public void saveAs(File file) throws IOException {
        if(!file.getAbsolutePath().endsWith(".iid0"))
            file = new File(file.getAbsolutePath() + ".iid0");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        String fileList = "";
        for(String fileName : fileNames){
            fileList += fileName + "\t";
        }
        fileList += '\n';
        writer.write(fileList);
        for(String str : dictionary.keySet()){
            String line = str;
            for(int fileID : dictionary.get(str))
                line += "\t" + fileID;
            line += '\n';
            writer.write(line);
        }
        writer.flush();
        writer.close();
    }

    public void loadFrom(File file) throws IOException {
        if(!file.getAbsolutePath().endsWith(".iid0")) throw new IOException("Wrong file format");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        dictionary = new TreeMap<>();
        fileNames = new ArrayList<>();
        int lineNum = 1;

        String line = reader.readLine();
        String[] files = line.split("\t");
        for(String fileName : files){
            if(fileName.isEmpty())
                throw new IOException("Error in " + fileName + ", line " + lineNum + ": Empty file name");
            fileNames.add(fileName);
        }
        line = reader.readLine();

        while(line != null){
            String[] tokens = line.split("\t");
            String name = tokens[0];
            if(name.isEmpty())
                throw new IOException("Error in " + file.getAbsolutePath() + ", line " + lineNum + ": Empty token");
            if(dictionary.containsKey(name))
                throw new IOException("Error in " + file.getAbsolutePath() + ", line " + lineNum + ": Duplicate token");
            dictionary.put(name, new HashSet<>());
            for(int i = 1; i < tokens.length; i++){
                int fileId = Integer.parseInt(tokens[i]);
                if(fileId > fileNames.size() || fileId < 0)
                    throw new IOException("Error in " + file.getAbsolutePath() + ", line " + lineNum + ": Invalid fileID");
                dictionary.get(name).add(fileId);
            }
            lineNum++;
            line = reader.readLine();
        }
        reader.close();
    }

    @Override
    public QueryResult findWord(String word) {
        Set<Integer> set = dictionary.get(normalize(word));
        if(set == null) return new InvIndQueryResult(null);
        LinkedList<Integer> files = new LinkedList<>(set);
        Collections.sort(files);
        return new InvIndQueryResult(files);
    }

    @Override
    public QueryResult findPhrase(String phrase) {
        return null;
    }

    @Override
    public QueryResult findWordsWithin(String word1, String word2, int n) {
        return null;
    }

    protected class InvIndQueryResult implements QueryResult {
        public InvIndQueryResult(List<Integer> postings){
            this.postings = Objects.requireNonNullElseGet(postings, LinkedList::new);
        }

        List<Integer> postings;

        @Override
        public void and(QueryResult other) {
            checkParam(other);
            postings = postings.stream()
                    .filter(((InvIndQueryResult)other).postings::contains)
                    .collect(Collectors.toList());
        }

        @Override
        public void or(QueryResult other) {
            checkParam(other);
            postings = Stream.concat(postings.stream(), ((InvIndQueryResult)other).postings.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }

        private void checkParam(QueryResult other) {
            if(!(other instanceof InvIndQueryResult))
                throw new IllegalCallerException("The parameter must be of the same class!");
        }

        @Override
        public void not() {
            postings = IntStream.rangeClosed(0, fileNames.size() - 1)
                    .filter(i -> !postings.contains(i))
                    .boxed()
                    .collect(Collectors.toList());
        }

        @Override
        public String[] value() {
            String[] result = new String[postings.size()];
            int index = 0;
            for(int i : postings)
                result[index++] = fileNames.get(i);
            return result;
        }
    }
}
