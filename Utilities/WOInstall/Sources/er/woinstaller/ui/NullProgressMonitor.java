package er.woinstaller.ui;


public class NullProgressMonitor implements IWOInstallerProgressMonitor {
  public void done() {
    // DO NOTHING
  }
  
  public void beginTask(String taskName, long totalWork) {
    // DO NOTHING
  }
  
  public boolean isCanceled() {
    return false;
  }
  
  public void worked(long amount) {
    // DO NOTHING
  }
}
