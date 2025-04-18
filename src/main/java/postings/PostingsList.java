package postings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;
import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class PostingsList<P extends Posting<P>> {
    public PostingsList(){
        this.postings = new ArrayList<>();
    }

    public PostingsList(List<P> postings) {
        this.postings = postings;
    }

    private List<P> postings;

    public int writePostingsList(DataOutputStream out) throws IOException{
        int bytesWritten = 0;
        bytesWritten += writeCodedInt(out, postings.size());
        for (P posting : postings) {
            bytesWritten += posting.writePosting(out);
        }
        return bytesWritten;
    }

    public void readPostingsList(DataInputStream in, PostingFactory<P> factory) throws IOException{
        int len = readCodedInt(in);
        postings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            P posting = factory.create(0, 0);
            posting.readPosting(in);
            postings.add(posting);
        }
    }

    public List<P> getPostings(){
        return postings;
    }

    public void addPosting(P posting){
        for(P p : postings){
            if(p.equals(posting)){
                p.merge(posting);
                return;
            }
        }
        postings.add(posting);
        Collections.sort(postings);
    }

    public void merge(PostingsList<P> other){
        List<P> merged = new ArrayList<>();
        int i = 0, j = 0;
        while (i < postings.size() && j < other.postings.size()) {
            P a = postings.get(i);
            P b = other.postings.get(j);
            int cmp = a.compareTo(b);
            if (cmp < 0) {
                merged.add(a);
                i++;
            } else if (cmp > 0) {
                merged.add(b);
                j++;
            } else {
                a.merge(b);
                merged.add(a);
                i++;
                j++;
            }
        }
        while (i < postings.size()) {
            merged.add(postings.get(i++));
        }
        while (j < other.postings.size()) {
            merged.add(other.postings.get(j++));
        }
        postings = merged;
    }

    public void intersect(PostingsList<P> other){
        List<P> intersected = new ArrayList<>();
        int i = 0, j = 0;
        while (i < postings.size() && j < other.postings.size()) {
            P a = postings.get(i);
            P b = other.postings.get(j);
            int cmp = a.compareTo(b);
            if (cmp < 0) {
                i++;
            } else if (cmp > 0) {
                j++;
            } else {
                a.intersect(b);
                intersected.add(a);
                i++;
                j++;
            }
        }
        postings = intersected;
    }

    public void subtract(PostingsList<P> other){
        List<P> subtracted = new ArrayList<>();
        int i = 0, j = 0;
        while (i < postings.size() && j < other.postings.size()) {
            P a = postings.get(i);
            P b = other.postings.get(j);
            int cmp = a.compareTo(b);
            if (cmp < 0) {
                subtracted.add(a);
                i++;
            } else if (cmp > 0) {
                j++;
            } else {
                i++;
                j++;
            }
        }
        while (i < postings.size()) {
            subtracted.add(postings.get(i++));
        }
        postings = subtracted;
    }

    public void sort(Comparator<P> comparator){
        postings.sort(comparator);
    }
}
