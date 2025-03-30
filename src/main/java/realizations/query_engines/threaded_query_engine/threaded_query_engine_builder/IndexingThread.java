package realizations.query_engines.threaded_query_engine.threaded_query_engine_builder;

import utils.file_parsing_utils.FileFormatParser;
import utils.file_parsing_utils.FileFormatParserFactory;
import utils.file_parsing_utils.StemmingStringTokenizer;
import utils.file_parsing_utils.Zone;
import utils.postings.LocalPosting;

import java.io.*;
import java.util.*;

import static utils.encoding_utils.BlockedDictionaryCompressor.writeCompressedMapping;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class IndexingThread implements Runnable {
    IndexingThread(File workingDir, List<File> targetFiles, int threadID){
        this.targetFiles = targetFiles;
        this.outputFile = new File(workingDir, "group" + threadID + "_output.txt");
        this.fileIDsFile = new File(workingDir, "group" + threadID + "fileIDs.txt");
        this.postingAddrFile = new File(workingDir, "group" + threadID + "postingAddr.txt");
        File cwd = new File(workingDir, "group" + threadID + "_temp_dir");
        if(!cwd.exists()) {
            boolean res = cwd.mkdir();
        }
        this.tempFileDir = cwd;
    }

    private final List<File> targetFiles;
    private final File outputFile;

    private final Map<String, Integer> postingAddresses = new HashMap<>();
    private final Map<String, Integer> fileIDs = new HashMap<>();
    private final File fileIDsFile;
    private final File postingAddrFile;

    private final HashMap<String, List<LocalPosting>> buffer = new HashMap<>();
    private int logsInMemory = 0;
    private static final int MAX_SIZE = 100_000;

    private final File tempFileDir;
    private final List<File> tempFiles = new ArrayList<>();

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
        FileFormatParser br = FileFormatParserFactory.getFileParser(file);
        int fileID = fileIDs.size();
        fileIDs.put(file.getAbsolutePath(), fileID);
        String line = br.readLine();
        while (line != null) {
            for(String word : StemmingStringTokenizer.tokenize(line))
                logWord(word, fileID, br.getCurrentZone());
            line = br.readLine();
        }
        br.close();
    }

    private void logWord(String word, int fileID, Zone zone) throws IOException {
        buffer.putIfAbsent(word, new ArrayList<>());
        List<LocalPosting> postings = buffer.get(word);
        LocalPosting current = new LocalPosting(fileID, zone);
        if(!postings.isEmpty() && postings.getLast().getFileID() == fileID) {
            postings.getLast().merge(current);
        } else {
            postings.add(current);
            if(++logsInMemory >= MAX_SIZE)
                writeBufferToFile();
        }
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
            LocalPosting.writePostingsList(out, buffer.get(term));
        }
        buffer.clear();
        logsInMemory = 0;
        out.close();
    }

    private void constructIndex() throws IOException {
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        BufferedRecordPQ pq = new BufferedRecordPQ(tempFiles);
        int bytesWritten = 0;
        while(pq.hasNext()){
            String currentTerm = pq.peekTerm();
            List<LocalPosting> postings = pq.peekPosting();
            pq.poll();
            while(pq.hasNext() && pq.peekTerm().equals(currentTerm)){
                postings.addAll(pq.peekPosting());
                pq.poll();
            }
            postingAddresses.put(currentTerm, bytesWritten);
            Collections.sort(postings);
            removeDuplicates(postings);
            bytesWritten += LocalPosting.writePostingsList(out, postings);
        }
        out.close();
    }

    private void removeDuplicates(List<LocalPosting> postings) {
        if (postings.isEmpty()) return;
        int uniqueIndex = 0;
        for (int i = 1; i < postings.size(); i++) {
            if (!postings.get(i).equals(postings.get(uniqueIndex))) {
                uniqueIndex++;
                postings.set(uniqueIndex, postings.get(i));
            }
        }
        while (postings.size() > uniqueIndex + 1) {
            postings.removeLast();
        }
    }

    private void saveIDs() throws IOException {
        writeCompressedMapping(postingAddrFile, postingAddresses);
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