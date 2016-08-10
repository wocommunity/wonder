package er.woinstaller.archiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import er.woinstaller.io.BoundedInputStream;
import er.woinstaller.io.FileUtilities;
import er.woinstaller.ui.IWOInstallerProgressMonitor;
import er.woinstaller.ui.NullProgressMonitor;

public class CPIO {

  public static final int S_IFDIR = 16384;
  public static final int S_IFREG = 32768;
  public static final int S_IFLNK = 40960;

  private File _cpioFile;
  InputStream paxStream;
  long fileLength = 0;

  public CPIO(File cpioFile) throws FileNotFoundException {
	this(new BufferedInputStream(new FileInputStream(cpioFile)));
    _cpioFile = cpioFile;
    fileLength = _cpioFile.length();

  }

  public CPIO(InputStream input) {
	paxStream = input;
  }

  public void setLength(long length) {
	  this.fileLength = length;
  }

  @SuppressWarnings("unused")
  public void extractTo(File destinationFolder, boolean symbolicLinksSupported, IWOInstallerProgressMonitor progressMonitor) throws IOException, InterruptedException {
    progressMonitor.beginTask("Extracting WebObjects ...", fileLength);
    
    long amount = 0;
    List<Link> links = new LinkedList<Link>();

    try {
      byte[] sixBuffer = new byte[6];
      byte[] elevenBuffer = new byte[11];
      boolean done = false;
      do {
        String magic = readString(paxStream, sixBuffer);
        if (!"070707".equals(magic)) {
          throw new IOException("Expected magic '070707' but got '" + magic + "' (next = " + readString(paxStream, new byte[50]) + ").");
        }
        else {
          String dev = readString(paxStream, sixBuffer);
          String ino = readString(paxStream, sixBuffer);
          String modeStr = readString(paxStream, sixBuffer);
          String uid = readString(paxStream, sixBuffer);
          String gid = readString(paxStream, sixBuffer);
          String nlink = readString(paxStream, sixBuffer);
          String rdev = readString(paxStream, sixBuffer);
          String mtime = readString(paxStream, elevenBuffer);
          String nameSizeStr = readString(paxStream, sixBuffer);
          String fileSizeStr = readString(paxStream, elevenBuffer);

          int nameSize = Integer.parseInt(nameSizeStr, 8);
          String name = readString(paxStream, new byte[nameSize]);

          int fileSize = Integer.parseInt(fileSizeStr, 8);

          if ("TRAILER!!!".equals(name)) {
            done = true;
          }
          else {
            File destinationFile = toFile(destinationFolder, name);
            int mode = Integer.parseInt(modeStr, 8);
            if ((mode & S_IFDIR) == S_IFDIR) {
              if (".".equals(name)) {
                // skip
              }
              else if (destinationFile.exists()) {
                throw new IOException("The directory '" + destinationFile + "' already exists.");
              }
              else if (!destinationFile.mkdirs()) {
                throw new IOException("Failed to create directory '" + destinationFile + "'.");
              }
              skipFully(paxStream, fileSize);
            }
            else if ((mode & S_IFLNK) == S_IFLNK) {
              String realName = readString(paxStream, new byte[fileSize]);
              File realFile = new File(realName);
              if (!symbolicLinksSupported) {
                realFile = toFile(destinationFile.getParentFile(), realName);
              }
              links.add(new Link(realFile, destinationFile));
            }
            else if ((mode & S_IFREG) == S_IFREG) {
              if (destinationFile.exists()) {
                throw new IOException("The file '" + destinationFile + "' already exists.");
              }
              InputStream is = new BoundedInputStream(paxStream, 0, fileSize);
              FileOutputStream fos = new FileOutputStream(destinationFile);
              FileUtilities.writeInputStreamToOutputStream(is, fos, fileSize, new NullProgressMonitor());
            }
            else {
              throw new IOException("Unknown mode " + modeStr + " for " + name + ".");
            }

            int relativeAmount = 70 + nameSize + fileSize;
            amount += relativeAmount;
            progressMonitor.worked(amount);
          }
        }
        
        if (progressMonitor.isCanceled()) {
          throw new IOException("Operation canceled.");
        }
      } while (!done);
    }
    finally {
//      System.out.println(amount + ":" + fileLength);
  	  paxStream.close();
    }
    progressMonitor.done();
    progressMonitor.beginTask("Linking WebObjects ...", links.size());
    Collections.sort(links, new LinkNameLengthComparator());
    int linkNum = 0;
    for (Link link : links) {
      link.create(symbolicLinksSupported);
      progressMonitor.worked(linkNum++);
    }
  }

