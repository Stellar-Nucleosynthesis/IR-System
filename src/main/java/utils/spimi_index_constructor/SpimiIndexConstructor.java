package utils.spimi_index_constructor;

import utils.encoding_utils.BlockedDictionaryCompressor;
import postings.Posting;
import postings.PostingFactory;
import postings.PostingsList;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class SpimiIndexConstructor<T extends Posting<T>> {
    public SpimiIndexConstructor(PostingFactory<T> factory){
        this.factory = factory;
    }

    private final PostingFactory<T> factory;
    private final HashMap<String, PostingsList<T>> buffer = new HashMap<>();
    private int logsInMemory = 0;
    private static final int MAX_SIZE = 100_000;
    private final List<File> tempFiles = new ArrayList<>();

    public void addPosting(String term, T posting) throws IOException {
        buffer.putIfAbsent(term, new PostingsList<>());
        PostingsList<T> postings = buffer.get(term);
        postings.addPosting(posting);
        if(++logsInMemory >= MAX_SIZE){
            writeBufferToFile();
        }
    }

    private void writeBufferToFile() throws IOException {
        List<String> terms = new ArrayList<>(buffer.keySet());
        Collections.sort(terms);
        File tempFile = Files.createTempFile("temp_" + UUID.randomUUID(), ".txt").toFile();
        tempFiles.add(tempFile);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        for (String term : terms) {
            writeCodedInt(out, term.length());
            out.writeChars(term);
            PostingsList<T> postings = buffer.get(term);
            postings.writePostingsList(out);
        }
        buffer.clear();
        logsInMemory = 0;
        out.close();
    }

    public void constructIndex(File outputDirectory) throws IOException {
        if(!buffer.isEmpty())
            writeBufferToFile();
        Map<String, Integer> postingAddresses = new HashMap<>();
        File outputFile = new File(outputDirectory, "output.txt");
        File postingAddrFile = new File(outputDirectory, "postingAddr.txt");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        BufferedRecordPQ<T> pq = new BufferedRecordPQ<>(tempFiles, factory);
        int bytesWritten = 0;
        while(pq.hasNext()){
            String currentTerm = pq.peekTerm();
            PostingsList<T> postings = pq.peekPosting();
            pq.poll();
            while(pq.hasNext() && pq.peekTerm().equals(currentTerm)){
                postings.merge(pq.peekPosting());
                pq.poll();
            }
            postingAddresses.put(currentTerm, bytesWritten);
            bytesWritten += postings.writePostingsList(out);
        }
        out.close();
        savePostingAddr(postingAddrFile, postingAddresses);
    }

    private void savePostingAddr(File postingAddrFile, Map<String, Integer> postingAddresses) throws IOException {
        BlockedDictionaryCompressor.writeCompressedMapping(postingAddrFile, postingAddresses);
    }
}
