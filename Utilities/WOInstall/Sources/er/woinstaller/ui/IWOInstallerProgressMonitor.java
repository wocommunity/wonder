package er.woinstaller.ui;

public interface IWOInstallerProgressMonitor {
  public boolean isCanceled();
  
  public void beginTask(String taskName, long totalWork);
  
  public void worked(long amount);
  
  public void done();
}
