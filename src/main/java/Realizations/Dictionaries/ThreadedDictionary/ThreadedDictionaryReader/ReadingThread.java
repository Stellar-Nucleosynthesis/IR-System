package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import static EncodingUtils.VariableByteEncoding.readCodedInt;

public class ReadingThread implements Runnable {
    ReadingThread(int threadID, File cwd, ThreadedDictionaryReader.ThreadedDictQueryResult result, Semaphore finishReport) {
        this.threadID = threadID;
        this.fileByID = new ConcurrentHashMap<>();
        this.result = result;
        this.finishReport = finishReport;
        timeToExitMutex = new Semaphore(1);
        waitForTask = new Semaphore(0);
        currentWordMutex = new Semaphore(1);
        this.indexFile = new File(cwd, "group" + threadID + "_output.txt");
        this.fileIDsFile = new File(cwd, "group" + threadID + "fileIDs.txt");
        this.postingAddrFile = new File(cwd, "group" + threadID + "postingAddr.txt");
        timeToExit = false;
    }

    private final int threadID;

    private volatile String currentWord;
    private final Semaphore currentWordMutex;

    private volatile boolean timeToExit;
    private final Semaphore timeToExitMutex;

    private final ThreadedDictionaryReader.ThreadedDictQueryResult result;

    private final Semaphore waitForTask;
    private final Semaphore finishReport;

    private final File indexFile;
    private final File fileIDsFile;
    private final File postingAddrFile;
    private final ConcurrentMap<Integer, String> fileByID;
    private CompressedTermToPosting postingAddresses;

    @Override
    public void run() {
        try {
            initializeDataStructures();
            while (true) {
                waitForTask.acquire();

                timeToExitMutex.acquire();
                if(timeToExit) {
                    timeToExitMutex.release();
                    break;
                }
                timeToExitMutex.release();

                currentWordMutex.acquire();
                String word = currentWord;
                currentWordMutex.release();

                result.setValue(findEntries(word));

                finishReport.release();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void unlockThread(){
        this.waitForTask.release();
    }

    public void signalEnd() throws InterruptedException {
        timeToExitMutex.acquire();
        timeToExit = true;
        timeToExitMutex.release();
    }

    public void setCurrentWord(String word) throws InterruptedException {
        currentWordMutex.acquire();
        this.currentWord = word;
        currentWordMutex.release();
    }

    public String getFileNameById(int fileID){
        return fileByID.get(fileID);
    }

    public Set<Long> getAllFileIDs(){
        Set<Long> result = new HashSet<>();
        for(int fileID : fileByID.keySet()){
            result.add(toGlobalID(fileID));
        }
        return result;
    }

    private void initializeDataStructures() throws IOException {
        this.postingAddresses = new CompressedTermToPosting(postingAddrFile);
        BufferedReader fileIDbr = new BufferedReader(new FileReader(fileIDsFile));
        String line = fileIDbr.readLine();
        while (line != null) {
            String[] tokens = line.split("\t");
            fileByID.put(Integer.parseInt(tokens[1]), tokens[0]);
            line = fileIDbr.readLine();
        }
       fileIDbr.close();
    }

    private List<Long> findEntries(String currentWord) throws IOException {
        if(!postingAddresses.containsTerm(currentWord)){
            return new ArrayList<>();
        }
        int postingAddress = postingAddresses.getPostingAddr(currentWord);
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
        in.skipBytes(postingAddress);
        int postingSize = readCodedInt(in);
        List<Long> result = new ArrayList<>(postingSize);
        int prevNum = 0;
        for (int i = 0; i < postingSize; i++) {
            int num = prevNum + readCodedInt(in);
            prevNum = num;
            result.add(toGlobalID(num));
        }
        return result;
    }

    private long toGlobalID(int ID){
        return (long) threadID << 32 | ID;
    }
}
