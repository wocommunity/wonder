package er.chronic.handlers;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import er.chronic.Options;
import er.chronic.repeaters.EnumRepeaterDayPortion;
import er.chronic.repeaters.IntegerRepeaterDayPortion;
import er.chronic.repeaters.Repeater;
import er.chronic.repeaters.RepeaterDayName;
import er.chronic.repeaters.RepeaterDayPortion;
import er.chronic.repeaters.RepeaterMonthName;
import er.chronic.repeaters.RepeaterTime;
import er.chronic.tags.Grabber;
import er.chronic.tags.Ordinal;
import er.chronic.tags.OrdinalDay;
import er.chronic.tags.Pointer;
import er.chronic.tags.Scalar;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarMonth;
import er.chronic.tags.ScalarYear;
import er.chronic.tags.Separator;
import er.chronic.tags.SeparatorAt;
import er.chronic.tags.SeparatorComma;
import er.chronic.tags.SeparatorIn;
import er.chronic.tags.SeparatorSlashOrDash;
import er.chronic.tags.Tag;
import er.chronic.tags.TimeZone;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class Handler {
  private static Map<Handler.HandlerType, List<Handler>> _definitions;

  public static enum HandlerType {
    TIME, DATE, ANCHOR, ARROW, NARROW
  }

  private HandlerPattern[] _patterns;
  private IHandler _handler;
  private boolean _compatible;

  public Handler(IHandler handler, HandlerPattern... patterns) {
    this(handler, true, patterns);
  }
  
  public Handler(IHandler handler, boolean compatible, HandlerPattern... patterns) {
    _handler = handler;
    _compatible = compatible;
    _patterns = patterns;
  }
  
  public boolean isCompatible(Options options) {
    return !options.isCompatibilityMode() || _compatible;
  }

  public IHandler getHandler() {
    return _handler;
  }

  public boolean match(List<Token> tokens, Map<Handler.HandlerType, List<Handler>> definitions) {
    // System.out.println("Handler.match: " + this);
    int tokenIndex = 0;
    for (HandlerPattern pattern : _patterns) {
      boolean optional = pattern.isOptional();
      if (pattern instanceof TagPattern) {
        boolean match = (tokenIndex < tokens.size() && tokens.get(tokenIndex).getTags(((TagPattern) pattern).getTagClass()).size() > 0);
        // System.out.println("Handler.match:   " + pattern + "=" + match);
        if (!match && !optional) {
          return false;
        }
        if (match) {
          tokenIndex++;
        }
        // next if !match && optional ?
      }
      else if (pattern instanceof HandlerTypePattern) {
        if (optional && tokenIndex == tokens.size()) {
          return true;
        }
        List<Handler> subHandlers = definitions.get(((HandlerTypePattern) pattern).getType());
        for (Handler subHandler : subHandlers) {
          if (subHandler.match(tokens.subList(tokenIndex, tokens.size()), definitions)) {
            return true;
          }
        }
        return false;
      }
    }
    if (tokenIndex != tokens.size()) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "[Handler: " + _handler + "]";
  }

  public static synchronized Map<Handler.HandlerType, List<Handler>> definitions() {
    if (_definitions == null) {
      Map<Handler.HandlerType, List<Handler>> definitions = new HashMap<Handler.HandlerType, List<Handler>>();

      List<Handler> timeHandlers = new LinkedList<Handler>();
      timeHandlers.add(new Handler(null, new TagPattern(RepeaterTime.class), new TagPattern(RepeaterDayPortion.class, true)));
      definitions.put(Handler.HandlerType.TIME, timeHandlers);

      List<Handler> dateHandlers = new LinkedList<Handler>();
      dateHandlers.add(new Handler(new RdnRmnSdTTzSyHandler(), new TagPattern(RepeaterDayName.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(RepeaterTime.class), new TagPattern(TimeZone.class), new TagPattern(ScalarYear.class)));
      // DIFF: We add an optional comma to MDY
      dateHandlers.add(new Handler(new RmnSdSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorComma.class, true), new TagPattern(ScalarYear.class)));
      dateHandlers.add(new Handler(new RmnSdSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorComma.class, true), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new RmnSdHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new RmnOdHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(OrdinalDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new RmnSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class)));
      dateHandlers.add(new Handler(new SdRmnSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new SmSdSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new SdSmSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new SySmSdHandler(), new TagPattern(ScalarYear.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      // DIFF: We make 05/06 interpret as month/day before month/year
      dateHandlers.add(new Handler(new SmSdHandler(), false, new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class)));
      dateHandlers.add(new Handler(new SmSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class)));
      definitions.put(Handler.HandlerType.DATE, dateHandlers);

      // tonight at 7pm
      List<Handler> anchorHandlers = new LinkedList<Handler>();
      anchorHandlers.add(new Handler(new RHandler(), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)));
      anchorHandlers.add(new Handler(new RHandler(), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)));
      anchorHandlers.add(new Handler(new RGRHandler(), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)));
      definitions.put(Handler.HandlerType.ANCHOR, anchorHandlers);

      // 3 weeks from now, in 2 months
      List<Handler> arrowHandlers = new LinkedList<Handler>();
      arrowHandlers.add(new Handler(new SRPHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class)));
      arrowHandlers.add(new Handler(new PSRHandler(), new TagPattern(Pointer.class), new TagPattern(Scalar.class), new TagPattern(Repeater.class)));
      arrowHandlers.add(new Handler(new SRPAHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class), new HandlerTypePattern(Handler.HandlerType.ANCHOR)));
      definitions.put(Handler.HandlerType.ARROW, arrowHandlers);

      // 3rd week in march
      List<Handler> narrowHandlers = new LinkedList<Handler>();
      narrowHandlers.add(new Handler(new ORSRHandler(), new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(SeparatorIn.class), new TagPattern(Repeater.class)));
      narrowHandlers.add(new Handler(new ORGRHandler(), new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)));
      definitions.put(Handler.HandlerType.NARROW, narrowHandlers);
      _definitions = definitions;
    }
    return _definitions;
  }

  public static Span tokensToSpan(List<Token> tokens, Options options) {
    if (options.isDebug()) {
      System.out.println("Chronic.tokensToSpan: " + tokens);
    }

    // maybe it's a specific date
    Map<Handler.HandlerType, List<Handler>> definitions = definitions();
    for (Handler handler : definitions.get(Handler.HandlerType.DATE)) {
      if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: date");
        }
        List<Token> goodTokens = new LinkedList<Token>();
        for (Token token : tokens) {
          if (token.getTag(Separator.class) == null) {
            goodTokens.add(token);
          }
        }
        return handler.getHandler().handle(goodTokens, options);
      }
    }

    // I guess it's not a specific date, maybe it's just an anchor
    for (Handler handler : definitions.get(Handler.HandlerType.ANCHOR)) {
      if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: anchor");
        }
        List<Token> goodTokens = new LinkedList<Token>();
        for (Token token : tokens) {
          if (token.getTag(Separator.class) == null) {
            goodTokens.add(token);
          }
        }
        return handler.getHandler().handle(goodTokens, options);
      }
    }

    // not an anchor, perhaps it's an arrow
    for (Handler handler : definitions.get(Handler.HandlerType.ARROW)) {
      if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: arrow");
        }
        List<Token> goodTokens = new LinkedList<Token>();
        for (Token token : tokens) {
          if (token.getTag(SeparatorAt.class) == null && token.getTag(SeparatorSlashOrDash.class) == null && token.getTag(SeparatorComma.class) == null) {
            goodTokens.add(token);
          }
        }
        return handler.getHandler().handle(goodTokens, options);
      }
    }

    // not an arrow, let's hope it's a narrow
    for (Handler handler : definitions.get(Handler.HandlerType.NARROW)) {
      if (handler.isCompatible(options) && handler.match(tokens, definitions)) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: narrow");
        }
        //List<Token> goodTokens = new LinkedList<Token>();
        //for (Token token : tokens) {
        //if (token.getTag(Separator.class) == null) {
        //  goodTokens.add(token);
        //}
        //}
        return handler.getHandler().handle(tokens, options);
      }
    }

    // I guess you're out of luck!
    if (options.isDebug()) {
      System.out.println("Chronic.tokensToSpan: none");
    }
    return null;
  }

  public static List<Repeater<?>> getRepeaters(List<Token> tokens) {
    List<Repeater<?>> repeaters = new LinkedList<Repeater<?>>();
    for (Token token : tokens) {
      Repeater<?> tag = token.getTag(Repeater.class);
      if (tag != null) {
        repeaters.add(tag);
      }
    }
    Collections.sort(repeaters);
    Collections.reverse(repeaters);
    return repeaters;
  }

  public static Span getAnchor(List<Token> tokens, Options options) {
    Grabber grabber = new Grabber(Grabber.Relative.THIS);
    Pointer.PointerType pointer = Pointer.PointerType.FUTURE;

    List<Repeater<?>> repeaters = getRepeaters(tokens);
    for (int i = 0; i < repeaters.size(); i++) {
      tokens.remove(tokens.size() - 1);
    }

    if (!tokens.isEmpty() && tokens.get(0).getTag(Grabber.class) != null) {
      grabber = tokens.get(0).getTag(Grabber.class);
      tokens.remove(tokens.size() - 1);
    }

    Repeater<?> head = repeaters.remove(0);
    head.setStart((Calendar) options.getNow().clone());

    Span outerSpan;
    Grabber.Relative grabberType = grabber.getType();
    if (grabberType == Grabber.Relative.LAST) {
      outerSpan = head.nextSpan(Pointer.PointerType.PAST);
    }
    else if (grabberType == Grabber.Relative.THIS) {
      if (repeaters.size() > 0) {
        outerSpan = head.thisSpan(PointerType.NONE);
      }
      else {
        outerSpan = head.thisSpan(options.getContext());
      }
    }
    else if (grabberType == Grabber.Relative.NEXT) {
      outerSpan = head.nextSpan(Pointer.PointerType.FUTURE);
    }
    else {
      throw new IllegalArgumentException("Invalid grabber type " + grabberType + ".");
    }

    if (options.isDebug()) {
      System.out.println("Chronic.getAnchor: outerSpan = " + outerSpan + "; repeaters = " + repeaters);
    }

    Span anchor = findWithin(repeaters, outerSpan, pointer, options);
    return anchor;
  }

  public static Span dayOrTime(Calendar dayStart, List<Token> timeTokens, Options options) {
    Span outerSpan = new Span(dayStart, Time.cloneAndAdd(dayStart, Calendar.DAY_OF_MONTH, 1));
    if (!timeTokens.isEmpty()) {

//      /** SUPER HACK MODE FOR TIMES **/
//      Tag<?> dayPortionTag = null;
//      Tag<?> timeTag = null;
//      for (Token token : timeTokens) {
//        Tag<?> tempDayPortionTag = token.getTag(RepeaterDayPortion.class);
//        if (tempDayPortionTag != null) {
//          dayPortionTag = tempDayPortionTag;
//        }
//
//        Tag<?> tempTimeTag = token.getTag(RepeaterTime.class);
//        if (tempTimeTag != null) {
//          timeTag = tempTimeTag;
//        }
//      }
//      
//      if (timeTag != null && dayPortionTag != null) {
//        Tick tick = (Tick)timeTag.getType();
//        RepeaterDayPortion.DayPortion dayPortion = (RepeaterDayPortion.DayPortion)dayPortionTag.getType();
//        if (tick.intValue() <= (RepeaterDay.DAY_SECONDS / 2)) {
//          if (dayPortion == RepeaterDayPortion.DayPortion.PM) {
//            if (tick.intValue() == (12 * 60 * 60)) {
//              Calendar exactTime = Time.cloneAndAdd(dayStart, Calendar.SECOND, tick.intValue());
//              return new Span(exactTime, exactTime);
//            }
//            Calendar exactTime = Time.cloneAndAdd(dayStart, Calendar.SECOND, tick.intValue() + RepeaterDay.DAY_SECONDS / 2);
//            return new Span(exactTime, exactTime);
//          }
//          else if (dayPortion == RepeaterDayPortion.DayPortion.AM) {
//            if (tick.intValue() == (12 * 60 * 60)) {
//              Calendar exactTime = dayStart;
//              return new Span(exactTime, exactTime);
//            }
//            Calendar exactTime = Time.cloneAndAdd(dayStart, Calendar.SECOND, tick.intValue());
//            return new Span(exactTime, exactTime);
//          }
//        }
//      }
//      /** SUPER HACK MODE FOR TIMES **/

      options.setNow(outerSpan.getBeginCalendar());
      Span time = getAnchor(dealiasAndDisambiguateTimes(timeTokens, options), options);
      return time;
    }
    return outerSpan;
  }

  /**
   * Recursively finds repeaters within other repeaters.
   * Returns a Span representing the innermost time span
   * or nil if no repeater union could be found
   */
  public static Span findWithin(List<Repeater<?>> tags, Span span, Pointer.PointerType pointer, Options options) {
    if (options.isDebug()) {
      System.out.println("Chronic.findWithin: " + tags + " in " + span);
    }
    if (tags.isEmpty()) {
      return span;
    }
    Repeater<?> head = tags.get(0);
    List<Repeater<?>> rest = (tags.size() > 1) ? tags.subList(1, tags.size()) : new LinkedList<Repeater<?>>();
    head.setStart((pointer == Pointer.PointerType.FUTURE) ? span.getBeginCalendar() : span.getEndCalendar());
    Span h = head.thisSpan(PointerType.NONE);

    if (span.contains(h.getBegin()) || span.contains(h.getEnd())) {
      return findWithin(rest, h, pointer, options);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static List<Token> dealiasAndDisambiguateTimes(List<Token> tokens, Options options) {
    // handle aliases of am/pm
    // 5:00 in the morning => 5:00 am
    // 7:00 in the evening => 7:00 pm

    int dayPortionIndex = -1;
    int tokenSize = tokens.size();
    for (int i = 0; dayPortionIndex == -1 && i < tokenSize; i++) {
      Token t = tokens.get(i);
      if (t.getTag(RepeaterDayPortion.class) != null) {
        dayPortionIndex = i;
      }
    }

    int timeIndex = -1;
    for (int i = 0; timeIndex == -1 && i < tokenSize; i++) {
      Token t = tokens.get(i);
      if (t.getTag(RepeaterTime.class) != null) {
        timeIndex = i;
      }
    }

    if (dayPortionIndex != -1 && timeIndex != -1) {
      Token t1 = tokens.get(dayPortionIndex);
      Tag<RepeaterDayPortion<?>> t1Tag = t1.getTag(RepeaterDayPortion.class);

      Object t1TagType = t1Tag.getType();
      if (RepeaterDayPortion.DayPortion.MORNING.equals(t1TagType)) {
        if (options.isDebug()) {
          System.out.println("Chronic.dealiasAndDisambiguateTimes: morning->am");
        }
        t1.untag(RepeaterDayPortion.class);
        t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AM));
      }
      else if (RepeaterDayPortion.DayPortion.AFTERNOON.equals(t1TagType) || RepeaterDayPortion.DayPortion.EVENING.equals(t1TagType) || RepeaterDayPortion.DayPortion.NIGHT.equals(t1TagType)) {
        if (options.isDebug()) {
          System.out.println("Chronic.dealiasAndDisambiguateTimes: " + t1TagType + "->pm");
        }
        t1.untag(RepeaterDayPortion.class);
        t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.PM));
      }
    }

