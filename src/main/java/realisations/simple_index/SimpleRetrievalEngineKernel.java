package realisations.simple_index;

import kernels.RetrievalEngineKernel;
import postings.PostingsList;
import utils.encoding_utils.BlockedCompressedDictionary;
import utils.file_parsing_utils.StemmingStringTokenizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleRetrievalEngineKernel implements RetrievalEngineKernel<SimpleRetrievalResult, SimplePosting> {
    public SimpleRetrievalEngineKernel(File workingDir, int threadId) throws IOException {
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
    public SimpleRetrievalResult retrieve(String phrase) {
        StemmingStringTokenizer tokenizer = new StemmingStringTokenizer();
        List<String> terms = tokenizer.tokenize(phrase);
        String term = terms.getFirst();
        if(!postingAddr.containsKey(term)) {
            return new SimpleRetrievalResult(new PostingsList<>());
        }
        try{
            int postingOffset = postingAddr.get(term);
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
            in.skipBytes(postingOffset);
            PostingsList<SimplePosting> postings = new PostingsList<>();
            postings.readPostingsList(in, SimplePosting::new);
            return new SimpleRetrievalResult(postings);
        } catch(Exception e){
            return new SimpleRetrievalResult(new PostingsList<>());
        }
    }

    @Override
    public SimpleRetrievalResult retrieve(String term1, String term2, int distance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SimpleRetrievalResult retrieveAll() {
        PostingsList<SimplePosting> postings = new PostingsList<>();
        for(int i = 0; i < fileNames.size(); i++){
            postings.addPosting(new SimplePosting(threadId, i));
        }
        return new SimpleRetrievalResult(postings);
    }

    @Override
    public String getFile(int fileId) {
        return fileNames.get(fileId);
    }
}
