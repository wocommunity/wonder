package er.extensions;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import er.extensions.*;

public class ERXCompressionUtilities {

    public static final ERXLogger log = ERXLogger.getERXLogger(ERXCompressionUtilities.class);
    
    public static byte[] gzipByteArray(byte[] input) {
        try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        GZIPOutputStream out = new GZIPOutputStream(bos);

        out.write(input, 0, input.length);

        out.finish();
        out.close();

        byte[] compressedData = bos.toByteArray();
        return compressedData;
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] gunzipByteArray(byte[] input) {
        try {
            ByteArrayInputStream bos = new ByteArrayInputStream(input);
            GZIPInputStream in = new GZIPInputStream(bos);

            byte[] uncompressedData = ERXFileUtilities.bytesFromInputStream(in);
            return uncompressedData;
        } catch (IOException e) {
            return null;
        }
    }
    
    public static String gzipString(String source) {
        try {
            return new String(gzipByteArray(source.getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String gunzipString(String source) {
        try {
            byte[] b = gunzipByteArray(source.getBytes("UTF-8"));
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String gunzipByteArrayAsString(byte[] input) {
        try {
            byte[] b = gunzipByteArray(input);

            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }    

    public static byte[] gzipStringAsByteArray(String source) {
        try {
            return gzipByteArray(source.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] zipByteArray(byte[] input, String zipEntryName) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
            ZipOutputStream out = new ZipOutputStream(bos);

            out.putNextEntry(new ZipEntry(zipEntryName));
            
            out.write(input, 0, input.length);

            out.closeEntry();
            out.finish();
            out.close();

            byte[] compressedData = bos.toByteArray();
            return compressedData;
        } catch (IOException e) {
            log.error("Caught exception zipping byte array: " + e, e);
            return null;
        }
    }    
    
    public static byte[] zipByteArray(byte[] input) {
        return zipByteArray(input, "tmp");
    }

    public static byte[] unzipByteArray(byte[] input) {
        try {
        ByteArrayInputStream bos = new ByteArrayInputStream(input);
        ZipInputStream in = new ZipInputStream(bos);

        byte[] uncompressedData = ERXFileUtilities.bytesFromInputStream(in);
        return uncompressedData;
        } catch (IOException e) {
            return null;
        }
    }

    public static String zipString(String source) {
        try {
        return new String(zipByteArray(source.getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String unzipString(String source) {
        try {
        byte[] b = unzipByteArray(source.getBytes("UTF-8"));
        return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] deflateByteArray(byte[] input) {
        try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        DeflaterOutputStream out = new DeflaterOutputStream(bos);

        out.write(input, 0, input.length);

        out.finish();
        out.close();

        byte[] compressedData = bos.toByteArray();
        return compressedData;
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] inflateByteArray(byte[] input) {
        try {
        ByteArrayInputStream bos = new ByteArrayInputStream(input);
        InflaterInputStream in = new InflaterInputStream(bos);

        byte[] uncompressedData = ERXFileUtilities.bytesFromInputStream(in);
        return uncompressedData;
        } catch (IOException e) {
            return null;
        }
    }

    public static String deflateString(String source) {
        try {
        return new String(deflateByteArray(source.getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String inflateString(String source) {
        try {
        byte[] b = inflateByteArray(source.getBytes("UTF-8"));
        return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
}
