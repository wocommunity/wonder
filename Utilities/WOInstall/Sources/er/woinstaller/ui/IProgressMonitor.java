package er.woinstaller.ui;

public interface IProgressMonitor {
  public void progress(long amount, long totalSize);
  
  public void done();
}
