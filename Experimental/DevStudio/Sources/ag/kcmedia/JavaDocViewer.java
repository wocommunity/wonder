package ag.kcmedia;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;
import ag.kcmedia.Jode.*;
import org.w3c.tidy.*;
import java.io.*;

/**
 * Class for Component JavaDocViewer.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Tue Aug 27 2002
 * @project DevStudio
 */

public class JavaDocViewer extends WOComponent {

    public String className;
    public ClassProxy selectedClass;
    public TagTable tt;
    /** logging support */
    private static final ERXLogger log = ERXLogger.getERXLogger(JavaDocViewer.class,"components");
	
    /**
     * Public constructor.
     * @param context the context
     */
    public JavaDocViewer(WOContext context) {
        super(context);
    }

    

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public Node node;

    public NSMutableArray comments;

    public Node parse(String filename)
    {
        Node document = null;

        try {
            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            TagTable tt = new TagTable();
            tt.setConfiguration(tidy.getConfiguration());
            log.info(filename);
            document = tidy.parse(new FileInputStream(filename), null);
            document = document.findBody(tt);
            comments = new NSMutableArray();
            Node next = document.getContent();
            while(next != null) {
                Node content = next.getContent();
                int type = next.getType();

                if(type == Node.CommentTag ) {
                    //comments.addObject(next.getText());
                } else if(next.getType() == Node.StartTag && next.getElement().equals("p")) {
                    comments.addObject("<br>-----" + next.getContent());
                } else if(next.getContent() != null && next.getContent().getType() == Node.CommentTag) {
                    //comments.addObject(next.getContent());
                } else if(next.getContent() != null && next.getContent().getType() == Node.CommentTag) {
                    //comments.addObject(next.getContent());
                } else if(next.getContent() != null && next.getContent().getContent() != null && next.getContent().getContent().getType() == Node.CommentTag) {
                    //comments.addObject(next.getContent().getContent());
                } else if(next.getContent() != null && next.getContent().getContent() != null && next.getContent().getContent().getContent() != null && next.getContent().getContent().getContent().getType() == Node.CommentTag) {
                    //comments.addObject(next.getContent().getContent().getContent());
                }
                next = next.getNext();
            }
            //EOQualifier q = EOQualifier.qualifierWithQualifierFormat("content = null and type=2");
            
        } catch (FileNotFoundException fnfe) {
            log.error("File not found", fnfe);
        }
        catch (IOException e) {
            log.error("General IOExceptino", e);
        }

        return document;
    }
    
    public void setClassName(String value) {
        selectedClass = Jode.classProxyForName(value);
        className = value;
        node = parse(selectedClass.documentationPath());
    }
    
}
