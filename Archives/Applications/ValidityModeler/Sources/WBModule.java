import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WBModule extends WOComponent {

    public WBModule(WOContext arg0) {
		super(arg0);
	}

	@Override
	public boolean synchronizesVariablesWithBindings(){
        return false;
    }

    public boolean hasAction(){
        if(this.hasBinding("pageName") || this.hasBinding("action")){
            return true;
        }
        return false;
    }

    public String filename(){
        return (String)this.valueForBinding("filename");
    }

    public String framework(){
        String work = (String)this.valueForBinding("framework");
        if(work.equals("app")){
            work = null;
        }
        return work;
    }

    public String tableWidth(){
        Object value = this.valueForBinding("tableWidth");
        if(value != null){
            return value.toString();
        }
        return null;
    }

    public Number borderWidth(){
        Number n = (Number)this.valueForBinding("borderWidth");
        if(n == null){
            n = Integer.valueOf(2);
        }
        return n;
    }

    public String borderColor(){
        String color = (String)this.valueForBinding("borderColor");
        if(color == null){
            color = "#000000";
        }
        return color;
    }

    public String contentBGColor(){
        String color = (String)this.valueForBinding("contentBGColor");
        if(color == null){
            color = "#FFFFFF";
        }
        return color;
    }

    public boolean showContent(){
        if(this.hasBinding("showContent")){
            return ((Boolean)this.valueForBinding("showContent")).booleanValue();
        }
        return true;
    }

    public boolean showHeader(){
        if(this.hasBinding("showHeader")){
            return ((Boolean)this.valueForBinding("showHeader")).booleanValue();
        }
        return false;
    }

    public String view(){
        String str = null;
        if(this.hasBinding("view")){
            str = (String)this.valueForBinding("view");
        } else {
            str = "NORTH";
        }
        return str;
    }

    public WOComponent action() {
        WOComponent aComponent = null;
        if(this.hasBinding("pageName")){
            String aPageName = (String)this.valueForBinding("pageName");
            aComponent = this.pageWithName(aPageName);
        } else {
            aComponent = (WOComponent)this.valueForBinding("action");
        }
        return aComponent;
    }

    public boolean isNorthView(){
        if(!this.isWestView()){
            return true;
        }
        return false;
    }

    public boolean isWestView(){
        if(this.view().toUpperCase().equals("WEST")){
            return true;
        }
        return false;
    }
 
}
