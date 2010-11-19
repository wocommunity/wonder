package ns.foundation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class _NSBase64 {

  public static byte[] encode(byte binaryData[]) {
    return encode(binaryData, 0, binaryData.length);
  }

  public static byte[] encode(byte binaryData[], int off, int len) {
    return new Codec().encode(binaryData, off, len);
  }
  
  public static byte[] decode(byte base64Data[]) {
    return new Codec().decode(base64Data);
  }
  
  public static String decode(String base64Data) {
    return new String(new Codec().decode(base64Data.getBytes(Charset.forName("UTF-8"))));
  }


  private static class Codec {
    public byte[] encode(byte binaryData[], int off, int len) {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      try {
        for (int i = 0; i < len; i += 3)
          if (i + 3 <= len)
            encodeAtom(outputstream, binaryData, i, 3);
          else
            encodeAtom(outputstream, binaryData, i, len - i);
      } catch (IOException e) {
        throw new Error("_NBase64.encode internal error");
      }
      return outputstream.toByteArray();
    }

    public byte[] decode(byte[] base64Data) {
      ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
      try {
        for (int i = 0; i < base64Data.length; i += 4)
          if (i + 4 <= base64Data.length)
            decodeAtom(outputstream, base64Data, i, 4);
          else
            decodeAtom(outputstream, base64Data, i, base64Data.length - i);
      } catch (IOException e) {
        throw new Error("_NBase64.encode internal error");
      }
      return outputstream.toByteArray();
    }

    protected static void encodeAtom(OutputStream outputstream, byte abyte0[], int i, int j) throws IOException {
      if (j == 1) {
        byte byte0 = abyte0[i];
        int k = 0;
        outputstream.write(pem_array[byte0 >>> 2 & 63]);
        outputstream.write(pem_array[(byte0 << 4 & 48) + (k >>> 4 & 15)]);
        outputstream.write(61);
        outputstream.write(61);
      } else if (j == 2) {
        byte byte1 = abyte0[i];
        byte byte3 = abyte0[i + 1];
        int l = 0;
        outputstream.write(pem_array[byte1 >>> 2 & 63]);
        outputstream.write(pem_array[(byte1 << 4 & 48) + (byte3 >>> 4 & 15)]);
        outputstream.write(pem_array[(byte3 << 2 & 60) + (l >>> 6 & 3)]);
        outputstream.write(61);
      } else {
        byte byte2 = abyte0[i];
        byte byte4 = abyte0[i + 1];
        byte byte5 = abyte0[i + 2];
        outputstream.write(pem_array[byte2 >>> 2 & 63]);
        outputstream.write(pem_array[(byte2 << 4 & 48) + (byte4 >>> 4 & 15)]);
        outputstream.write(pem_array[(byte4 << 2 & 60) + (byte5 >>> 6 & 3)]);
        outputstream.write(pem_array[byte5 & 63]);
      }
    }

    private static final char pem_array[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
        'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    protected void decodeAtom(OutputStream outputstream, byte[] abyte0, int i, int j) throws IOException {
      byte byte0 = -1;
      byte byte1 = -1;
      byte byte2 = -1;
      byte byte3 = -1;
      if (j < 2)
        throw new Error("_NSBase64: Not enough bytes for an atom.");

      if (j > 3 && abyte0[i+3] == 61)
        j = 3;
      if (j > 2 && abyte0[i+2] == 61)
        j = 2;
      switch (j) {
      case 4: // '\004'
        byte3 = pem_convert_array[abyte0[i+3] & 255];
        // fall through

      case 3: // '\003'
        byte2 = pem_convert_array[abyte0[i+2] & 255];
        // fall through

      case 2: // '\002'
        byte1 = pem_convert_array[abyte0[i+1] & 255];
        byte0 = pem_convert_array[abyte0[i+0] & 255];
        // fall through

      default:
        switch (j) {
        case 2: // '\002'
          outputstream.write((byte) (byte0 << 2 & 252 | byte1 >>> 4 & 3));
          break;

        case 3: // '\003'
          outputstream.write((byte) (byte0 << 2 & 252 | byte1 >>> 4 & 3));
          outputstream.write((byte) (byte1 << 4 & 240 | byte2 >>> 2 & 15));
          break;

        case 4: // '\004'
          outputstream.write((byte) (byte0 << 2 & 252 | byte1 >>> 4 & 3));
          outputstream.write((byte) (byte1 << 4 & 240 | byte2 >>> 2 & 15));
          outputstream.write((byte) (byte2 << 6 & 192 | byte3 & 63));
          break;
        }
        break;
      }
    }

    private static final byte pem_convert_array[];

    static {
      pem_convert_array = new byte[256];
      for (int i = 0; i < 255; i++)
        pem_convert_array[i] = -1;

      for (int j = 0; j < pem_array.length; j++)
        pem_convert_array[pem_array[j]] = (byte) j;

    }

  }

}
