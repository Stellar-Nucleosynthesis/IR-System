package utils.spimi_index_constructor;

import postings.Posting;
import postings.PostingFactory;
import postings.PostingsList;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public class BufferedRecordPQ<T extends Posting<T>> {
    public BufferedRecordPQ(List<File> files, PostingFactory<T> factory) throws IOException {
        for (File file : files) {
            BufferedRecordReader<T> reader = new BufferedRecordReader<>(file, factory);
            if (reader.hasNext()) {
                pq.add(reader);
            }
        }
    }

    private final PriorityQueue<BufferedRecordReader<T>> pq = new PriorityQueue<>(Comparator.comparing(o -> o.term));

    public void poll() throws IOException {
        if(!hasNext()) throw new NoSuchElementException();
        BufferedRecordReader<T> reader = pq.poll();
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

    public PostingsList<T> peekPosting(){
        if(!hasNext()) throw new NoSuchElementException();
        assert pq.peek() != null;
        return pq.peek().postings;
    }

    public boolean hasNext(){
        return !pq.isEmpty();
    }
}