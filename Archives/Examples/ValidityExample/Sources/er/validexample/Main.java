package er.validexample;
//
// Main.java: Class file for WO Component 'Main'
// Project ValidityExample
//
// Created by msacket on Mon Jun 11 2001
//
 
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class Main extends WOComponent {

     public Main(WOContext context) {
        super(context);
    }
    
    public AllTogetherPage goToAllTogetherPage(){
        AllTogetherPage nextPage = (AllTogetherPage)pageWithName("AllTogetherPage");
        return nextPage;
    }

    public SeparatePage goToSeparatePage(){
        SeparatePage nextPage = (SeparatePage)pageWithName("SeparatePage");
        return nextPage;
    }

}
