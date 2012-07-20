package er.woinstaller.ui;


public class NullProgressMonitor implements IWOInstallerProgressMonitor {
  @Override
  public void done() {
    // DO NOTHING
  }
  
  @Override
  public void beginTask(String taskName, long totalWork) {
    // DO NOTHING
  }
  
  @Override
  public boolean isCanceled() {
    return false;
  }
  
  @Override
  public void worked(long amount) {
    // DO NOTHING
  }
}
