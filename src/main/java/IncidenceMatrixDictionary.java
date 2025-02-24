import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opennlp.tools.stemmer.PorterStemmer;

public class IncidenceMatrixDictionary {
    public IncidenceMatrixDictionary(){
        matrix = new ArrayList<>();
        fileNames = new ArrayList<>();
        words = new HashMap<>();
        currentFile = null;
        wasModified = false;
    }

    public IncidenceMatrixDictionary(String fileName) throws IOException {
        readFrom(fileName);
    }

    private List<String> fileNames;

    private Map<String, Integer> words;

    private ArrayList<BitSet> matrix;

    private File currentFile;

    private boolean wasModified;

    public void analyzeFile(String fileName) throws IOException {
        if(fileNames.contains(fileName)) return;
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
                    if(!words.containsKey(word)){
                        words.put(word, words.size());
                        matrix.add(new BitSet(fileID));
                    }
                    matrix.get(words.get(word)).set(fileID);
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
        if (fileName == null) {
            throw new IOException("No destination file for saving specified");
        }
        if (!fileName.endsWith(".dict1")) {
            fileName += ".dict1";
        }
        currentFile = new File(fileName);

        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(currentFile))) {
            for (String file : fileNames) {
                writer.write((file + "\t").getBytes());
            }
            writer.write("\n".getBytes());

            for (Map.Entry<String, Integer> entry : words.entrySet()) {
                writer.write((entry.getKey() + "\t" + entry.getValue() + "\t").getBytes());
            }
            writer.write("\n".getBytes());

            for (BitSet bits : matrix) {
                byte[] bitArray = bits.toByteArray();
                writer.write(bitArray.length);
                writer.write(bitArray);
            }

            writer.flush();
        }

        wasModified = false;
    }


    public void readFrom(String fileName) throws IOException {
        if (wasModified) save();
        if (!fileName.endsWith(".dict1")) throw new IOException("Wrong file format");

        currentFile = new File(fileName);

        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(currentFile))) {
            StringBuilder sb = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != '\n' && ch != -1) {
                sb.append((char) ch);
            }
            String[] files = sb.toString().split("\t");
            fileNames = new ArrayList<>(Arrays.asList(files));

            sb.setLength(0);
            while ((ch = reader.read()) != '\n' && ch != -1) {
                sb.append((char) ch);
            }
            words = new HashMap<>();
            matrix = new ArrayList<>();

            String[] wordTokens = sb.toString().split("\t");
            for (int i = 0; i < wordTokens.length - 1; i += 2) {
                String word = wordTokens[i];
                int index = Integer.parseInt(wordTokens[i + 1]);
                words.put(word, index);
                matrix.add(new BitSet(fileNames.size()));
            }

            for (int i = 0; i < matrix.size(); i++) {
                int bitArrayLength = reader.read();
                if (bitArrayLength < 0) {
                    throw new IOException("Corrupted incidence matrix: unexpected end of file.");
                }
                byte[] bitArray = new byte[bitArrayLength];
                if (reader.read(bitArray) != bitArrayLength) {
                    throw new IOException("Corrupted incidence matrix: insufficient data.");
                }
                matrix.get(i).or(BitSet.valueOf(bitArray));
            }
        }
        wasModified = false;
    }


    public List<String> query(String query){
        String[] tokens = query.split(" ");
        LinkedList<BitSet> valStack = new LinkedList<>();
        LinkedList<String> opStack = new LinkedList<>();
        for(String token : tokens){
            switch(token){
                case "AND":
                    while(!opStack.isEmpty()){
                        if(opStack.peek().equals("AND")){
                            opStack.pop();
                            assert valStack.peek() != null;
                            valStack.peek().and(valStack.pop());
                        }
                    }
                    opStack.push("AND");
                    break;
                case "OR":
                    while(!opStack.isEmpty()){
                        if(opStack.peek().equals("AND")){
                            opStack.pop();
                            assert valStack.peek() != null;
                            valStack.peek().and(valStack.pop());
                        }
                        assert opStack.peek() != null;
                        if(opStack.peek().equals("OR")){
                            opStack.pop();
                            assert valStack.peek() != null;
                            valStack.peek().or(valStack.pop());
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
                        BitSet bits = getArr(token);
                        bits.flip(0, bits.size());
                        valStack.push(bits);
                    } else {
                        valStack.push(getArr(token));
                    }
                    break;
            }
        }
        List<String> res = new LinkedList<>();
        assert valStack.peek() != null;
        for(int i = 0; i < fileNames.size(); i++){
            if(valStack.peek().get(i)){
                res.add(fileNames.get(i));
            }
        }
        return res;
    }

    BitSet getArr(String word){
        int index = words.get(normalize(word));
        return matrix.get(index);
    }
}