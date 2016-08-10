package er.woinstaller.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import er.woinstaller.ui.IWOInstallerProgressMonitor;

public class FileUtilities {

  /**
   * Copies the contents of the given URL to a file.
   * 
   * @param url the URL to copy from
   * @param file the File to write to 
   * @throws IOException if the copy fails
   */
  public static void writeUrlToFile(URL url, File file, IWOInstallerProgressMonitor progressMonitor) throws IOException {
    URLConnection conn = url.openConnection();
    int totalSize = conn.getContentLength();
    FileUtilities.writeInputStreamToFile(conn.getInputStream(), file, totalSize, progressMonitor);
  }

  /**
   * Writes the contents of an InputStream to a specified file.
   * @param file to write to
   * @param stream to pull data from
   */
  public static void writeInputStreamToFile(InputStream stream, File file, int totalSize, IWOInstallerProgressMonitor progressMonitor) throws IOException {
    OutputStream out;
    try {
      if (file == null)
        throw new IllegalArgumentException("Attempting to write to a null file!");
      File parent = file.getParentFile();
      if (parent != null && !parent.exists() && !parent.mkdirs()) {
          throw new IllegalArgumentException("Failed to create directory " + parent.getAbsolutePath());        
      }
      out = new BufferedOutputStream(new FileOutputStream(file));
    }
    catch (IOException e) {
      stream.close();
      throw e;
    }
    catch (RuntimeException e) {
      stream.close();
      throw e;
    }
    writeInputStreamToOutputStream(stream, out, totalSize, progressMonitor);
  }

  /**
   * Copies the contents of the input stream to the given output stream.  Both streams are
   * guaranteed to be closed by the end of this method.
   * 
   * @param in the input stream to copy from
   * @param out the output stream to copy to
   * @throws IOException if there is any failure
   */
  public static void writeInputStreamToOutputStream(InputStream in, OutputStream out, int totalSize, IWOInstallerProgressMonitor progressMonitor) throws IOException {
    try {
      BufferedInputStream bis = new BufferedInputStream(in);
      try {
        byte buf[] = new byte[1024 * 50]; //64 KBytes buffer
        int read = -1;
        while ((read = bis.read(buf, 0, buf.length)) != -1) {
          out.write(buf, 0, read);
          progressMonitor.worked((int)read);
          if (progressMonitor.isCanceled()) {
            throw new IOException("Operation canceled");
          }
        }
      }
      finally {
        bis.close();
      }
      out.flush();
    }
    finally {
      out.close();
    }
  }

}
