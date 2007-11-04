package er.chronic.repeaters;

import java.util.List;

import er.chronic.Options;
import er.chronic.tags.Pointer;
import er.chronic.tags.Tag;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public abstract class Repeater<T> extends Tag<T> implements Comparable<Repeater<?>> {
  public Repeater(T type) {
    super(type);
  }

  public static List<Token> scan(List<Token> tokens) {
    return Repeater.scan(tokens, new Options());
  }

  public static List<Token> scan(List<Token> tokens, Options options) {
    for (Token token : tokens) {
      Tag<?> t;
      t = RepeaterMonthName.scan(token);
      if (t != null) {
        token.tag(t);
      }
      t = RepeaterDayName.scan(token);
      if (t != null) {
        token.tag(t);
      }
      t = RepeaterDayPortion.scan(token);
      if (t != null) {
        token.tag(t);
      }
      t = RepeaterTime.scan(token, tokens, options);
      if (t != null) {
        token.tag(t);
      }
      t = RepeaterUnit.scan(token);
      if (t != null) {
        token.tag(t);
      }
    }
    return tokens;
  }

  public int compareTo(Repeater<?> other) {
    return Integer.valueOf(getWidth()).compareTo(Integer.valueOf(other.getWidth()));
  }

  /**
   * returns the width (in seconds or months) of this repeatable.
   */
  public abstract int getWidth();

  /** 
   * returns the next occurance of this repeatable.
   */
  public Span nextSpan(Pointer.PointerType pointer) {
    if (getNow() == null) {
      throw new IllegalStateException("Start point must be set before calling #next");
    }
    return _nextSpan(pointer);
  }

  protected abstract Span _nextSpan(Pointer.PointerType pointer);

  public Span thisSpan(Pointer.PointerType pointer) {
    if (getNow() == null) {
      throw new IllegalStateException("Start point must be set before calling #this");
    }
    return _thisSpan(pointer);
  }

  protected abstract Span _thisSpan(Pointer.PointerType pointer);

  public abstract Span getOffset(Span span, int amount, Pointer.PointerType pointer);

  @Override
  public String toString() {
    return "repeater";
  }
}
