package realizations.query_engines.threaded_query_engine.threaded_query_engine_reader;
import utils.encoding_utils.BlockedCompressedDictionary;
import utils.postings.GlobalPosting;
import utils.postings.LocalPosting;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

public class ReadingThread implements Runnable {
    ReadingThread(int threadID, File cwd, ThreadedQueryEngineReader.ThreadedDictQueryResult result, Semaphore finishReport) {
        this.threadID = threadID;
        this.result = result;
        this.finishReport = finishReport;
        this.indexFile = new File(cwd, "group" + threadID + "_output.txt");
        this.fileIDsFile = new File(cwd, "group" + threadID + "fileIDs.txt");
        this.postingAddrFile = new File(cwd, "group" + threadID + "postingAddr.txt");
    }

    private final int threadID;

    private volatile String currentWord;
    private final Semaphore currentWordMutex = new Semaphore(1);

    private volatile boolean timeToExit = false;
    private final Semaphore timeToExitMutex = new Semaphore(1);

    private final ThreadedQueryEngineReader.ThreadedDictQueryResult result;

    private final Semaphore waitForTask = new Semaphore(0);
    private final Semaphore finishReport;

    private final File indexFile;
    private final File fileIDsFile;
    private final File postingAddrFile;
    private final ConcurrentMap<Integer, String> fileByID = new ConcurrentHashMap<>();
    private BlockedCompressedDictionary postingAddresses;

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

    public Set<GlobalPosting> getAllFileIDs(){
        Set<GlobalPosting> result = new HashSet<>();
        for(int fileID : fileByID.keySet()){
            result.add(new GlobalPosting(threadID, new LocalPosting(fileID)));
        }
        return result;
    }

    private void initializeDataStructures() throws IOException {
        this.postingAddresses = new BlockedCompressedDictionary(postingAddrFile);
        BufferedReader fileIDbr = new BufferedReader(new FileReader(fileIDsFile));
        String line = fileIDbr.readLine();
        while (line != null) {
            String[] tokens = line.split("\t");
            fileByID.put(Integer.parseInt(tokens[1]), tokens[0]);
            line = fileIDbr.readLine();
        }
       fileIDbr.close();
    }

    private List<GlobalPosting> findEntries(String currentWord) throws IOException {
        if(!postingAddresses.containsTerm(currentWord)){
            return new ArrayList<>();
        }
        int postingAddress = postingAddresses.getPostingAddr(currentWord);
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
        in.skipBytes(postingAddress);
        List<LocalPosting> result = LocalPosting.readPostingsList(in);
        List<GlobalPosting> res = new ArrayList<>();
        for(LocalPosting posting : result){
            res.add(posting.toGlobalPosting(threadID));
        }
        Collections.sort(res);
        return res;
    }
}
