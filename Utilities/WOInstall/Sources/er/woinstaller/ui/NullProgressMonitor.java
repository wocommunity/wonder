package er.woinstaller.ui;


public class NullProgressMonitor implements IProgressMonitor {
  public void done() {
    // DO NOTHING
  }

  public void progress(long amount, long totalSize) {
    // DO NOTHING
  }
}
