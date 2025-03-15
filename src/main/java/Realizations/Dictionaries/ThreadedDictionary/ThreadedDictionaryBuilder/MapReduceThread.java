package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

import FileParsingUtils.FileParser;
import FileParsingUtils.FileParserBuilder;
import FileParsingUtils.StemmingStringTokenizer;

import java.io.*;
import java.util.*;

public class MapReduceThread implements Runnable {
    MapReduceThread(File workingDir, List<File> targetFiles, int threadID){
        this.targetFiles = targetFiles;
        this.termIDs = new HashMap<>();
        this.postingAddresses = new HashMap<>();
        this.fileIDs = new HashMap<>();
        this.outputFile = new File(workingDir, "group" + threadID + "_output.txt");
        this.fileIDsFile = new File(workingDir, "group" + threadID + "fileIDs.txt");
        this.termIDsFile = new File(workingDir, "group" + threadID + "termIDs.txt");
        File cwd = new File(workingDir, "group" + threadID + "_temp_dir");
        if(!cwd.exists()) {
            boolean res = cwd.mkdir();
        }
        this.tempFileDir = cwd;
        buffer = new ArrayList<>();
        tempFiles = new ArrayList<>();
    }

    private final List<File> targetFiles;
    private final File outputFile;

    private final Map<String, Integer> termIDs;
    private final Map<Integer, Integer> postingAddresses;
    private final Map<String, Integer> fileIDs;
    private final File fileIDsFile;
    private final File termIDsFile;

    private final List<Tuple> buffer;
    private static final int BUFFER_SIZE = 0x1000000;

    private final File tempFileDir;
    private final List<File> tempFiles;

    @Override
    public void run() {
        try{
            for(File targetFile : targetFiles)
                scanFile(targetFile);
            if(!buffer.isEmpty())
                writeBufferToFile();
            System.out.println("Buffers written to file");
            constructIndex();
            saveIDs();
            clearUp();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void scanFile(File file) throws IOException {
        FileParser br = FileParserBuilder.getFileParser(file);
        int fileID = fileIDs.size();
        fileIDs.put(file.getAbsolutePath(), fileID);
        String line = br.readLine();
        while (line != null) {
            for(String word : StemmingStringTokenizer.tokenize(line))
                logWord(word, fileID);
            line = br.readLine();
        }
        br.close();
    }

    private void logWord(String word, int fileID) throws IOException {
        termIDs.putIfAbsent(word, termIDs.size());
        int termID = termIDs.get(word);
        buffer.add(new Tuple(termID, fileID));
        if(buffer.size() >= BUFFER_SIZE)
            writeBufferToFile();
    }

    private void writeBufferToFile() throws IOException {
        Collections.sort(buffer);
        File tempFile = new File(tempFileDir, "temp_" + tempFiles.size() + ".bin");
        tempFiles.add(tempFile);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        Tuple prevTuple = null;
        for (Tuple t : buffer) {
            if(t.compareTo(prevTuple) != 0){
                out.writeInt(t.termID);
                out.writeInt(t.fileID);
                prevTuple = t;
            }
        }
        buffer.clear();
        out.close();
    }

    private void constructIndex() throws IOException {
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        TuplePQ pq = new TuplePQ(tempFiles);
        int bytesWritten = 0;
        while(pq.hasNext()){
            int currentTermID = pq.peek().termID;
            List<Integer> posting = new ArrayList<>();
            while(pq.hasNext() && pq.peek().termID == currentTermID){
                int currentFileID = pq.peek().fileID;
                while(pq.hasNext() && pq.peek().fileID == currentFileID && pq.peek().termID == currentTermID){
                    pq.poll();
                }
                posting.add(currentFileID);
            }
            postingAddresses.put(currentTermID, bytesWritten);
            out.writeInt(posting.size());
            for(int num : posting){
                out.writeInt(num);
            }
            bytesWritten += Integer.BYTES * (posting.size() + 1);
        }
        out.close();
    }

    private static class TuplePQ{
        TuplePQ(List<File> files) throws IOException {
            pq = new PriorityQueue<>(Comparator.comparing(o -> o.current));
            for (File file : files) {
                BufferedTupleReader reader = new BufferedTupleReader(file);
                if (reader.hasNext()) {
                    pq.add(reader);
                }
            }
        }

        private final PriorityQueue<BufferedTupleReader> pq;

        public void poll() throws IOException {
            if(!hasNext()) throw new NoSuchElementException();
            BufferedTupleReader reader = pq.poll();
            assert reader != null;
            Tuple tuple = reader.current;
            reader.advance();
            if (reader.hasNext()) {
                pq.add(reader);
            } else {
                reader.close();
            }
        }

        public Tuple peek(){
            if(!hasNext()) throw new NoSuchElementException();
            assert pq.peek() != null;
            return pq.peek().current;
        }

        public boolean hasNext(){
            return !pq.isEmpty();
        }
    }

    private void saveIDs() throws IOException {
        BufferedWriter termIDbw = new BufferedWriter(new FileWriter(termIDsFile));
        for(String word : termIDs.keySet()){
            int termID = termIDs.get(word);
            termIDbw.write(word + '\t' + postingAddresses.get(termID) + '\n');
        }
        termIDbw.close();
        BufferedWriter fileIDbw = new BufferedWriter(new FileWriter(fileIDsFile));
        for(String file : fileIDs.keySet()){
            fileIDbw.write(file + '\t' + fileIDs.get(file) + '\n');
        }
        fileIDbw.close();
    }

    private void clearUp(){
        for(File file : tempFiles){
            file.delete();
        }
        tempFileDir.delete();
    }
}