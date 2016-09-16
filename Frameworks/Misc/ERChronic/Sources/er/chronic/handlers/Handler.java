package er.chronic.handlers;

import java.util.Arrays;
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
import er.chronic.tags.Pointer.PointerType;
import er.chronic.tags.Scalar;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarMonth;
import er.chronic.tags.ScalarYear;
import er.chronic.tags.Separator;
import er.chronic.tags.SeparatorAt;
import er.chronic.tags.SeparatorComma;
import er.chronic.tags.SeparatorIn;
import er.chronic.tags.SeparatorOn;
import er.chronic.tags.SeparatorSlashOrDash;
import er.chronic.tags.Tag;
import er.chronic.tags.TimeZone;
import er.chronic.utils.EndianPrecedence;
import er.chronic.utils.Range;
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
    return matchCount(tokens, definitions) != null;
  }
  
  public Range matchCount(List<Token> tokens, Map<Handler.HandlerType, List<Handler>> definitions) {
    Range range = _matchCount(tokens, definitions);
    return (range != null && range.getEnd() == tokens.size()) ? range : null;
  }
  
  // MS: Switching to count offsets allows us to have variable length times at the front instead of
  // the end ... for instance, 5pm 2/10 instead of 2/10 5pm
  public Range _matchCount(List<Token> tokens, Map<Handler.HandlerType, List<Handler>> definitions) {
    int tokenStartIndex = 0;
    int tokenIndex = 0;
    for (HandlerPattern pattern : _patterns) {
      //System.out.println("Handler.match:   pattern = " + pattern + " (tokenIndex = " + tokenIndex + ", " + tokens.get(tokenIndex) + ")");
      boolean optional = pattern.isOptional();
      if (pattern instanceof TagPattern) {
        boolean match = (tokenIndex < tokens.size() && tokens.get(tokenIndex).getTags(((TagPattern) pattern).getTagClass()).size() > 0);
        // System.out.println("Handler.match:   " + pattern + "=" + match);
        if (!match && !optional) {
          return null;
          //return false;
        }
        if (match) {
          tokenIndex++;
        }
        // next if !match && optional ?
      }
      else if (pattern instanceof HandlerTypePattern) {
        if (optional && tokenIndex == tokens.size()) {
          return new Range(0, tokens.size());
        }
        List<Handler> subHandlers = definitions.get(((HandlerTypePattern) pattern).getType());
        for (Handler subHandler : subHandlers) {
          Range range = subHandler._matchCount(tokens.subList(tokenIndex, tokens.size()), definitions);
          if (range != null) {
            if (tokenIndex == 0) {
              tokenStartIndex = (int)range.getEnd();
            }
            tokenIndex += range.getEnd();
          }
        }
        //return false;
      }
      else {
        throw new IllegalArgumentException("Unknown pattern: " + pattern);
      }
    }
    //if (tokenIndex != tokens.size()) {
      //return -1;
      //return false;
    //}
    return new Range(tokenStartIndex, tokenIndex);
  }

  @Override
  public String toString() {
    return "[Handler: " + _handler + "]";
  }

  public static synchronized Map<Handler.HandlerType, List<Handler>> definitions(Options options) {
    Map<Handler.HandlerType, List<Handler>> definitions = _definitions;
    
    // MS: technically we can't cache any longer because endian precendence mucks with the settings ...
    List<EndianPrecedence> defaultEndianPrecedences = Arrays.asList(EndianPrecedence.Middle, EndianPrecedence.Little);
    if (definitions == null || (options.getEndianPrecedence() != null && !options.getEndianPrecedence().equals(defaultEndianPrecedences))) {
      List<EndianPrecedence> endianPrecendence = options.getEndianPrecedence();
      if (endianPrecendence == null) {
        endianPrecendence = defaultEndianPrecedences;
      }

      // ensure the endian precedence is exactly two elements long
      if (endianPrecendence.size() != 2) {
        throw new IllegalArgumentException("More than two elements specified for endian precedence array: " + endianPrecendence + ".");
      }

      Map<EndianPrecedence, Handler> endianHandlers = new HashMap<>();
      // handler for dd/mm/yyyy
      endianHandlers.put(EndianPrecedence.Little, new Handler(new SdSmSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));

      // handler for mm/dd/yyyy
      endianHandlers.put(EndianPrecedence.Middle, new Handler(new SmSdSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));

      // ensure we have valid endian values
      // MS: generics guarantee this for us 

      definitions = new HashMap<Handler.HandlerType, List<Handler>>();

      List<Handler> timeHandlers = new LinkedList<>();
      timeHandlers.add(new Handler(null, new TagPattern(RepeaterTime.class), new TagPattern(Grabber.class, true), new TagPattern(RepeaterDayPortion.class, true)));
      definitions.put(Handler.HandlerType.TIME, timeHandlers);

      List<Handler> dateHandlers = new LinkedList<>();
      dateHandlers.add(new Handler(new RdnRmnSdTTzSyHandler(), new TagPattern(RepeaterDayName.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(RepeaterTime.class), new TagPattern(SeparatorSlashOrDash.class, true), new TagPattern(TimeZone.class), new TagPattern(ScalarYear.class)));
      // DIFF: We add scalar year as a standalone match
      dateHandlers.add(new Handler(new SyHandler(), new TagPattern(ScalarYear.class)));
      // DIFF: We add an optional comma to MDY
      dateHandlers.add(new Handler(new RmnSdSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorComma.class, true), new TagPattern(ScalarYear.class)));
      dateHandlers.add(new Handler(new RmnSdSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorComma.class, true), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new RmnSdHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      for (EndianPrecedence precedence : endianPrecendence) {
        dateHandlers.add(endianHandlers.get(precedence));
      }
      dateHandlers.add(new Handler(new RmnSdOnHandler(), new TagPattern(RepeaterTime.class), new TagPattern(RepeaterDayPortion.class, true), new TagPattern(SeparatorOn.class, true), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class)));
      dateHandlers.add(new Handler(new RmnOdHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(OrdinalDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new RmnOdOnHandler(), new TagPattern(RepeaterTime.class), new TagPattern(RepeaterDayPortion.class, true), new TagPattern(SeparatorOn.class, true), new TagPattern(RepeaterMonthName.class), new TagPattern(OrdinalDay.class)));
      dateHandlers.add(new Handler(new RmnSyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class)));
      dateHandlers.add(new Handler(new SdRmnSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new SmSdSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new SdSmSyHandler(), new TagPattern(ScalarDay.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      dateHandlers.add(new Handler(new SySmSdHandler(), new TagPattern(ScalarYear.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      // DIFF: We make 05/06 interpret as month/day before month/year
      dateHandlers.add(new Handler(new SmSdHandler(), false, new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class), new TagPattern(SeparatorAt.class, true), new HandlerTypePattern(Handler.HandlerType.TIME, true)));
      // DIFF: Also, we support a leading time -- for 5pm 2/10
      dateHandlers.add(new Handler(new SmSdHandler(), false, new HandlerTypePattern(Handler.HandlerType.TIME, true), new TagPattern(SeparatorOn.class, true), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarDay.class)));
      dateHandlers.add(new Handler(new SmSyHandler(), new TagPattern(ScalarMonth.class), new TagPattern(SeparatorSlashOrDash.class), new TagPattern(ScalarYear.class)));
      definitions.put(Handler.HandlerType.DATE, dateHandlers);

      // tonight at 7pm
      List<Handler> anchorHandlers = new LinkedList<>();
      anchorHandlers.add(new Handler(new RHandler(), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)));
      // DIFF: Add support for "next" and "last" grabbers
      anchorHandlers.add(new Handler(new RHandler(), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class), new TagPattern(Repeater.class), new TagPattern(SeparatorAt.class, true), new TagPattern(Grabber.class, true), new TagPattern(Repeater.class, true), new TagPattern(Repeater.class, true)));
      anchorHandlers.add(new Handler(new RGRHandler(), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)));
      definitions.put(Handler.HandlerType.ANCHOR, anchorHandlers);

      // 3 weeks from now, in 2 months
      List<Handler> arrowHandlers = new LinkedList<>();
      arrowHandlers.add(new Handler(new SRPHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class)));
      arrowHandlers.add(new Handler(new PSRHandler(), new TagPattern(Pointer.class), new TagPattern(Scalar.class), new TagPattern(Repeater.class)));
      arrowHandlers.add(new Handler(new SRPAHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class), new HandlerTypePattern(Handler.HandlerType.ANCHOR)));
      definitions.put(Handler.HandlerType.ARROW, arrowHandlers);

      // 3rd week in march
      List<Handler> narrowHandlers = new LinkedList<>();
      narrowHandlers.add(new Handler(new ORSRHandler(), new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(SeparatorIn.class), new TagPattern(Repeater.class)));
      narrowHandlers.add(new Handler(new ORGRHandler(), new TagPattern(Ordinal.class), new TagPattern(Repeater.class), new TagPattern(Grabber.class), new TagPattern(Repeater.class)));
      definitions.put(Handler.HandlerType.NARROW, narrowHandlers);
      
      if (_definitions == null) {
        _definitions = definitions;
      }
    }
    return definitions;
  }

  public static Span tokensToSpan(List<Token> tokens, Options options) {
    if (options.isDebug()) {
      System.out.println("Chronic.tokensToSpan: " + tokens);
    }

    Range range = null;
    // maybe it's a specific date
    Map<Handler.HandlerType, List<Handler>> definitions = Handler.definitions(options);
    for (Handler handler : definitions.get(Handler.HandlerType.DATE)) {
      if (handler.isCompatible(options) && (range = handler.matchCount(tokens, definitions)) != null) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: date " + handler);
        }
        List<Token> goodTokens = new LinkedList<>();
        for (Token token : range.subList(tokens)) {
          if (token.getTag(Separator.class) == null) {
            goodTokens.add(token);
          }
        }
        return handler.getHandler().handle(goodTokens, options);
      }
    }

    // I guess it's not a specific date, maybe it's just an anchor
    for (Handler handler : definitions.get(Handler.HandlerType.ANCHOR)) {
      if (handler.isCompatible(options) && (range = handler.matchCount(tokens, definitions)) != null) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: anchor " + handler);
        }
        List<Token> goodTokens = new LinkedList<>();
        for (Token token : range.subList(tokens)) {
          if (token.getTag(Separator.class) == null) {
            goodTokens.add(token);
          }
        }
        return handler.getHandler().handle(goodTokens, options);
      }
    }

    // not an anchor, perhaps it's an arrow
    for (Handler handler : definitions.get(Handler.HandlerType.ARROW)) {
      if (handler.isCompatible(options) && (range = handler.matchCount(tokens, definitions)) != null) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: arrow " + handler);
        }
        List<Token> goodTokens = new LinkedList<>();
        for (Token token : range.subList(tokens)) {
          if (token.getTag(SeparatorAt.class) == null && token.getTag(SeparatorSlashOrDash.class) == null && token.getTag(SeparatorComma.class) == null) {
            goodTokens.add(token);
          }
        }
        return handler.getHandler().handle(goodTokens, options);
      }
    }

    // not an arrow, let's hope it's a narrow
    for (Handler handler : definitions.get(Handler.HandlerType.NARROW)) {
      if (handler.isCompatible(options) && (range = handler.matchCount(tokens, definitions)) != null) {
        if (options.isDebug()) {
          System.out.println("Chronic.tokensToSpan: narrow " + handler);
        }
        //List<Token> goodTokens = new LinkedList<>();
        //for (Token token : tokens) {
        //if (token.getTag(Separator.class) == null) {
        //  goodTokens.add(token);
        //}
        //}
        return handler.getHandler().handle(range.subList(tokens), options);
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
    Grabber grabber;
    // DIFF: If your pointer is PAST, you want "last Monday" not "this Monday"
    Pointer.PointerType pointer;
    if (options.getContext() == PointerType.PAST) {
      grabber = new Grabber(Grabber.Relative.LAST);
    }
    else {
      grabber = new Grabber(Grabber.Relative.THIS);
    }
    pointer = Pointer.PointerType.FUTURE;

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
      System.out.println("Chronic.getAnchor: grabber = " + grabber + "; repeaters = " + repeaters + "; outerSpan = " + outerSpan);
    }

    Span anchor = findWithin(repeaters, outerSpan, pointer, options);
    return anchor;
  }
  
  // MS: Not necessary because we changed the impl to be more javaish
  //def apply_endian_precedences(precedences)
  //def endian_variable_name_for(e)
  //def swap(arr, a, b); arr[a], arr[b] = arr[b], arr[a]; end

  public static Span parseTime(List<Token> tokens, int timeTokenOffset, int year, int month, int day, Options options) {
    // MS: properly parse time in this format
    Span span;
    try {
      List<Token> timeTokens = tokens.subList(timeTokenOffset, tokens.size());
      Calendar dayStart = Time.construct(year, month, day);
      span = Handler.dayOrTime(dayStart, timeTokens, options);
    }
    catch (IllegalArgumentException e) {
      if (options.isDebug()) {
        e.printStackTrace(System.out);
      }
      span = null;
    }
    return span;
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

      // DIFF: If you're generating past dates, you want to use the end of the day as the offset so "5pm" ends up on the same day as the date you chose
      if (options.getContext() == PointerType.PAST) {
        options.setNow(outerSpan.getEndCalendar());
      }
      else {
        options.setNow(outerSpan.getBeginCalendar());
      }
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
      List<Token> ttokens = new LinkedList<>();
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
