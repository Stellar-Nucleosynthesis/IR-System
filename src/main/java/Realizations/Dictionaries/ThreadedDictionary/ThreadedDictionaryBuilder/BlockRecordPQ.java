package Realizations.Dictionaries.ThreadedDictionary.ThreadedDictionaryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

public class BlockRecordPQ {
    BlockRecordPQ(List<File> files) throws IOException {
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