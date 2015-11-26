package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class FormEntries extends SampleComponentBase {

  private static final long serialVersionUID = 1L;

  //********************************************************************
  //  Constructor : コンストラクタ
  //********************************************************************

  public FormEntries(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods : メソッド
  //********************************************************************

  public String _testString = null;
  public String _testPassword = null;
  public String _testNumber = null;
  public String _testMail = null;
  public String _testTel = null;
  public String _testUrl = null;
  public String _testDate = null;
  public String _testTime = null;
  public String _testMonth = null;
  public String _testDT = null;
  public String _testDTL = null;
  public String _testWeek = null;
  public String _testColor = null;
  public String _testText = null;
  public String _testRange = null;
  public String _testFlip = "0";
  public String _testsearch = null;

  public void setTestCheckbox(Boolean testCheckbox) {
    this.testCheckbox = testCheckbox;
  }
  public Boolean testCheckbox() {
    return testCheckbox;
  }
  public Boolean testCheckbox = Boolean.FALSE;

  //********************************************************************
  //  Actions : アクション
  //********************************************************************

  public WOActionResults doTestAction() {
    System.err.println("**doTestAction**");
    System.err.println(" testString = " + _testString);
    System.err.println(" testPassword = " + _testPassword);
    System.err.println(" testNumber = " + _testNumber);
    System.err.println(" testMail = " + _testMail);
    System.err.println(" testTel = " + _testTel);
    System.err.println(" testUrl = " + _testUrl);
    System.err.println(" testDate = " + _testDate);
    System.err.println(" testTime = " + _testTime);
    System.err.println(" testMonth = " + _testMonth);
    System.err.println(" testDT = " + _testDT);
    System.err.println(" testDTL = " + _testDTL);
    System.err.println(" testWeek = " + _testWeek);
    System.err.println(" testColor = " + _testColor);
    System.err.println(" testText = " + _testText);
    System.err.println(" testRange = " + _testRange);
    System.err.println(" testFlip = " + _testFlip);
    System.err.println(" testsearch = " + _testsearch);
    System.err.println(" testCheckbox = " + testCheckbox());

    return null;
  }

}
