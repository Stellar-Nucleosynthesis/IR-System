import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvertedIndexDictionary {
    public InvertedIndexDictionary(){
        dictionary = new HashMap<>();
        fileNames = new ArrayList<>();
        currentFile = null;
        wasModified = false;
    }

    public InvertedIndexDictionary(String fileName) throws IOException {
        readFrom(fileName);
    }

    private List<String> fileNames;

    private Map<String, HashSet<Integer>> dictionary;

    private File currentFile;

    private boolean wasModified;

    public void analyzeFile(String fileName) throws IOException {
        FileParser br = FileParserBuilder.getFileParser(new File(fileName));
        int fileID = fileNames.size();
        fileNames.add(fileName);
        String line = br.readLine();
        while (line != null) {
            Pattern pattern = Pattern.compile("[a-zA-Z0-9-'`]*");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()) {
                String word = matcher.group();
                if(!word.isEmpty()){
                    word = normalize(word);
                    dictionary.putIfAbsent(word, new HashSet<>());
                    dictionary.get(word).add(fileID);
                    wasModified = true;
                }
            }
            line = br.readLine();
        }
    }

    private String normalize(String word){
        PorterStemmer stemmer = new PorterStemmer();
        return stemmer.stem(word.toLowerCase());
    }

    public void save() throws IOException {
        if(currentFile == null)
            throw new IOException("No destination file for saving specified");
        if(!wasModified) return;
        saveAs(currentFile.getAbsolutePath());
    }

    public void saveAs(String fileName) throws IOException {
        if(fileName == null){
            throw new IOException("No destination file for saving specified");
        } else {
            if(!fileName.endsWith(".dict0"))
                fileName += ".dict0";
            currentFile = new File(fileName);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
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
        wasModified = false;
        writer.close();
    }

    public void readFrom(String fileName) throws IOException {
        if(dictionary != null && !dictionary.isEmpty() && wasModified) save();
        if(!fileName.endsWith(".dict0")) throw new IOException("Wrong file format");
        currentFile = new File(fileName);
        BufferedReader reader = new BufferedReader(new FileReader(currentFile));
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
        wasModified = false;
        reader.close();
    }

    public List<String> query(String query){
        String[] tokens = query.split(" ");
        LinkedList<List<Integer>> valStack = new LinkedList<>();
        LinkedList<String> opStack = new LinkedList<>();
        for(String token : tokens){
            switch(token){
                case "AND":
                    while(!opStack.isEmpty()){
                        if(opStack.peek().equals("AND")){
                            opStack.pop();
                            valStack.push(and(valStack.pop(), valStack.pop()));
                        }
                    }
                    opStack.push("AND");
                    break;
                case "OR":
                    while(!opStack.isEmpty()){
                        if(opStack.peek().equals("AND")){
                            opStack.pop();
                            valStack.push(and(valStack.pop(), valStack.pop()));
                        }
                        assert opStack.peek() != null;
                        if(opStack.peek().equals("OR")){
                            opStack.pop();
                            valStack.push(or(valStack.pop(), valStack.pop()));
                        }
                    }
                    opStack.push("OR");
                    break;
                case "NOT":
                    opStack.push("NOT");
                    break;
                default:
                    if(opStack.peek() != null && opStack.peek().equals("NOT")){
                        opStack.pop();
                        valStack.push(not(getPresences(token)));
                    } else {
                        valStack.push(getPresences(token));
                    }
                    break;
            }
        }
        List<String> res = new LinkedList<>();
        assert valStack.peek() != null;
        for(int id : valStack.peek()){
            res.add(fileNames.get(id));
        }
        return res;
    }

    private List<Integer> and(List<Integer> a, List<Integer> b){
        List<Integer> result = new ArrayList<>();
        int i1 = 0, i2 = 0;
        while(i1 < a.size() && i2 < b.size()){
            int val1 = a.get(i1), val2 = b.get(i2);
            if(val1 > val2){
                i2++;
            } else if(val1 < val2){
                i1++;
            } else {
                result.add(val1);
                i2++;
                i1++;
            }
        }
        return result;
    }

    private List<Integer> or(List<Integer> a, List<Integer> b){
        List<Integer> result = new ArrayList<>();
        int i1 = 0, i2 = 0;
        while(i1 < a.size() && i2 < b.size()){
            int val1 = a.get(i1), val2 = b.get(i2);
            if(val1 > val2){
                i2++;
                result.add(val2);
            } else if(val1 < val2){
                i1++;
                result.add(val1);
            } else {
                result.add(val1);
                i2++;
                i1++;
            }
        }
        return result;
    }

    private List<Integer> not(List<Integer> list){
        if (list == null || list.isEmpty()) return Collections.emptyList();
        HashSet<Integer> set = new HashSet<>(list);
        List<Integer> result = new LinkedList<>();
        for(int i = 0; i < fileNames.size(); i++){
            if(set.contains(i)) continue;
            result.add(i);
        }
        return result;
    }

    private List<Integer> getPresences(String term){
        Set<Integer> set = dictionary.get(normalize(term));
        if(set == null) return Collections.emptyList();
        LinkedList<Integer> files = new LinkedList<>(set);
        Collections.sort(files);
        return files;
    }
}
