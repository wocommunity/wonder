package er.extensions.foundation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSData;


public class ERXCompressionUtilities {

	public static final Logger log = Logger.getLogger(ERXCompressionUtilities.class);

	/**
	 * Returns an NSData containing the gzipped version of the given input stream.
	 * 
	 * @param input the input stream to compress
	 * @param length the length of the input stream
	 * @return gzipped NSData
	 */
	public static NSData gzipInputStreamAsNSData(InputStream input, int length) {
		try {
			ERXRefByteArrayOutputStream bos = new ERXRefByteArrayOutputStream(length);
			if (input != null) {
				GZIPOutputStream out = new GZIPOutputStream(bos);
				try {
					ERXFileUtilities.writeInputStreamToOutputStream(input, true, out, false);
				}
				finally {
					try {
						out.finish();
					}
					finally {
						out.close();
					}
				}
			}
			return bos.toNSData();
		}
		catch (IOException e) {
			log.error("Failed to gzip byte array.", e);
			return null;
		}
	}

	public static NSData gzipNSDataAsNSData(NSData data) {
		NSData gzippedData = null;
		if (data != null) {
			gzippedData = ERXCompressionUtilities.gzipByteArrayAsNSData(data._bytesNoCopy(), 0, data.length());
		}
		return gzippedData;
	}
	
	public static NSData gzipByteArrayAsNSData(byte[] input, int offset, int length) {
		try {
			ERXRefByteArrayOutputStream bos = new ERXRefByteArrayOutputStream(length);
			if (input != null) {
				GZIPOutputStream out = new GZIPOutputStream(bos);
	
				out.write(input, offset, length);
	
				out.finish();
				out.close();
			}
			return bos.toNSData();
		}
		catch (IOException e) {
			log.error("Failed to gzip byte array.", e);
			return null;
		}
	}

	public static byte[] gzipByteArray(byte[] input) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
			GZIPOutputStream out = new GZIPOutputStream(bos);

			out.write(input, 0, input.length);

			out.finish();
			out.close();

			byte[] compressedData = bos.toByteArray();
			return compressedData;
		}
		catch (IOException e) {
			log.error("Failed to gzip byte array.", e);
			return null;
		}
	}

	public static byte[] gunzipByteArray(byte[] input) {
		try {
			ByteArrayInputStream bos = new ByteArrayInputStream(input);
			GZIPInputStream in = new GZIPInputStream(bos);

			byte[] uncompressedData = ERXFileUtilities.bytesFromInputStream(in);
			return uncompressedData;
		}
		catch (IOException e) {
			return null;
		}
	}

	public static String gunzipString(String source) {
		try {
			byte[] b = gunzipByteArray(source.getBytes(CharEncoding.UTF_8));
			return new String(b, CharEncoding.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String gunzipByteArrayAsString(byte[] input) {
		try {
			byte[] b = gunzipByteArray(input);

			return new String(b, CharEncoding.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static byte[] gzipStringAsByteArray(String source) {
		try {
			return gzipByteArray(source.getBytes(CharEncoding.UTF_8));
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static byte[] zipByteArray(byte[] input) {
		return ERXCompressionUtilities.zipByteArray(input, "tmp");
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
		}
		catch (IOException e) {
			log.error("Caught exception zipping byte array: " + e, e);
			return null;
		}
	}

	public static File unzipByteArrayIntoDirectory(byte[] input, File directory, boolean overwrite) {
		try {
			if (!directory.exists()) {
				directory.mkdirs();
				directory.mkdir();
				if (!directory.exists()) {
					throw new IllegalStateException("could not create directory " + directory);
				}
			}
			else {
				if (!overwrite) {
					throw new IllegalStateException("overwrite is false and file " + directory + " does exist");
				}
				else if (!directory.isDirectory()) {
					throw new IllegalArgumentException("file " + directory + " is NOT an directory");
				}
			}

			long start = System.currentTimeMillis();
			ByteArrayInputStream bos = new ByteArrayInputStream(input);
			ZipInputStream in = new ZipInputStream(bos);
			ZipEntry entry = null;
			while ((entry = in.getNextEntry()) != null) {

				String oriName = entry.getName();
				String filename = directory.getAbsolutePath() + File.separator + oriName;

				if (entry.isDirectory()) {
					if (log.isDebugEnabled())
						log.debug("creating directory " + oriName);

					File f = new File(filename);
					f.mkdirs();
					f.mkdir();

				}
				else {
					int uncompressedSize = (int) entry.getSize();

					if (uncompressedSize > -1) {
						byte[] b = new byte[uncompressedSize];
						in.read(b);
						FileOutputStream fis = new FileOutputStream(filename);
						fis.write(b);
						fis.flush();
						fis.close();

						if (log.isDebugEnabled())
							log.debug("unzipped entry " + filename);
					}
				}
				long end1 = System.currentTimeMillis();

			}
			long end = System.currentTimeMillis();
			if (log.isDebugEnabled())
				log.debug("whole decompression took " + (end - start));
			return directory;
		}
		catch (IOException e) {
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
		}
		catch (IOException e) {
			return null;
		}
	}

	public static byte[] inflateByteArray(byte[] input) {
		try {
			ByteArrayInputStream bos = new ByteArrayInputStream(input);
			InflaterInputStream in = new InflaterInputStream(bos);

			byte[] uncompressedData = ERXFileUtilities.bytesFromInputStream(in);
			return uncompressedData;
		}
		catch (IOException e) {
			return null;
		}
	}

	public static String deflateString(String source) {
		try {
			return new String(deflateByteArray(source.getBytes(CharEncoding.UTF_8)), CharEncoding.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String inflateString(String source) {
		try {
			byte[] b = inflateByteArray(source.getBytes(CharEncoding.UTF_8));
			return new String(b, CharEncoding.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}

}
