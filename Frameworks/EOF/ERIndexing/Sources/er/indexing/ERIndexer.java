package er.indexing;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXFetchSpecificationBatchIterator;

/**
 * Utility class to reindex all objects of a model group, a model or an entity.
 * @author ak
 *
 */
public class ERIndexer {
    
    private NSArray<ERAutoIndex> _indices;
    
    private static final Logger log = LoggerFactory.getLogger(ERIndexer.class);

    public ERIndexer(NSArray<ERAutoIndex> indices) {
        _indices = indices;
    }

    public void clear() {
        for(Enumeration i = _indices.objectEnumerator(); i.hasMoreElements(); ) {
            ERIndex index = (ERIndex) i.nextElement();
            index.clear();
        }
    }

    public NSArray indicesForEntity(String entityName) {
        NSMutableArray result = new NSMutableArray();
        for(Enumeration i = _indices.objectEnumerator(); i.hasMoreElements(); ) {
            ERAutoIndex index = (ERAutoIndex) i.nextElement();
            if(index.handlesEntity(entityName)) {
                result.addObject(index);
            }
        }
        return result;
    } 
    public void indexAllObjects(EOEntity entity) {
        NSArray<ERIndex> incides = indicesForEntity(entity.name());
        if(incides.count() > 0) {
            long start = System.currentTimeMillis();
            int treshhold = 10;
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                EOFetchSpecification fs = new EOFetchSpecification(entity.name(), null, null);
                ERXFetchSpecificationBatchIterator iterator = new ERXFetchSpecificationBatchIterator(fs);
                iterator.setEditingContext(ec);
                while(iterator.hasNextBatch()) {
                    NSArray objects = iterator.nextBatch();
                    if(iterator.currentBatchIndex() % treshhold == 0) {
                        ec.unlock();
                        // ec.dispose();
                        ec = ERXEC.newEditingContext();
                        ec.lock();
                        iterator.setEditingContext(ec);
                    }
                    for(Enumeration i = incides.objectEnumerator(); i.hasMoreElements(); ) {
                        ERIndex index = (ERIndex) i.nextElement();
                        index.addObjectsToIndex(ec, objects);
                    }
                }
            } finally {
                ec.unlock();
            }
            log.info("Indexing {} took: {}ms", entity.name(), System.currentTimeMillis() - start);
        }
    }

    public void indexAllObjects(EOModel model) {
        for (Enumeration entities = model.entities().objectEnumerator(); entities.hasMoreElements();) {
            EOEntity entity = (EOEntity) entities.nextElement();
            indexAllObjects(entity);
        }
    }

    public void indexAllObjects(EOModelGroup group) {
        for (Enumeration models = group.models().objectEnumerator(); models.hasMoreElements();) {
            EOModel model = (EOModel) models.nextElement();
            indexAllObjects(model);
        }
    }
}
