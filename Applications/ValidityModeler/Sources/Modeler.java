import com.gammastream.validity.GSVAttribute;
import com.gammastream.validity.GSVEOAttribute;
import com.gammastream.validity.GSVEOEntity;
import com.gammastream.validity.GSVEntity;
import com.gammastream.validity.GSVModel;
import com.gammastream.validity.GSVRule;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class Modeler extends WOComponent {

    protected Application app = (Application)WOApplication.application();
    protected Session session = (Session)session();
    protected WBTruncateFormatter formatte25 = new WBTruncateFormatter(25,"...");
    protected GSVEOEntity currentEntity = null;
    protected GSVEOEntity selectedEntity = null;
    protected GSVEOAttribute currentAttribute = null;
    protected GSVEOAttribute selectedAttribute = null;
    protected GSVRule currentRule = null;
    protected GSVRule selectedRule = null;
    protected GSVRule currentSavedRule = null;
    protected GSVRule selectedSaveRule = null;
    protected NSMutableArray keyValueParameters = new NSMutableArray();
    protected KeyValue currentParameter = null;
    protected boolean showEntity = false;
    protected boolean showAttribute = false;
    protected boolean showInspector = false;
    protected boolean newFlag = false;
    protected boolean error = false;
    protected boolean checked = false;
    protected String errorMessage = "";

    
    public Modeler(WOContext arg0) {
		super(arg0);
	}

	@Override
	public void awake(){
        super.awake();
        error=false;
        checked=false;
    }

    public WOComponent showEntityAction() {
        //this.setSelectedEntity(currentEntity);
        if(selectedEntity==null){
            showAttribute = false;
            showEntity = false;
            showInspector = false;
            return null;
        }
        showAttribute = false;
        showEntity = true;
        showInspector = false;

        return null;
    }

    public WOComponent showAttributeAction() {
        this.setSelectedAttribute(currentAttribute);
        showAttribute = true;
        showEntity = false;
        showInspector = false;
        return null;
    }

    public WOComponent showAttributeActionBack() {
        showAttribute = true;
        showEntity = false;
        showInspector = false;
        return null;
    }


    public WOComponent showInspectorAction() {
        this.setSelectedRule(currentRule);
        showAttribute = false;
        showEntity = false;
        showInspector = true;
        keyValueParameters = new NSMutableArray();
        this.convertRuleDictionaryToKeyValue();
        return null;
    }

    public WOComponent newRuleAction() {
        if(selectedSaveRule==null){
            this.setSelectedRule(new GSVRule("Untitled",null,null,null,null));
            keyValueParameters = new NSMutableArray();
        }else{
            this.setSelectedRule(this.newRule(selectedSaveRule));
            keyValueParameters = new NSMutableArray();
            this.convertRuleDictionaryToKeyValue();
        }
        selectedSaveRule=null;
        showAttribute = false;
        showEntity = false;
        showInspector = true;
        newFlag = true;
        return null;
    }
    
    public WOComponent newKeyValueAction() {
        keyValueParameters.addObject(new KeyValue("NewKey","NewValue"));
        return null;
    }
    public WOComponent deleteKeyValueAction() {
        keyValueParameters.removeObject(currentParameter);
        return null;
    }


    public WOComponent deleteRuleInList() {
        String entityName = this.selectedAttribute().entity().name();
        GSVModel model = session.model();
        GSVEntity entity = model.entityNamed(entityName);
        if(entity==null){
            entity = new GSVEntity(model, entityName);
            model.addEntity(entity);
        }
        GSVAttribute att = entity.attributeNamed(this.selectedAttribute().name());
        if(att==null){
            att = new  GSVAttribute(entity, this.selectedAttribute().name());
            entity.addAttribute(att);
        }
        att.removeRule(currentRule);
        model.saveModel();

        showAttribute = true;
        showEntity = false;
        showInspector = false;
        newFlag = false;
        return null;
    }

    
    public WOComponent deleteRule() {
        String entityName = this.selectedAttribute().entity().name();
        GSVModel model = session.model();
        GSVEntity entity = model.entityNamed(entityName);
        if(entity==null){
            entity = new GSVEntity(model, entityName);
            model.addEntity(entity);
        }
        GSVAttribute att = entity.attributeNamed(this.selectedAttribute().name());
        if(att==null){
            att = new  GSVAttribute(entity, this.selectedAttribute().name());
            entity.addAttribute(att);
        }
        att.removeRule(this.selectedRule());
        model.saveModel();

        showAttribute = true;
        showEntity = false;
        showInspector = false;
        newFlag = false;
        return null;
    }

    public WOComponent saveAction() {
        error=false;
        if(this.selectedRule().ruleName()==null){
            error=true;
            errorMessage="Rule Name can not be left blank.";
            return null;
        }
        
            String entityName = this.selectedAttribute().entity().name();
            GSVModel model = session.model();
            GSVEntity entity = model.entityNamed(entityName);
            if(entity==null){
                entity = new GSVEntity(model, entityName);
                model.addEntity(entity);
            }
            entity.setModel(model);
            GSVAttribute att = entity.attributeNamed(this.selectedAttribute().name());
            if(att==null){
                att = new  GSVAttribute(entity, this.selectedAttribute().name());
                entity.addAttribute(att);
            }
            att.setEntity(entity);
            if(newFlag){
                if(att.ruleNamed(selectedRule().ruleName())!=null){
                    errorMessage = "There is already a rule named: "+ selectedRule().ruleName();
                    error=true;
                    return null;
                }
            }else{
                NSMutableArray temp = new NSMutableArray(att.rules());
                if(temp.count()!=0)
                    temp.removeObject(selectedRule());
                GSVRule ru=null;
                for(int s=0;s<temp.count();s++){
                    ru = (GSVRule)temp.objectAtIndex(s);
                    if(ru.ruleName().equals(selectedRule().ruleName())){
                        errorMessage = "There is already a rule named: "+ selectedRule().ruleName();
                        error=true;
                        return null;
                    }
                }
            }
            if(!this.convertRuleKeyValueToDictionary()){
                errorMessage = "All Keys in the Parameter NSDictionary can not contain spaces.<BR>Keys and Values can not be blank.";
                error=true;
                return null;
            }
            if(checked){
                app.config().addRule(selectedRule());
            }else{
                app.config().removeRule(selectedRule());
            }
            if(newFlag)
                att.addRule(this.selectedRule());
            model.saveModel();
            app.saveConfiguration();
            showAttribute = true;
            showEntity = false;
            showInspector = false;
            newFlag = false;
            selectedRule=null;
        return null;
    }

    //returns list of Rules for attribute 
    public NSArray rules() {
        try{
            GSVEntity entity = session.model().entityNamed(this.selectedAttribute().entity().name());
            GSVAttribute att = entity.attributeNamed(this.selectedAttribute().name());
            return att.rules();
        }catch(Exception e){
            return null;
        }
    }

    public boolean hasRules(){
        if(rules()!=null){
            if(rules().count()==0)
                return false;
            return true;
        }
        return false;
    }

    
    //Selected Entity is grey
    public String currentEntityBGColor() {
        return ( currentEntity == this.selectedEntity() ) ? "#DDDDDD" : "#FFFFFF";
    }
    
    public int ruleCount(){
        String entityName = this.currentAttribute.entity().name();
        GSVModel model = session.model();
        GSVEntity entity = model.entityNamed(entityName);
        if(entity==null){
            return 0;
        }
        GSVAttribute att = entity.attributeNamed(this.currentAttribute.name());
        if(att==null){
            return 0;
        }
        return att.rules().count();
    }

    public GSVRule newRule(GSVRule copyRule){
        GSVRule rule = new GSVRule(copyRule.ruleName(),copyRule.cName(),copyRule.mName(),"","");
        rule.setParameters(copyRule.parameters());
        rule.setOnSave(copyRule.onSave());
        rule.setOnInsert(copyRule.onInsert());
        rule.setOnDelete(copyRule.onDelete());
        return rule;
    }
    



    //Convertions
    public void convertRuleDictionaryToKeyValue(){
        NSMutableDictionary dict = new NSMutableDictionary();
        dict = selectedRule.parameters();
        String k,v;
        KeyValue temp;
        for(int i = 0; i<dict.allKeys().count();i++){
            k = (String)dict.allKeys().objectAtIndex(i);
            v = (String)dict.objectForKey(k);
            temp  = new KeyValue(k,v);
            keyValueParameters.addObject(temp);
        }
    }
    
    public boolean convertRuleKeyValueToDictionary(){
        NSMutableDictionary dict = new NSMutableDictionary();
        KeyValue temp;
        for(int i = 0; i<keyValueParameters.count();i++){
            temp  = (KeyValue)keyValueParameters.objectAtIndex(i);
            if(temp.key()!=null && temp.val()!=null){
                if(temp.key().indexOf(" ")==-1)
                    dict.setObjectForKey(temp.val(),temp.key());
                else{
                    return false;   
                }
            }else{
                return false;
            }
        }
        selectedRule.setParameters(dict);
        return true;
    }

    
//Attribute properties
    public boolean showKey(){
        try{
            return this.selectedEntity().primaryKeyAttributes().containsObject(currentAttribute.name());
        }catch(Exception e){
            return false;
        }
    }
    public boolean showInclude() {
        try{
            return this.selectedEntity().classProperties().containsObject(currentAttribute.name());
        }catch(Exception e){
            return false;
        }
    }
    public boolean showLock() {
        try{
            return this.selectedEntity().attributesUsedForLocking().containsObject(currentAttribute.name());
        }catch(Exception e){
            return false;
        }
    }
    public boolean showNull() {
        try{
            return (currentAttribute.allowsNull().equals("Y"));
        }catch(Exception e){
            return false;
        }
    }
    public String widthStr() {
        try{
            return (currentAttribute.width().intValue() > 0) ? ""+currentAttribute.width().intValue():"&nbsp; ";
        }catch(Exception e){
            return "&nbsp; ";
        }
    }

    //Get and Set Methods
    
    public GSVEOEntity selectedEntity(){
        return selectedEntity;
    }
    
    public void setSelectedEntity(GSVEOEntity newSelectedEntity){
        selectedEntity = newSelectedEntity;
    }
    
    public GSVEOAttribute selectedAttribute(){
        return selectedAttribute;
    }
    
    public void setSelectedAttribute(GSVEOAttribute newSelectedAttribute){
        selectedAttribute = newSelectedAttribute;
    }
    
    public GSVRule selectedRule(){
        return selectedRule;
    }
    
    public void setSelectedRule(GSVRule newSelectedRule){
        selectedRule = newSelectedRule;
    }
    
    public NSMutableArray keyValueParameters(){
        return keyValueParameters;
    }
    
    public GSVRule currentSavedRule(){
        return currentSavedRule;
    }
    
    public void setCurrentSavedRule(GSVRule newCurrentSavedRule){
        currentSavedRule = newCurrentSavedRule;
    }
    
    public String currentSavedRuleDisplayString() {
        int lastindex;
        String temp = currentSavedRule().cName();

        if(temp==null)
            temp="Undefined";
        lastindex = temp.lastIndexOf(".")+1;
        temp = temp.substring(lastindex,temp.length());
        return currentSavedRule().ruleName()+" - "+temp+"."+currentSavedRule().mName()+"()";
    }
    
    public GSVRule selectedSaveRule(){
        return selectedSaveRule;
    }
    
    public void setSelectedSaveRule(GSVRule newSelectedSaveRule){
        selectedSaveRule = newSelectedSaveRule;
    }
    
    public boolean checked(){
        return app.config().quickRules().containsObject(selectedRule);
    }
    
    public void setChecked(boolean d){
        checked = d;
    }
    
    public WOComponent promoteRule(){
        NSMutableArray rules = (NSMutableArray)this.rules();
        
        int index = rules.indexOfObject(currentRule);
        if( index == rules.count()-1 ){
            //last object
            rules.removeObjectAtIndex(index);
            rules.insertObjectAtIndex(currentRule, 0);
        } else {
            rules.removeObjectAtIndex(index);
            rules.insertObjectAtIndex(currentRule, index+1);
        }
        
        GSVModel model = session.model();
        model.saveModel();
        return null;
    }
    
    public WOComponent demoteRule(){
        NSMutableArray rules = (NSMutableArray)this.rules();
        
        int index = rules.indexOfObject(currentRule);
        if( index == 0 ){
            //last object
            rules.removeObjectAtIndex(index);
            rules.addObject(currentRule);
        } else {
            rules.removeObjectAtIndex(index);
            rules.insertObjectAtIndex(currentRule, index-1);
        }
        
        GSVModel model = session.model();
        model.saveModel();
        return null;
    }


}
