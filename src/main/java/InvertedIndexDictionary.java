import FileParser.*;
import QuerySystem.Dictionary;
import QuerySystem.QueryResult;
import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InvertedIndexDictionary implements Dictionary {
    InvertedIndexDictionary() {
        fileNames = new LinkedList<>();
        dictionary = new HashMap<>();
    }

    protected List<String> fileNames;

    protected Map<String, HashSet<Integer>> dictionary;

    @Override
    public void analyze(String fileName) throws IOException {
        FileParser br = FileParserBuilder.getFileParser(new File(fileName));
        int fileID = fileNames.size();
        fileNames.add(fileName);
        String line = br.readLine();
        while (line != null) {
            for(String word : tokenize(line)) {
                dictionary.putIfAbsent(word, new HashSet<>());
                dictionary.get(word).add(fileID);
            }
            line = br.readLine();
        }
    }

    protected List<String> tokenize(String line) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("[a-zA-Z0-9-'`]*");
        Matcher matcher = pattern.matcher(line);
        while(matcher.find()) {
            String word = matcher.group();
            if(!word.isEmpty()){
                word = normalize(word);
                result.add(word);
            }
        }
        return result;
    }

    protected String normalize(String word){
        PorterStemmer stemmer = new PorterStemmer();
        return stemmer.stem(word.toLowerCase());
    }

    @Override
    public void saveAs(String fileName) throws IOException {
        if(!fileName.endsWith(".iid0"))
            fileName += ".iid0";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        String fileList = "";
        for(String file : fileNames){
            fileList += file + "\t";
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

    @Override
    public void loadFrom(String fileName) throws IOException {
        if(!fileName.endsWith(".iid0")) throw new IOException("Wrong file format");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        dictionary = new HashMap<>();
        fileNames = new ArrayList<>();
        int lineNum = 1;

        String line = reader.readLine();
        String[] files = line.split("\t");
        for(String file : files){
            if(file.isEmpty())
                throw new IOException("Error in " + fileName + ", line " + lineNum + ": Empty file name");
            fileNames.add(file);
        }
        line = reader.readLine();

        while(line != null){
            String[] tokens = line.split("\t");
            String name = tokens[0];
            if(name.isEmpty())
                throw new IOException("Error in " + fileName + ", line " + lineNum + ": Empty token");
            if(dictionary.containsKey(name))
                throw new IOException("Error in " + fileName + ", line " + lineNum + ": Duplicate token");
            dictionary.put(name, new HashSet<>());
            for(int i = 1; i < tokens.length; i++){
                int fileId = Integer.parseInt(tokens[i]);
                if(fileId > fileNames.size() || fileId < 0)
                    throw new IOException("Error in " + fileName + ", line " + lineNum + ": Invalid fileID");
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
        InvIndQueryResult(List<Integer> postings){
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
