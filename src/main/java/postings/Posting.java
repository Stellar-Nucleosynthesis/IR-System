package postings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Posting<T extends Posting<T>> extends Comparable<T> {
    int writePosting(DataOutputStream out) throws IOException;

    void readPosting(DataInputStream in) throws IOException;

    int getFileId();

    int getThreadId();

    double getRating();

    void merge(T other);

    void intersect(T other);

    void subtract(T other);

    int compareTo(T other);
}
