package utils.encoding_utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static utils.encoding_utils.VariableByteEncoding.writeCodedInt;

public class BlockedDictionaryCompressor {
    private static final int TERMS_IN_BLOCK = 8;

    public static void writeCompressedMapping(File file, Map<String, Integer> postingAddr) throws IOException {
        ArrayList<String> keys = new ArrayList<>(postingAddr.keySet());
        Collections.sort(keys);
        int tail = keys.size() % TERMS_IN_BLOCK;
        if(tail != 0) {
            for(int i = 0; i < tail; i++) {
                keys.add(keys.getLast());
            }
        }
        int arrSize = keys.size() / TERMS_IN_BLOCK;
        StringBuilder resultStr = new StringBuilder();
        int[][] resultArr = new int[arrSize][TERMS_IN_BLOCK + 1];
        for(int i = 0; i < arrSize; i++) {
            resultArr[i][0] = resultStr.length();
            int index = i * TERMS_IN_BLOCK;
            String prefix = keys.get(index);
            for(int j = 1; j < TERMS_IN_BLOCK; j++) {
                prefix = commonPrefix(prefix, keys.get(index + j));
            }
            resultStr.append((char)prefix.length());
            resultStr.append(prefix);
            for(int j = 0; j < TERMS_IN_BLOCK; j++) {
                String suffix = keys.get(index + j).substring(prefix.length());
                resultStr.append((char)suffix.length());
                resultStr.append(suffix);
                resultArr[i][j + 1] = postingAddr.get(keys.get(index + j));
            }
        }
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.writeInt(resultArr.length);
        out.writeInt(resultStr.length());
        out.writeInt(TERMS_IN_BLOCK);
        for (int[] block : resultArr) {
            for (int anInt : block) {
                writeCodedInt(out, anInt);
            }
        }
        out.writeChars(resultStr.toString());
        out.close();
    }

    private static String commonPrefix(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        int i = 0;
        while (i < minLength && str1.charAt(i) == str2.charAt(i)) {
            i++;
        }
        return str1.substring(0, i);
    }
}
