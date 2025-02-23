import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dictionary {
    public Dictionary(){
        dictionary = new HashMap<>();
        fileNames = new ArrayList<>();
        currentFile = null;
        wasModified = false;
    }

    public Dictionary(String fileName) throws IOException {
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
        return word.toLowerCase();
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

    public List<String> getFileNames() {
        return new ArrayList<>(fileNames);
    }

    public int getNumOfWords() {
        return dictionary.size();
    }

    public boolean containsWord(String word) {
        return dictionary.containsKey(word);
    }

    public boolean fileContainsWord(String fileName, String word) {
        int index = fileNames.indexOf(fileName);
        return dictionary.containsKey(word) && dictionary.get(word).contains(index);
    }
}
