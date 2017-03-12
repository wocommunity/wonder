package er.chronic.repeaters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class RepeaterSeasonName extends Repeater<Object> {
  public static Pattern SPRING_PATTERN = Pattern.compile("^springs?$"); 
  public static Pattern SUMMER_PATTERN = Pattern.compile("^summers?$"); 
  public static Pattern AUTUMN_PATTERN = Pattern.compile("^(autumn|fall)s?$"); 
  public static Pattern WINTER_PATTERN = Pattern.compile("^winters?$"); 

  public static enum SeasonName {
    SPRING, SUMMER, AUTUMN, WINTER
  }

  public RepeaterSeasonName(Object type) {
    super(type);
  }

  @Override
  protected Span _nextSpan(PointerType pointer) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public Span getOffset(Span span, float amount, PointerType pointer) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return (91 * 24 * 60 * 60);
  }

  @Override
  public String toString() {
    return super.toString() + "-season-" + getType();
  }

  public static RepeaterSeasonName scan(Token token) {
    Map<Pattern, RepeaterSeasonName.SeasonName> scanner = new HashMap<>();
    scanner.put(RepeaterSeasonName.SPRING_PATTERN, RepeaterSeasonName.SeasonName.SPRING);
    scanner.put(RepeaterSeasonName.SUMMER_PATTERN, RepeaterSeasonName.SeasonName.SUMMER);
    scanner.put(RepeaterSeasonName.AUTUMN_PATTERN, RepeaterSeasonName.SeasonName.AUTUMN);
    scanner.put(RepeaterSeasonName.WINTER_PATTERN, RepeaterSeasonName.SeasonName.WINTER);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new RepeaterSeasonName(scanner.get(scannerItem));
      }
    }
    return null;
  }
}
