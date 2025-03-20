package EncodingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VaribleByteEncoding {
    public static int writeCodedInt(OutputStream stream, int value) throws IOException {
        int bytesWritten = 0;
        do{
            byte b = (byte) (value & 0x7F);
            value >>>= 7;
            if(value == 0)
                b |= (byte) 0x80;
            stream.write(b);
            bytesWritten++;
        }
        while(value != 0);
        return bytesWritten;
    }

    public static int readCodedInt(InputStream stream) throws IOException {
        int res = 0;
        for(int i = 0; i < 5; i++){
            byte b = stream.readNBytes(1)[0];
            res |= (b & 0x7F) << i * 7;
            if((b & 0x80) != 0)
                break;
        }
        return res;
    }
}
