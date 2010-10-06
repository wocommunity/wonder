package er.woinstaller.ui;


public class NullProgressMonitor implements IWOInstallerProgressMonitor {
  public void done() {
    // DO NOTHING
  }
  
  public void beginTask(String taskName, int totalWork) {
    // DO NOTHING
  }
  
  public boolean isCanceled() {
    return false;
  }
  
  public void worked(int amount) {
    // DO NOTHING
  }
}
