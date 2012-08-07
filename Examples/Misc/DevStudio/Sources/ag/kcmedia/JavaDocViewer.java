package ag.kcmedia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.w3c.tidy.Node;
import org.w3c.tidy.TagTable;
import org.w3c.tidy.Tidy;

import ag.kcmedia.Jode.ClassProxy;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSMutableArray;

/**
 * Class for Component JavaDocViewer.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Tue Aug 27 2002
 */
public class JavaDocViewer extends WOComponent {

    public String className;
    public ClassProxy selectedClass;
    public TagTable tt;
    /** logging support */
    private static final Logger log = Logger.getLogger(JavaDocViewer.class);
	
    /**
     * Public constructor.
     * @param context the context
     */
    public JavaDocViewer(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    @Override
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