  protected File toFile(File workingDir, String path) {
    String localPath = path.replaceFirst("^\\./", "");
    localPath = localPath.replace("/", File.separator);
    File file = new File(localPath);
    if (!file.isAbsolute()) {
      file = new File(workingDir, localPath);
    }
    return file;
  }

  protected String readString(InputStream is, byte[] b) throws IOException {
    readFully(is, b);
    int length;
    for (length = b.length - 1; length >= 0 && b[length] == 0; length--) {
      // skip
    }
    return new String(b, 0, length + 1);
  }

  protected byte[] readFully(InputStream is, byte[] b) throws IOException {
    return readFully(is, b, 0, b.length);
  }

  protected byte[] readFully(InputStream is, byte[] b, int offset, int length) throws IOException {
    int totalAmountRead = 0;
    while (totalAmountRead < length) {
      int amountRead = is.read(b, offset + totalAmountRead, length - totalAmountRead);
      if (amountRead == -1) {
        throw new IOException("Stream ended before " + length + " bytes (read " + totalAmountRead + ")");
      }
      totalAmountRead += amountRead;
    }
    return b;
  }
  
  protected void skipFully(InputStream inputStream, long skip) throws IOException {
	  long toSkip = skip;
	  while (toSkip > 0) {
		  toSkip -= inputStream.skip(toSkip);
	  }
  }

  protected static class Link {
    private File _realFile;
    private File _linkFile;

    public Link(File realFile, File linkFile) {
      _realFile = realFile;
      _linkFile = linkFile;
    }

    public Link(String realName, String linkName) {
      _realFile = new File(realName);
      _linkFile = new File(linkName);
    }
    
    public File getRealFile() {
      return _realFile;
    }

    public File getLinkFile() {
      return _linkFile;
    }

    public void create(boolean symbolicLinksSupported) throws IOException, InterruptedException {
      if (symbolicLinksSupported) {
    	  
        Process p = Runtime.getRuntime().exec(new String[] { "/bin/ln", "-s", _realFile.getPath(), _linkFile.getCanonicalPath() });
        int retval = p.waitFor();
        if (retval != 0) {
          throw new IOException("Failed to create link from " + _realFile + " to " + _linkFile);
        }
      }
      else {
    	  try {
    		  copyFileToFile(_realFile, _linkFile);
    	  } catch (IOException e) {
    		  System.err.println(e.getMessage());
    	  }
      }
    }

    protected void copyFileToFile(File source, File destination) throws IOException {
      if (!source.exists()) {
        throw new IOException("The file '" + source + "' does not exist (tried to link to '" + destination + "').");
      }
      if (destination.exists()) {
        throw new IOException("The file '" + destination + "' already exists.");
      }
      if (source.isDirectory()) {
        if (!destination.mkdirs()) {
          throw new IOException("Failed to create the directory '" + destination + "'.");
        }
        for (File child : source.listFiles()) {
          copyFileToFile(child, new File(destination, child.getName()));
        }
      }
      else {
        FileInputStream fis = new FileInputStream(source);
        FileUtilities.writeInputStreamToFile(fis, destination, (int) source.length(), new NullProgressMonitor());
      }
    }
  }

  protected static class LinkNameLengthComparator implements Comparator<Link>, Serializable {

    public int compare(Link s1, Link s2) {
      int length1 = s1.getRealFile().toString().length();
      int length2 = s2.getRealFile().toString().length();
      int comparison;
      if (length1 > length2) {
        comparison = 1;
      }
      else if (length1 < length2) {
        comparison = -1;
      }
      else {
        comparison = 0;
      }
      return comparison;
    }
  }
}
