package query_engines.simple_query_engines;

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

public class InvertedIndexQueryEngine implements QueryEngine<InvertedIndexQueryEngine.InvIndQueryResult> {
    public InvertedIndexQueryEngine(List<File> targetFiles) throws IOException {
        for (File targetFile : targetFiles) {
            analyze(targetFile);
        }
    }

    protected List<String> fileNames = new ArrayList<>();

    protected TreeMap<String, HashSet<Integer>> dictionary = new TreeMap<>();

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
        StringBuilder fileList = new StringBuilder();
        for(String fileName : fileNames){
            fileList.append(fileName).append("\t");
        }
        fileList.append('\n');
        writer.write(fileList.toString());
        for(String str : dictionary.keySet()){
            StringBuilder line = new StringBuilder(str);
            for(int fileID : dictionary.get(str))
                line.append("\t").append(fileID);
            line.append('\n');
            writer.write(line.toString());
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
    public InvIndQueryResult findWord(String word) {
        Set<Integer> set = dictionary.get(normalize(word));
        if(set == null) return new InvIndQueryResult(null);
        LinkedList<Integer> files = new LinkedList<>(set);
        Collections.sort(files);
        return new InvIndQueryResult(files);
    }

    @Override
    public InvIndQueryResult findPhrase(String phrase) {
        return null;
    }

    @Override
    public InvIndQueryResult findWordsWithin(String word1, String word2, int n) {
        return null;
    }

    public class InvIndQueryResult implements QueryResult<InvIndQueryResult> {
        public InvIndQueryResult(List<Integer> postings){
            this.postings = Objects.requireNonNullElseGet(postings, LinkedList::new);
        }

        List<Integer> postings;

        @Override
        public void and(InvIndQueryResult other) {
            postings = postings.stream()
                    .filter(other.postings::contains)
                    .collect(Collectors.toList());
        }

        @Override
        public void or(InvIndQueryResult other) {
            postings = Stream.concat(postings.stream(), other.postings.stream())
                    .distinct()
                    .collect(Collectors.toList());
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