//    int tokenSize = tokens.size();
//    for (int i = 0; i < tokenSize; i++) {
//      Token t0 = tokens.get(i);
//      if (i < tokenSize - 1) {
//        Token t1 = tokens.get(i + 1);
//        RepeaterDayPortion<?> t1Tag = t1.getTag(RepeaterDayPortion.class);
//        if (t1Tag != null && t0.getTag(RepeaterTime.class) != null) {
//          if (t1Tag.getType() == RepeaterDayPortion.DayPortion.MORNING) {
//            t1.untag(RepeaterDayPortion.class);
//            t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AM));
//          }
//          else if (t1Tag.getType() == RepeaterDayPortion.DayPortion.AFTERNOON || t1Tag.getType() == RepeaterDayPortion.DayPortion.EVENING || t1Tag.getType() == RepeaterDayPortion.DayPortion.NIGHT) {
//            t1.untag(RepeaterDayPortion.class);
//            t1.tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.PM));
//          }
//        }
//      }
//    }

    // handle ambiguous times if :ambiguous_time_range is specified
    if (options.getAmbiguousTimeRange() != 0) {
      List<Token> ttokens = new LinkedList<Token>();
      for (int i = 0; i < tokenSize; i++) {
        Token t0 = tokens.get(i);
        ttokens.add(t0);
        Token t1 = null;
        if (i < tokenSize - 1) {
          t1 = tokens.get(i + 1);
        }
        if (t0.getTag(RepeaterTime.class) != null && t0.getTag(RepeaterTime.class).getType().isAmbiguous() && (t1 == null || t1.getTag(RepeaterDayPortion.class) == null)) {
          Token distoken = new Token("disambiguator");
          distoken.tag(new IntegerRepeaterDayPortion(Integer.valueOf(options.getAmbiguousTimeRange())));
          ttokens.add(distoken);
        }
      }
      tokens = ttokens;
    }

    if (options.isDebug()) {
      System.out.println("Chronic.dealiasAndDisambiguateTimes: " + tokens);
    }

    return tokens;
  }
}
