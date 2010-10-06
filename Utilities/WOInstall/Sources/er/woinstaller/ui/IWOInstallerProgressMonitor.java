package er.woinstaller.ui;

public interface IWOInstallerProgressMonitor {
  public boolean isCanceled();
  
  public void beginTask(String taskName, int totalWork);
  
  public void worked(int amount);
  
  public void done();
}
