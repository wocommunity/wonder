package er.woinstaller.ui;


public class ConsoleProgressMonitor implements IWOInstallerProgressMonitor {
  private String _name;
  private long _totalWork;

  public ConsoleProgressMonitor() {
  }
  
  public void beginTask(String taskName, long totalWork) {
    _name = taskName;
    _totalWork = totalWork;
  }
  
  public void worked(long amount) {
    System.out.print(_name + ": " + (int) (((float) amount / (float) _totalWork) * 100.0) + "%  \r");
  }

  public void done() {
    System.out.println(_name + ": Done                                           ");
  }

  public boolean isCanceled() {
    return false;
  }
}
