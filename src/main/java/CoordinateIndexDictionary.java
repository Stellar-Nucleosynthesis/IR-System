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

public class CoordinateIndexDictionary implements Dictionary {
    CoordinateIndexDictionary() {
        fileNames = new LinkedList<>();
        dictionary = new HashMap<>();
    }

    private List<String> fileNames;

    private Map<String, Map<Integer, Set<Integer>>> dictionary;

    @Override
    public void analyze(String fileName) throws IOException {
        FileParser br = FileParserBuilder.getFileParser(new File(fileName));
        int fileID = fileNames.size();
        fileNames.add(fileName);
        String line = br.readLine();
        int position = 0;
        while (line != null) {
            for(String word : tokenize(line)) {
                dictionary.putIfAbsent(word, new HashMap<>());
                dictionary.get(word).putIfAbsent(fileID, new HashSet<>());
                dictionary.get(word).get(fileID).add(position++);
            }
            line = br.readLine();
        }
    }

    private List<String> tokenize(String line) {
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

    private String normalize(String word){
        PorterStemmer stemmer = new PorterStemmer();
        return stemmer.stem(word.toLowerCase());
    }

    @Override
    public void saveAs(String fileName) throws IOException {
        if(!fileName.endsWith(".cid0"))
            fileName += ".cid0";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        String fileList = "";
        for(String file : fileNames){
            fileList += file + "\t";
        }
        fileList += '\n';
        writer.write(fileList);
        for(String str : dictionary.keySet()){
            String line = str;
            for(int fileID : dictionary.get(str).keySet()){
                line += "\t" + fileID;
                for(int position : dictionary.get(str).get(fileID))
                    line += " " + position;
            }
            line += '\n';
            writer.write(line);
        }
        writer.flush();
        writer.close();
    }

    @Override
    public void loadFrom(String fileName) throws IOException {
        if(!fileName.endsWith(".cid0")) throw new IOException("Wrong file format");
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
            dictionary.put(name, new HashMap<>());
            for(int i = 1; i < tokens.length; i++){
                String[] positions = tokens[i].split(" ");
                int fileId = Integer.parseInt(positions[0]);
                if(fileId > fileNames.size() || fileId < 0)
                    throw new IOException("Error in " + fileName + ", line " + lineNum + ": Invalid fileID");
                dictionary.get(name).put(fileId, new HashSet<>());

                for(int j = 1; j < positions.length; j++){
                    dictionary.get(name).get(fileId).add(Integer.parseInt(positions[j]));
                }
            }
            lineNum++;
            line = reader.readLine();
        }
        reader.close();
    }

    @Override
    public QueryResult findWord(String word) {
        word = normalize(word);
        if(!dictionary.containsKey(word)) return new CoordIndQueryResult(null);
        Set<Integer> set = dictionary.get(word).keySet();
        LinkedList<Integer> files = new LinkedList<>(set);
        Collections.sort(files);
        return new CoordIndQueryResult(files);
    }

    @Override
    public QueryResult findPhrase(String phrase) {
        List<String> words = tokenize(phrase);
        if(words.isEmpty()) return new CoordIndQueryResult(null);
        if(!dictionary.containsKey(words.getFirst())) return new CoordIndQueryResult(null);
        CoordIndQueryResult res = new CoordIndQueryResult(null);
        for(int fileID : dictionary.get(words.getFirst()).keySet()){
            boolean allIn = true;
            for(String word : words){
                if(!wordIsInFile(word, fileID)){
                    allIn = false;
                    break;
                }
            }
            if(!allIn) continue;
            Set<Integer> baseSet = positionsInFile(words.getFirst(), fileID);
            for(int i = 1; i < words.size(); i++){
                Set<Integer> currentPositions = positionsInFile(words.get(i), fileID);
                HashSet<Integer> newPositions = new HashSet<>();
                for(int n : baseSet){
                    if(currentPositions.contains(n + i)){
                        newPositions.add(n);
                    }
                }
                baseSet = newPositions;
            }
            if(!baseSet.isEmpty()){
                res.postings.add(fileID);
            }
        }
        return res;
    }

    @Override
    public QueryResult findWordsWithin(String word1, String word2, int n) {
        word1 = normalize(word1);
        word2 = normalize(word2);
        if(!dictionary.containsKey(word1) || !dictionary.containsKey(word2))
            return new CoordIndQueryResult(null);
        CoordIndQueryResult res = new CoordIndQueryResult(null);
        Set<Integer> files = dictionary.get(word1).keySet();
        for(int fileID : files){
            TreeSet<Integer> positions1 = positionsInFile(word1, fileID);
            TreeSet<Integer> positions2 = positionsInFile(word2, fileID);
            for(int num : positions1){
                Integer ceiling = positions2.ceiling(num);
                Integer floor = positions2.floor(num);
                if(ceiling != null && ceiling - num <= n){
                    res.postings.add(fileID);
                    break;
                }
                if(floor != null && num - floor <= n){
                    res.postings.add(fileID);
                    break;
                }
            }
        }
        return res;
    }

    private TreeSet<Integer> positionsInFile(String word, int fileID){
        if(!dictionary.containsKey(word)) return new TreeSet<>();
        Set<Integer> fileSet = dictionary.get(word).keySet();
        if(!fileSet.contains(fileID)) return new TreeSet<>();
        return new TreeSet<>(dictionary.get(word).get(fileID));
    }

    private boolean wordIsInFile(String word, int fileID){
        if(!dictionary.containsKey(word)) return false;
        return dictionary.get(word).containsKey(fileID);
    }

    private class CoordIndQueryResult implements QueryResult {
        CoordIndQueryResult(List<Integer> postings){
            this.postings = Objects.requireNonNullElseGet(postings, LinkedList::new);
        }

        List<Integer> postings;

        @Override
        public void and(QueryResult other) {
            checkParam(other);
            postings = postings.stream()
                    .filter(((CoordinateIndexDictionary.CoordIndQueryResult)other).postings::contains)
                    .collect(Collectors.toList());
        }

        @Override
        public void or(QueryResult other) {
            checkParam(other);
            postings = Stream.concat(postings.stream(), ((CoordinateIndexDictionary.CoordIndQueryResult)other).postings.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }

        private void checkParam(QueryResult other) {
            if(!(other instanceof CoordinateIndexDictionary.CoordIndQueryResult))
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
