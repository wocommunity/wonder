package com.mdimension.jchronic;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.mdimension.jchronic.handlers.DummyHandler;
import com.mdimension.jchronic.handlers.Handler;
import com.mdimension.jchronic.handlers.HandlerTypePattern;
import com.mdimension.jchronic.handlers.TagPattern;
import com.mdimension.jchronic.repeaters.EnumRepeaterDayPortion;
import com.mdimension.jchronic.repeaters.Repeater;
import com.mdimension.jchronic.repeaters.RepeaterDayName;
import com.mdimension.jchronic.repeaters.RepeaterDayPortion;
import com.mdimension.jchronic.repeaters.RepeaterMonthName;
import com.mdimension.jchronic.repeaters.RepeaterTime;
import com.mdimension.jchronic.repeaters.RepeaterYear;
import com.mdimension.jchronic.tags.Pointer;
import com.mdimension.jchronic.tags.Scalar;
import com.mdimension.jchronic.tags.ScalarDay;
import com.mdimension.jchronic.utils.Time;
import com.mdimension.jchronic.utils.Token;

public class HandlerTestCase extends TestCase {
  private Calendar _now;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _now = Time.construct(2006, 8, 16, 14, 0, 0, 0);
  }
  
  public void testHandlerClass1() {
    Handler handler = new Handler(new DummyHandler(), new TagPattern(Repeater.class));
    List<Token> tokens = new LinkedList<Token>();
    tokens.add(new Token("friday"));
    tokens.get(0).tag(new RepeaterDayName(RepeaterDayName.DayName.FRIDAY));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("afternoon"));
    tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AFTERNOON));
    
    assertFalse(handler.match(tokens, Handler.definitions()));
  }
  
  public void testHandlerClass2() {
    Handler handler = new Handler(new DummyHandler(), new TagPattern(Repeater.class), new TagPattern(Repeater.class, true));
    List<Token> tokens = new LinkedList<Token>();
    tokens.add(new Token("friday"));
    tokens.get(0).tag(new RepeaterDayName(RepeaterDayName.DayName.FRIDAY));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("afternoon"));
    tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AFTERNOON));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("afternoon"));
    tokens.get(2).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AFTERNOON));
    
    assertFalse(handler.match(tokens, Handler.definitions()));
  }

  public void testHandlerClass3() {
    Handler handler = new Handler(new DummyHandler(), new TagPattern(Repeater.class), new HandlerTypePattern(Handler.HandlerType.TIME, true));
    List<Token> tokens = new LinkedList<Token>();
    tokens.add(new Token("friday"));
    tokens.get(0).tag(new RepeaterDayName(RepeaterDayName.DayName.FRIDAY));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("afternoon"));
    tokens.get(1).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.AFTERNOON));
    
    assertFalse(handler.match(tokens, Handler.definitions()));
  }

  public void testHandlerClass4() {
    Handler handler = new Handler(new DummyHandler(), new TagPattern(RepeaterMonthName.class), new TagPattern(ScalarDay.class), new HandlerTypePattern(Handler.HandlerType.TIME, true));
    List<Token> tokens = new LinkedList<Token>();
    tokens.add(new Token("may"));
    tokens.get(0).tag(new RepeaterMonthName(RepeaterMonthName.MonthName.MAY));
    
    assertFalse(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("27"));
    tokens.get(1).tag(new ScalarDay(Integer.valueOf(27)));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
  }

  public void testHandlerClass5() {
    Handler handler = new Handler(new DummyHandler(), new TagPattern(Repeater.class), new HandlerTypePattern(Handler.HandlerType.TIME, true));
    List<Token> tokens = new LinkedList<Token>();
    tokens.add(new Token("friday"));
    tokens.get(0).tag(new RepeaterDayName(RepeaterDayName.DayName.FRIDAY));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("5:00"));
    tokens.get(1).tag(new RepeaterTime("5:00"));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
    
    tokens.add(new Token("pm"));
    tokens.get(2).tag(new EnumRepeaterDayPortion(RepeaterDayPortion.DayPortion.PM));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
  }

  public void testHandlerClass6() {
    Handler handler = new Handler(new DummyHandler(), new TagPattern(Scalar.class), new TagPattern(Repeater.class), new TagPattern(Pointer.class));
    List<Token> tokens = new LinkedList<Token>();
    tokens.add(new Token("3"));
    tokens.add(new Token("years"));
    tokens.add(new Token("past"));
    
    tokens.get(0).tag(new Scalar(Integer.valueOf(3)));
    tokens.get(1).tag(new RepeaterYear());
    tokens.get(2).tag(new Pointer(Pointer.PointerType.PAST));
    
    assertTrue(handler.match(tokens, Handler.definitions()));
  }

//    def test_constantize
//      handler = Chronic::Handler.new([], :handler)
//      assert_equal Chronic::RepeaterTime, handler.constantize(:repeater_time)
//    end
//    
//  end
}
