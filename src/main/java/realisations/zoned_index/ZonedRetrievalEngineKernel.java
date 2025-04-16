package realisations.zoned_index;

import kernels.RetrievalEngineKernel;
import utils.encoding_utils.BlockedCompressedDictionary;
import postings.PostingsList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static utils.file_parsing_utils.StemmingStringTokenizer.tokenize;

public class ZonedRetrievalEngineKernel implements RetrievalEngineKernel<ZonedRetrievalResult, ZonedPosting> {
    ZonedRetrievalEngineKernel(File workingDir, int threadId) throws IOException {
        this.indexFile = new File(workingDir, "index.txt");
        File postingAddrFile = new File(workingDir, "postingAddr.txt");
        this.postingAddr = new BlockedCompressedDictionary(postingAddrFile);
        File fileNamesFile = new File(workingDir, "fileNames.txt");
        BufferedReader reader = new BufferedReader(new FileReader(fileNamesFile));
        while (reader.ready()) {
            fileNames.add(reader.readLine());
        }
        reader.close();
        this.threadId = threadId;
    }

    private final int threadId;

    private final BlockedCompressedDictionary postingAddr;
    private final List<String> fileNames = new ArrayList<>();

    private final File indexFile;

    @Override
    public ZonedRetrievalResult retrieve(String phrase) {
        List<String> terms = tokenize(phrase);
        String term = terms.getFirst();
        if(!postingAddr.containsKey(term)) {
            return new ZonedRetrievalResult(new PostingsList<>());
        }
        try{
            int postingOffset = postingAddr.get(term);
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
            in.skipBytes(postingOffset);
            PostingsList<ZonedPosting> postings = new PostingsList<>();
            postings.readPostingsList(in, new ZonedPostingFactory());
            return new ZonedRetrievalResult(postings);
        } catch(Exception e){
            return new ZonedRetrievalResult(new PostingsList<>());
        }
    }

    @Override
    public ZonedRetrievalResult retrieve(String term1, String term2, int distance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ZonedRetrievalResult retrieveAll() {
        PostingsList<ZonedPosting> postings = new PostingsList<>();
        for(int i = 0; i < fileNames.size(); i++){
            postings.addPosting(new ZonedPosting(threadId, i));
        }
        return new ZonedRetrievalResult(postings);
    }

    @Override
    public String getFile(int fileId) {
        return fileNames.get(fileId);
    }
}
