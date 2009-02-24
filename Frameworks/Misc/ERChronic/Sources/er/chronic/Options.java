package er.chronic;

import java.util.Calendar;
import java.util.List;

import er.chronic.tags.Pointer;
import er.chronic.utils.EndianPrecedence;

public class Options {
  private Pointer.PointerType _context;
  private Calendar _now;
  private boolean _guess;
  private boolean _debug;
  private Integer _ambiguousTimeRange;
  private boolean _compatibilityMode;
  private List<EndianPrecedence> _endianPrecedence;

  public Options() {
    this(Pointer.PointerType.FUTURE, Calendar.getInstance(), true, Integer.valueOf(6));
  }

  public Options(Calendar now) {
    this(Pointer.PointerType.FUTURE, now, true, Integer.valueOf(6));
  }

  public Options(Calendar now, boolean guess) {
    this(Pointer.PointerType.FUTURE, now, guess, Integer.valueOf(6));
  }

  public Options(Pointer.PointerType context) {
    this(context, Calendar.getInstance(), true, Integer.valueOf(6));
  }

  public Options(boolean guess) {
    this(Pointer.PointerType.FUTURE, Calendar.getInstance(), guess, Integer.valueOf(6));
  }

  public Options(Integer ambiguousTimeRange) {
    this(Pointer.PointerType.FUTURE, Calendar.getInstance(), true, ambiguousTimeRange);
  }

  public Options(Pointer.PointerType context, Calendar now, boolean guess, Integer ambiguousTimeRange) {
    _context = context;
    _now = now;
    _guess = guess;
    _ambiguousTimeRange = ambiguousTimeRange;
    _endianPrecedence = null;
  }
  
  public List<EndianPrecedence> getEndianPrecedence() {
    return _endianPrecedence;
  }
  
  public void setEndianPrecedence(List<EndianPrecedence> endianPrecedence) {
    _endianPrecedence = endianPrecedence;
  }
  
  public void setDebug(boolean debug) {
    _debug = debug;
  }
  
  public boolean isDebug() {
    return _debug;
  }
  
  public void setCompatibilityMode(boolean compatibilityMode) {
    _compatibilityMode = compatibilityMode;
  }
  
  public boolean isCompatibilityMode() {
    return _compatibilityMode;
  }

  public void setContext(Pointer.PointerType context) {
    _context = context;
  }

  public Pointer.PointerType getContext() {
    return _context;
  }

  public void setNow(Calendar now) {
    _now = now;
  }

  public Calendar getNow() {
    return _now;
  }

  public void setGuess(boolean guess) {
    _guess = guess;
  }

  public boolean isGuess() {
    return _guess;
  }

  public void setAmbiguousTimeRange(Integer ambiguousTimeRange) {
    _ambiguousTimeRange = ambiguousTimeRange;
  }

  public Integer getAmbiguousTimeRange() {
    return _ambiguousTimeRange;
  }
}
