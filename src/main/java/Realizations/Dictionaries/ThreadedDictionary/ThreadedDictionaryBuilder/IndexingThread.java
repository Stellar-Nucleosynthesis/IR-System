package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

import FileParsingUtils.FileParser;
import FileParsingUtils.FileParserBuilder;
import FileParsingUtils.StemmingStringTokenizer;

import java.io.*;
import java.util.*;

import static EncodingUtils.VaribleByteEncoding.writeCodedInt;

public class IndexingThread implements Runnable {
    IndexingThread(File workingDir, List<File> targetFiles, int threadID){
        this.targetFiles = targetFiles;
        this.postingAddresses = new HashMap<>();
        this.fileIDs = new HashMap<>();
        this.outputFile = new File(workingDir, "group" + threadID + "_output.txt");
        this.fileIDsFile = new File(workingDir, "group" + threadID + "fileIDs.txt");
        this.postingAddrFile = new File(workingDir, "group" + threadID + "postingAddr.txt");
        File cwd = new File(workingDir, "group" + threadID + "_temp_dir");
        if(!cwd.exists()) {
            boolean res = cwd.mkdir();
        }
        this.tempFileDir = cwd;
        buffer = new HashMap<>();
        tempFiles = new ArrayList<>();
    }

    private final List<File> targetFiles;
    private final File outputFile;

    private final Map<String, Integer> postingAddresses;
    private final Map<String, Integer> fileIDs;
    private final File fileIDsFile;
    private final File postingAddrFile;

    private final HashMap<String, List<Integer>> buffer;
    private int logsInMemory = 0;
    private static final int MAX_SIZE = 100_000;

    private final File tempFileDir;
    private final List<File> tempFiles;

    @Override
    public void run() {
        try{
            for(File targetFile : targetFiles)
                scanFile(targetFile);
            if(!buffer.isEmpty())
                writeBufferToFile();
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
        buffer.putIfAbsent(word, new ArrayList<>());
        List<Integer> posting = buffer.get(word);
        if(!posting.isEmpty() && posting.getLast() == fileID) return;
        posting.add(fileID);
        if(++logsInMemory >= MAX_SIZE)
            writeBufferToFile();
    }

    private void writeBufferToFile() throws IOException {
        List<String> terms = new ArrayList<>(buffer.keySet());
        Collections.sort(terms);
        File tempFile = new File(tempFileDir, "temp_" + tempFiles.size() + ".bin");
        tempFiles.add(tempFile);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        for (String term : terms) {
            writeCodedInt(out, term.length());
            out.writeChars(term);
            writeCodedInt(out, buffer.get(term).size());
            for(int fileID : buffer.get(term)){
                writeCodedInt(out, fileID);
            }
        }
        buffer.clear();
        logsInMemory = 0;
        out.close();
    }

    private void constructIndex() throws IOException {
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        RecordPQ pq = new RecordPQ(tempFiles);
        int bytesWritten = 0;
        while(pq.hasNext()){
            String currentTerm = pq.peekTerm();
            List<Integer> posting = pq.peekPosting();
            pq.poll();
            while(pq.hasNext() && pq.peekTerm().equals(currentTerm)){
                posting.addAll(pq.peekPosting());
                pq.poll();
            }
            postingAddresses.put(currentTerm, bytesWritten);
            bytesWritten += writeCodedInt(out, posting.size());
            for(int num : posting){
                bytesWritten += writeCodedInt(out, num);
            }
        }
        out.close();
    }

    private static class RecordPQ{
        RecordPQ(List<File> files) throws IOException {
            pq = new PriorityQueue<>(Comparator.comparing(o -> o.term));
            for (File file : files) {
                BufferedRecordReader reader = new BufferedRecordReader(file);
                if (reader.hasNext()) {
                    pq.add(reader);
                }
            }
        }

        private final PriorityQueue<BufferedRecordReader> pq;

        public void poll() throws IOException {
            if(!hasNext()) throw new NoSuchElementException();
            BufferedRecordReader reader = pq.poll();
            assert reader != null;
            reader.advance();
            if (reader.hasNext()) {
                pq.add(reader);
            } else {
                reader.close();
            }
        }

        public String peekTerm(){
            if(!hasNext()) throw new NoSuchElementException();
            assert pq.peek() != null;
            return pq.peek().term;
        }

        public List<Integer> peekPosting(){
            if(!hasNext()) throw new NoSuchElementException();
            assert pq.peek() != null;
            return pq.peek().posting;
        }

        public boolean hasNext(){
            return !pq.isEmpty();
        }
    }

    private void saveIDs() throws IOException {
        BufferedWriter postingAddrbw = new BufferedWriter(new FileWriter(postingAddrFile));
        for(String term : postingAddresses.keySet()){
            int postingAddr = postingAddresses.get(term);
            postingAddrbw.write(term + '\t' + postingAddr + '\n');
        }
        postingAddrbw.close();
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