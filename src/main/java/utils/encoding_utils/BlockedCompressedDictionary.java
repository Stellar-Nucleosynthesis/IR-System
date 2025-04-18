package utils.encoding_utils;

import java.io.*;
import java.security.InvalidKeyException;

import static utils.encoding_utils.VariableByteEncoding.readCodedInt;

public class BlockedCompressedDictionary {
    public BlockedCompressedDictionary(File file) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        int arrayLen = in.readInt();
        int stringLen = in.readInt();
        TERMS_IN_BLOCK = in.readInt();
        array = new int[arrayLen][TERMS_IN_BLOCK + 1];
        for (int i = 0; i < arrayLen; i++) {
            for (int j = 0; j < TERMS_IN_BLOCK + 1; j++) {
                array[i][j] = readCodedInt(in);
            }
        }
        string = new char[stringLen];
        for (int i = 0; i < stringLen; i++) {
            string[i] = in.readChar();
        }
        in.close();
        position = 0;
    }

    private int position;
    private final int TERMS_IN_BLOCK;
    private final char[] string;
    private final int[][] array;

    public int get(String term) throws InvalidKeyException {
        int lo = 0, hi = array.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int res = cmpToKey(array[mid], term);
            if(res == 0){
                return findValueIn(array[mid], term);
            } else if(res > 0){
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }
        throw new InvalidKeyException("Key " + term + " not present.");
    }

    public boolean containsKey(String term) {
        try{
            get(term);
        } catch(InvalidKeyException e){
            return false;
        }
        return true;
    }

    private int cmpToKey(int[] arr, String term) {
        position = arr[0];
        String prefix = readString(readChar());
        for (int i = 0; i < TERMS_IN_BLOCK; i++) {
            String currentTerm = prefix + readString(readChar());
            if(term.equals(currentTerm)) {
                return 0;
            }
            if(i == TERMS_IN_BLOCK - 1) {
                return currentTerm.compareTo(term);
            }
        }
        return -1;
    }

    private int findValueIn(int[] arr, String term) {
        position = arr[0];
        String prefix = readString(readChar());
        for (int i = 0; i < TERMS_IN_BLOCK; i++) {
            String suffix = readString(readChar());
            if(term.equals(prefix + suffix)) {
                return arr[i + 1];
            }
        }
        return -1;
    }

    private char readChar(){
        return string[position++];
    }

    private String readString(int n){
        int prevPos = position;
        position += n;
        return new String(string, prevPos, n);
    }
}
