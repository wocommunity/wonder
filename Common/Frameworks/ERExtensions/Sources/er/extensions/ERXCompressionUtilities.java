package er.extensions;

import java.io.*;
import java.util.zip.*;

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

    public static byte[] zipByteArray(byte[] input) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
            ZipOutputStream out = new ZipOutputStream(bos);

            out.write(input, 0, input.length);

            out.finish();
            out.close();

            byte[] compressedData = bos.toByteArray();
            return compressedData;
        } catch (IOException e) {
            return null;
        }
    }

    public static File unzipByteArrayIntoDirectory(byte[] input, File directory, boolean overwrite) {
        try {
            if (!directory.exists()) {
                directory.mkdirs();
                directory.mkdir();
                if (!directory.exists()) { throw new IllegalStateException("could not create directory " + directory); }
            } else {
                if (!overwrite) {
                    throw new IllegalStateException("overwrite is false and file " + directory + " does exist");
                } else if (!directory.isDirectory()) { throw new IllegalArgumentException("file " + directory + " is NOT an directory"); }
            }

            long start = System.currentTimeMillis();
            ByteArrayInputStream bos = new ByteArrayInputStream(input);
            ZipInputStream in = new ZipInputStream(bos);
            ZipEntry entry = null;
            while ((entry = in.getNextEntry()) != null) {
                long start1 = System.currentTimeMillis();

                String oriName = entry.getName();
                String filename = directory.getAbsolutePath() + File.separator + oriName;

                if (entry.isDirectory()) {
                    if (log.isDebugEnabled()) log.debug("creating directory " + oriName);

                    File f = new File(filename);
                    f.mkdirs();
                    f.mkdir();

                } else {
                    int uncompressedSize = (int) entry.getSize();

                    if (uncompressedSize > -1) {
                        byte[] b = new byte[uncompressedSize];
                        in.read(b);
                        FileOutputStream fis = new FileOutputStream(filename);
                        fis.write(b);
                        fis.flush();
                        fis.close();

                        if (log.isDebugEnabled()) log.debug("unzipped entry " + filename);
                    }
                }
                long end1 = System.currentTimeMillis();

            }
            long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) log.debug("whole decompression took " + (end - start));
            return directory;
        } catch (IOException e) {
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
