package er.woinstaller.ui;


public class ConsoleProgressMonitor implements IProgressMonitor {
  private String _name;

  public ConsoleProgressMonitor(String name) {
    _name = name;
  }

  public void progress(long amount, long totalSize) {
    System.out.print(_name + ": " + (int) (((float) amount / (float) totalSize) * 100.0) + "%  \r");
  }

  public void done() {
    System.out.println(_name + ": Done");
  }
}
