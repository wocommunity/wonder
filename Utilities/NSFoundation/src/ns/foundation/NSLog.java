package ns.foundation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public final class NSLog {
  public static final int DebugLevelOff = 0;
  public static final int DebugLevelCritical = 1;
  public static final int DebugLevelInformational = 2;
  public static final int DebugLevelDetailed = 3;
  public static volatile Logger debug = new JDKLogger(DebugLevelDetailed);
  public static volatile Logger err = new JDKLogger(DebugLevelCritical);
  public static volatile Logger out = new JDKLogger(DebugLevelInformational);

  public static void _conditionallyLogPrivateException(Throwable t) {
    if (_debugLoggingAllowedForLevel(DebugLevelDetailed)) {
      debug.appendln(t);
    }
  }

  public static boolean _debugLoggingAllowedForLevel(int aDebugLevel) {
    return debugLoggingAllowedForLevel(aDebugLevel);
  }

  public static boolean debugLoggingAllowedForLevel(int aDebugLevel) {
    return ((aDebugLevel <= 1) || ((aDebugLevel > 0) && (aDebugLevel <= debug.allowedDebugLevel())));
  }

  public static void setDebug(Logger instance) {
    if (instance != null) {
      debug = instance;
    }
  }

  public static void setDebug(Logger instance, int aDebugLevel) {
    if (instance != null) {
      instance.setAllowedDebugLevel(aDebugLevel);
      debug = instance;
    }
  }

  public static void setErr(Logger instance) {
    if (instance != null) {
      err = instance;
    }
  }

  public static void setOut(Logger instance) {
    if (instance != null) {
      out = instance;
    }
  }

  public static String throwableAsString(Throwable t) {
    final StringBuffer sb = new StringBuffer();
    OutputStream os = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        sb.append((char) b);
      }
    };
    t.printStackTrace(new PrintStream(os));
    return sb.toString();
  }

  static {
    out.setIsVerbose(false);
    debug.setIsVerbose(true);
    err.setIsVerbose(true);
  }

  public static class JDKLogger extends NSLog.Logger {
    java.util.logging.Logger log = java.util.logging.Logger.getLogger("NSLog");
    
    public JDKLogger() {
    }

    public JDKLogger(int level) {
      setAllowedDebugLevel(level);
    }
    
    @Override
    public void appendln() {
      appendln("");
    }

    @Override
    public void appendln(Object paramObject) {
      switch (allowedDebugLevel()) {
      case DebugLevelCritical:
        log.severe(paramObject.toString());
        break;
      case DebugLevelInformational:
        log.info(paramObject.toString());
        break;
      case DebugLevelDetailed:
        log.fine(paramObject.toString());
      }
    }

    @Override
    public void flush() {
    }

  }

  public static abstract class Logger {
    protected int debugLevel = 0;
    protected boolean isEnabled = true;
    protected boolean isVerbose = true;

    public int allowedDebugLevel() {
      return this.debugLevel;
    }

    public void appendln(boolean aValue) {
      appendln((aValue) ? Boolean.TRUE : Boolean.FALSE);
    }

    public void appendln(byte aValue) {
      appendln(Byte.valueOf(aValue));
    }

    public void appendln(byte[] aValue) {
      appendln(Arrays.toString(aValue));
    }

    public void appendln(char aValue) {
      appendln(Character.valueOf(aValue));
    }

    public void appendln(char[] aValue) {
      appendln(new String(aValue));
    }

    public void appendln(double aValue) {
      appendln(Double.valueOf(aValue));
    }

    public void appendln(float aValue) {
      appendln(Float.valueOf(aValue));
    }

    public void appendln(int aValue) {
      appendln(Integer.valueOf(aValue));
    }

    public void appendln(long aValue) {
      appendln(Long.valueOf(aValue));
    }

    public void appendln(short aValue) {
      appendln(Short.valueOf(aValue));
    }

    public void appendln(Throwable aValue) {
      appendln(NSLog.throwableAsString(aValue));
    }

    public abstract void appendln(Object paramObject);

    public abstract void appendln();

    public abstract void flush();

    public boolean isEnabled() {
      return this.isEnabled;
    }

    public boolean isVerbose() {
      return this.isVerbose;
    }

    public void setAllowedDebugLevel(int aDebugLevel) {
      if ((aDebugLevel >= 0) && (aDebugLevel <= 3)) {
        this.debugLevel = aDebugLevel;
      } else {
        throw new IllegalArgumentException("<" + super.getClass().getName() + "> Invalid debug level: " + aDebugLevel);
      }
    }

    public void setIsEnabled(boolean aBool) {
      this.isEnabled = aBool;
    }

    public void setIsVerbose(boolean aBool) {
      this.isVerbose = aBool;
    }
  }
}
