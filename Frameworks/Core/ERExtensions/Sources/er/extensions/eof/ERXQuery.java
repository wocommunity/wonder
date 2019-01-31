package er.extensions.eof;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOProperty;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCPlugIn;

import er.extensions.eof.ERXEOAccessUtilities.ChannelAction;
import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * <h1>ERXQuery</h1>
 * 
 * <h2>Overview</h2>
 * 
 * This class has a fluent API that mimics a select statement:
 * <pre>
 * {@code
 * NSArray<NSDictionary<String,Object>> records =
 *     ERXQuery.create()
 *         .select(keys)
 *         .from(entity)
 *         .where(qualifier)
 *         .groupBy(groupings)
 *         .having(havingQualifier)
 *         .orderBy(sortings)
 *         .fetch();
 * }
 * </pre>
 * 
 * It allows you to use EOF/Wonder higher-level constructs (qualifiers, attributes,
 * orderings, key paths, ERXKeys, etc.) to create a query that looks like this:
 * 
 * <pre>
 * SELECT ...
 * FROM ...
 * WHERE ...
 * GROUP BY ...
 * HAVING ...
 * ORDER BY ...
 * </pre>
 * 
 * <h2>Specifying the Attributes to Fetch</h2>
 * 
 * The select() method is very flexible and powerful. It accepts a variable number
 * of objects of different types that specify the attributes to fetch. These objects
 * can be EOAttributes, ERXKeys, Strings. You may also specify any Iterable such as
 * NSArray, List, Collection, etc. containing any combination of these (EOAttributes,
 * ERXKeys, Strings).
 * <p>
 * The ERXKeys and String objects correspond to keys and key paths to the attributes
 * to fetch, i.e. "customer.name". The keys and key paths can also be relationships
 * to objects, i.e. "customer" which translate into a fetch of foreign keys used to
 * build object faults and return them in the results.
 * <p>
 * You may call the select() method multiple times to keep adding to the list of
 * attributes to fetch.
 * 
 * <h2>Using Ad Hoc Attributes</h2>
 * 
 * It is very common to aggregate attributes in these queries. For this purpose, you may
 * want to create what ERXQuery refers to as ad hoc attributes. These attributes have a
 * definition but are not physically attached to the entity. You can use the
 * ERXQueryAttributes class to easily create multiple ad hoc attributes. The definition
 * of the attribute can reference relationships and attributes as shown below. If you
 * just want to create a single ad hoc attribute you may use the ERXQueryEOAttribute class.
 * 
 * <pre>
 * {@code
 * // Using a single query against the order entity to count the number of
 * // orders and line items that match an order qualifier.
 * 
 * ERXQueryAttributes attributes = ERXQueryAttributes.create(orderEntity)
 *     .add("itemCount", "COUNT(DISTINCT lineItems.lineItemID)", "intNumber")
 *     .add("orderCount", "COUNT(DISTINCT orderID)", "intNumber");
 * 
 * ERXQuery query =
 *     ERXQuery.create()
 *         .select(attributes)
 *         .from(orderEntity)
 *         .where(qualifier);
 * 
 * // Fetch into a dictionary
 * NSDictionary<String,Object> row = query.fetch().lastObject();
 * 
 * int orderCount = ((Number) row.objectForKey("orderCount")).intValue();
 * int itemCount = ((Number) row.objectForKey("itemCount")).intValue();
 * }
 * </pre>
 * 
 * <h2>Fetching Results into a Custom Class</h2>
 * 
 * It is useful to fetch results into objects of a custom class.
 * This allows you to have type checking on the getter methods and add methods for
 * computed values on the data fetched. For the example above you could have fetched the
 * results into a custom class as follows:
 * <pre>
 * {@code
 * // Fetch into object instances of the a custom Result class
 * Result result = query.fetch(editingContext, Result.class).lastObject();
 * int orderCount = result.orderCount();
 * int itemCount = result.itemCount();
 * }
 * </pre>
 * 
 * The Result custom class would have to be defined as
 * shown below. The constructor may keep the mutable dictionary passed in to the
 * constructor or make an immutable copy from it as shown below.
 * <pre><code>
 * public static class Result {
 *     {@code NSDictionary<String,Object> data;}
 *     
 *     public Result(EOEditingContext ec, {@code NSMutableDictionary<String,Object>} row) {
 *         data = row.immutableClone();
 *     }
 *     
 *     public int itemCount() {
 *         return ((Number) data.objectForKey("itemCount")).intValue();
 *     }
 *     public int orderCount() {
 *         return ((Number) data.objectForKey("orderCount")).intValue();
 *     }
 * }
 * </code></pre>
 * In general, fetching into a custom class can be done in several ways:
 * <pre><code>
 * // If your custom class has a constructor that takes an editing context and
 * // a mutable dictionary then it is very simple:
 * {@code NSArray<Foo>} objs = query.fetch(editingContext, Foo.class);
 * 
 * // Using java 8 or later you may use a lambda expression:
 * {@code NSArray<Foo>} objs = query.fetch(editingContext, (ec, row) -> new Foo(ec, row));
 * 
 * // You may also create an implementation of the RecordConstructor
 * // functional interface and pass it into the fetch method:
 * {@code ERXQuery.RecordConstructor<Foo>} recordConstructor =
 *     new {@code ERXQuery.RecordConstructor<Foo>} {
 *         {@literal @}Override
 *         public Foo constructRecord(EOEditingContext ec, {@code NSMutableDictionary<String,Object>} row) {
 *             return new Foo(ec, row);
 *         }
 *     };
 * NSArray<Foo> objs = query.fetch(editingContext, recordConstructor);
 * </code></pre>
 * 
 * <h2>Augmenting Row Values</h2>
 * 
 * You can have entries from a dictionary added in to the rows
 * fetched from the database. The mutable dictionary passed in to the record
 * constructor will contain the data fetched along with the keys/values from
 * this recordInitializationValues dictionary.
 * <pre>
 * {@code
 * NSDictionary<String,Object> recordInitializationValues = new NSDictionary<>((Object)2017, "preferredYear");
 * NSArray<Foo> objs = query.fetch(editingContext, recordInitializationValues, Foo.class);
 * Foo aFoo = objs.lastObject();
 * int preferredYear = aFoo.preferredYear(); // i.e. 2017
 * }
 * </pre>
 * 
 * <h2>Defining Ad Hoc Attributes in the Entity</h2>
 * 
 * An alternate way to define your ad hoc attributes is to define them in your entity
 * and flagging them as non-class properties. Unlike ERXQueryEOAttribute objects,
 * these attributes will be instances of EOAttribute and reside in your entity. They
 * may be a bit distracting when looking at the entity if you have a lot but this
 * method allows you to reuse all the existing attributes and relationships already
 * defined in the entity and does not require code for creating the attributes.
 * <p>
 * One incovenience is that eogeneration templates do not generate ERXKeys
 * for non-class properties. However, this problem could be overcome by enhancing
 * the eogeneration templates to generate ERXKeys for <b>derived non-class property</b>
 * attributes.
 * <pre><code>
 * // Fetch last year's customer order totals exceeding $1000 in descending order
 * {@code NSArray<OrderSummary>} lastYearTopSales =
 *     ERXQuery.create()
 *         .select(Order.CUSTOMER)             // customer to-one
 *         .select(Order.SUM_TOTAL_AMOUNT)     // non-class property defined as SUM(totalAmount)
 *         .from(Order.ENTITY_NAME)
 *         .where(lastYearQualifier)
 *         .groupBy(Order.CUSTOMER)
 *         .having(Order.SUM_TOTAL_AMOUNT.greaterThan(1000.00))
 *         .orderBy(Order.SUM_TOTAL_AMOUNT.desc())
 *         .fetch(editingContext, OrderSummary.class);
 * 
 * // Peek at top sale record
 * OrderSummary topSale = ERXArrayUtilities.firstObject(lastYearTopSales);
 * if (topSale != null) {
 *     System.out.println("Customer " + topSale.customer().fullName() 
 *         + " ordered " + moneyFormatter.format(topSale.sumTotalAmount()));
 * }
 * </code></pre>
 * 
 * It would be nice to enhance the eogeneration templates to also create a custom
 * class for fetching the results, i.e. WonderEntitySummary.java and _WonderEntitySummary.java
 * with the getters for attributes/relationships in the entity including derived non-class
 * properties. These templates would be used when the entity has a user info key with
 * ERXQuery.enabled=yes.
 * 
 * <h2>Limitations</h2>
 * 
 * Ad hoc attributes created with ERXQueryAttributes or ERXQueryEOAttribute are not
 * physically attached to an entity. When EOF generates SQL for a qualifier it calls
 * sqlStringForSQLExpression(q,e) where q is an EOQualifier and e is an EOSQLExpression.
 * Qualifiers then try to reach the attribute by following the qualifier's referenced
 * keys starting with the entity of the EOSQLExpression, i.e. e.entity().
 * <p>
 * The current workaround used by ERXQuery is to temporarily add to the entity any
 * ad hoc attributes referenced by the qualifiers. This typically happens with the
 * havingQualifier which normally references the ad hoc attributes corresponding to
 * aggregated attributes. For example, {@code "sumTotalAmount"} defined as
 * {@code "SUM(totalAmount)"} could be used in a having qualifier:
 * <pre>
 * {@code
 * // When grouping orders by customer and fetching sumTotalAmount we may want to have
 * // this having qualifier so that we only fetch the groups totaling more than 1000.
 * EOQualifier havingQualifier = ERXQ.greaterThan("sumTotalAmount", new BigDecimal(1000.0));
 * }
 * </pre>
 * However, if you were to define your {@code "sumTotalAmount"} attribute in your entity
 * as a derived non-class property with definition {@code "SUM(totalAmount)"} then ERXQuery
 * doesn't have to add the attribute to the entity.
 * 
 * <h2>Defaults for Behavior Properties</h2>
 * <ol>
 * <li>er.extensions.eof.ERXQuery.useBindVariables=false</li>
 * <li>er.extensions.eof.ERXQuery.useEntityRestrictingQualifiers=true</li>
 * <li>er.extensions.eof.ERXQuery.removeForeignKeysFromRowValues=true</li>
 * </ol>
 * 
 * @author Ricardo J. Parada
 */
public class ERXQuery {
	
	/** 
	 * <a href="http://wiki.wocommunity.org/display/documentation/Wonder+Logging">new org.slf4j.Logger</a> 
	 */
	static final Logger log = LoggerFactory.getLogger(ERXQuery.class);
	
	protected EOEditingContext editingContext;
	protected EOEntity mainEntity;
	protected EOQualifier mainSelectQualifier;
	protected EOQualifier havingQualifier;
	protected NSMutableArray<EOSortOrdering> orderings;
	protected NSMutableDictionary<String,String> relationshipAliases;
	
	protected boolean usesDistinct;
	protected boolean isCountingStatement;
	protected String queryHint;
	protected boolean useBindVariables;
	
	// These are populated by computeSelectAndGroupingAttributes()
	protected NSMutableArray<String> fetchKeys;
	protected NSMutableArray<String> groupingKeys;
	protected NSMutableArray<EOAttribute> adHocAttributes;
	protected NSMutableArray<EOAttribute> adHocGroupings;
	
	protected NSMutableArray<EOAttribute> selectAttributes;
	protected NSMutableArray<EOAttribute> groupingAttributes;
	protected NSMutableDictionary<String,EOAttribute> attributesByName;
	protected NSMutableSet<RelationshipKeyInfo> relationshipKeysSet;
	protected boolean refreshRefetchedObjects;
	
	protected int serverFetchLimit;
	protected int clientFetchLimit;
	
	protected double queryEvaluationTime;
	
	protected ERXQuery() {
		// Set defaults
		fetchKeys = new NSMutableArray<>();
		groupingKeys = new NSMutableArray<>();
		orderings = new NSMutableArray<>();
		refreshRefetchedObjects = false;
		usesDistinct = false;
		isCountingStatement = false;
		queryHint = null;
		relationshipAliases = new NSMutableDictionary<>();
		
		// This will hold any ad hoc attributes to be selected
		adHocAttributes = new NSMutableArray<>(2);
		// This will hold any ad hoc attributes to use in the group by clause
		adHocGroupings = new NSMutableArray<>(2);
		
		// Determine features to enable / disable
		useBindVariables = ERXProperties.booleanForKeyWithDefault("er.extensions.eof.ERXQuery.useBindVariables", false);
	}
	
	//
	// FLUENT API
	//
	
	public static ERXQuery create() {
		return new ERXQuery();
	}
	
	/**
	 * Specifies whether to select count(*)
	 * 
	 * @return this query
	 */
	public ERXQuery selectCount() {
		isCountingStatement = true;
		return this;
	}
	
	/**
	 * Specifies the attributes to fetch. The attributes may be specified using 
	 * EOAttributes, ERXKeys, String objects (for keys and key paths), or Iterable
	 * objects such as NSArray, List, Collection containing EOAttributes, ERXKeys,
	 * Strings or inclusive other Iterables. The String and ERXKey objects must
	 * correspond to the names of the attributes to fetch or to the key paths to
	 * leading to the attributes to fetch. You may call the select() method
	 * multiple times to keep adding to the list of attributes to fetch.
	 * 
	 * @param attributesOrKeys list of attributes to select in the fetch
	 * 
	 * @return this query
	 */
	public ERXQuery select(Object... attributesOrKeys) {
		for (Object obj : attributesOrKeys) {
			if (obj instanceof String) {
				String key = (String) obj;
				fetchKeys.add(key);
			} else if (obj instanceof ERXKey<?>) {
				ERXKey<?> erxKey = (ERXKey<?>) obj;
				String key = erxKey.key();
				fetchKeys.add(key);
			} else if (obj instanceof EOAttribute) {
				EOAttribute adHocAttribute = (EOAttribute) obj;
				adHocAttributes.add(adHocAttribute);
			} else if (obj instanceof Iterable) {
				Iterable iterable = (Iterable) obj;
				// Use recursion to add each object in the array
				for (Object e : iterable) {
					select(e);
				}
			}
		}
		return this;
	}
	
	
	/**
	 * Specifies whether or not to use DISTINCT.
	 * 
	 * @return this query
	 */
	public ERXQuery usingDistinct() {
		usesDistinct = true;
		return this;
	}
	
	/**
	 * Specifies whether to refresh refetched objects referenced
	 * by relationship keys, i.e. "customer".
	 * 
	 * @return this query
	 */
	public ERXQuery refreshingRefetchedObjects() {
		refreshRefetchedObjects = true;
		return this;
	}

	/**
	 * Specifies the EOEntity object to select from.
	 * 
	 * @param entity the entity to fetch
	 * 
	 * @return this query
	 */
	public ERXQuery from(EOEntity entity) {
		mainEntity = entity;
		return this;
	}
	
	/**
	 * Specifies the name of EOEntity object to select from.
	 * 
	 * @param entityName the entity to fetch
	 * 
	 * @return this query
	 */
	public ERXQuery from(String entityName) {
		return from(ERXModelGroup.defaultGroup().entityNamed(entityName));
	}
	
	/**
	 * Specifies the main qualifier used to build the where clause.
	 * 
	 * @param qual the qualifier for the fetch
	 * 
	 * @return this query
	 */
	public ERXQuery where(EOQualifier qual) {
		mainSelectQualifier = mainEntity.schemaBasedQualifier(qual);
		return this;
	}
	
	/**
	 * Use this to specify the attributes to group by. The objects can be EOAttributes,
	 * ERXKeys, Strings, or any Iterable such as NSArrays, Lists, Collections, etc. containing
	 * EOAttributes, ERXKeys, Strings or inclusive other Iterables. The ERXKeys and String
	 * objects must correspond to the keys or key paths to the attributes to group by. You may
	 * call this method multiple times to keep on adding to the list of attributes to group by.
	 * 
	 * @param attributesOrKeys list of attributes to use for the group by
	 * 
	 * @return this query
	 */
	public ERXQuery groupBy(Object... attributesOrKeys) {
		for (Object obj : attributesOrKeys) {
			if (obj instanceof String) {
				String key = (String) obj;
				groupingKeys.add(key);
			} else if (obj instanceof ERXKey<?>) {
				ERXKey<?> erxKey = (ERXKey<?>) obj;
				String key = erxKey.key();
				groupingKeys.add(key);
			} else if (obj instanceof EOAttribute) {
				EOAttribute adHocAttribute = (EOAttribute) obj;
				groupingAttributes.add(adHocAttribute);
			} else if (obj instanceof Iterable) {
				Iterable iterable = (Iterable) obj;
				for (Object e : iterable) {
					groupBy(e);
				}
			} else if (obj != null) {
				throw new RuntimeException(getClass().getSimpleName() 
						+ "'s groupBy() does not accept instances of " 
						+ obj.getClass().getName() + ". Only String, ERXKey, EOAttribute "
								+ "or collection of them are valid.");
			}
		}
		return this;
	}
	
	
	/**
	 * Specifies the sort orderings used to build the order by clause. The objects passed
	 * in can be EOSortOrderings or Iterables containing EOSortOrderings or other Iterables.
	 * You may call this method multiple times to keep adding to the list of orderings.
	 * 
	 * @param orderingObjects sort orders
	 * 
	 * @return this query
	 */
	public ERXQuery orderBy(Object... orderingObjects) {
		for (Object obj : orderingObjects) {
			if (obj instanceof EOSortOrdering) {
				EOSortOrdering sortOrdering = (EOSortOrdering) obj;
				orderings.add(sortOrdering);
			} else if (obj instanceof Iterable) {
				Iterable iterable = (Iterable) obj;
				for (Object o : iterable) {
					orderBy(o);
				}
			} else if (obj != null) {
				throw new RuntimeException(getClass().getSimpleName() 
						+ "'s orderBy() does not accept instances of " 
						+ obj.getClass().getName() + ". Only String, ERXKey, EOAttribute "
						+ "or collection of them are valid.");
			}
		}
		return this;
	}
	
	/**
	 * Specifies the qualifier for the having clause.
	 * 
	 * @param qual qualifier for the having clause
	 * 
	 * @return this query
	 */
	public ERXQuery having(EOQualifier qual) {
		havingQualifier = mainEntity.schemaBasedQualifier(qual);
		return this;
	}
	
	/**
	 * This string is inserted after the SELECT keyword in the generated SQL
	 * padded with a space on both sides. This allows you to send a hint
	 * to the database server.
	 * 
	 * @param value query hint
	 * 
	 * @return this query
	 */
	public ERXQuery usingQueryHint(String value) {
		queryHint = value;
		return this;
	}
	
	/**
	 * Enables use of bind variables. If this is not called then
	 * ERXQuery looks at the er.extensions.eof.ERXQuery.useBindVariables property
	 * which defaults to false currently, thereby placing values in-line
	 * with the SQL generated.
	 * 
	 * @return this query
	 */
	public ERXQuery usingBindVariables() {
		useBindVariables = true;
		return this;
	}
	
	/**
	 * If specified then the query will be wrapped with something like
	 * this, depending on the database product:
	 * 
	 * <pre><code>SELECT * FROM ( query ) WHERE ROWNUM <= limit</code></pre>
	 * 
	 * @param limit max number of rows in result
	 * 
	 * @return this query
	 */
	public ERXQuery serverFetchLimit(int limit) {
		this.serverFetchLimit = limit;
		return this;
	}
	
	/**
	 * If specified then the fetch will be stopped/canceled after fetching
	 * clientFetchLimit records. This does not affect the SQL generated unlike
	 * serverFetchLimit.
	 * 
	 * @param limit max number of rows in result
	 * 
	 * @return this query
	 */
	public ERXQuery clientFetchLimit(int limit) {
		this.clientFetchLimit = limit;
		return this;
	}
	
	//
	// Fetch methods
	//
	
	/**
	 * Does the fetch and returns the values.
	 * 
	 * @return list of fetched records
	 */
	public NSArray<NSDictionary<String,Object>> fetch() {
		NSDictionary<String,Object> recordInitializationValues = NSDictionary.emptyDictionary();
		EOEditingContext ec = ERXEC.newEditingContext();
		return fetch(ec, recordInitializationValues);
	}
	
	/**
	 * Does the fetch and returns the values.
	 * 
	 * @param ec the editing context to use for the fetch
	 * 
	 * @return list of fetched records
	 */
	public NSArray<NSDictionary<String,Object>> fetch(EOEditingContext ec) {
		NSDictionary<String,Object> recordInitializationValues = NSDictionary.emptyDictionary();
		return fetch(ec, recordInitializationValues);
	}
	
	public NSArray<NSDictionary<String, Object>> fetch(EOEditingContext ec, NSDictionary<String,Object> recordInitializationValues) {
		return fetch(getExpression(ec), selectAttributes, ec, recordInitializationValues, new DefaultRecordConstructor());
	}
	
	/**
	 * Returns fetch(ec, recordClass, NSDictionary.emptyDictionary())
	 * 
	 * @param ec the editing context to use for the fetch
	 * @param recordClass class to use for record entries
	 * 
	 * @return list of fetched records
	 */
	public <T> NSArray<T> fetch(EOEditingContext ec, Class<T> recordClass) {
		NSDictionary<String,Object> recordInitializationValues = NSDictionary.emptyDictionary();
		return fetch(ec, recordInitializationValues, recordClass);
	}
	
	/**
	 * Use this method to fetch results into objects of the specified class. The class
	 * must have a constructor that takes an EOEditingContext and an NSMutableDictionary
	 * as arguments The row passed into the constructor will contain data fetched for the
	 * row as well as the entries from recordInitializatonValues dictionary.
	 * 
	 * @param anEC the editing context to use for the fetch
	 * @param recordInitializationValues values to add as record entries to result
	 * @param recordClass class to use for record entries
	 * 
	 * @return list of fetched records
	 */
	public <T> NSArray<T> fetch(EOEditingContext anEC, final NSDictionary<String,Object> recordInitializationValues, Class<T> recordClass) {
		// Get the constructor once here before we enter the fetch-loop
		final Constructor<T> constructor;
		try {
			Class<?>[] parameterTypes = new Class<?>[] { EOEditingContext.class, NSMutableDictionary.class };
			constructor = recordClass.getConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new NSForwardException(e, "ERXQuery: record class '" + recordClass.getName() + "' must have a constructor with an EOEditingContext and NSMutableDictionary as arguments");
		} catch (SecurityException e) {
			throw new NSForwardException(e);
		}
		// We got the constructor for recordClass above... Now let's use it
		// to create a RecordConstructor implementation that calls it to
		// create instances for the recordClass class.
		RecordConstructor<T> recordConstructor = 
			new RecordConstructor<T>() {
				@Override
				public T constructRecord(EOEditingContext ec, NSMutableDictionary<String, Object> row) {
					try {
						Object[] args = new Object[] { ec, row };
						return constructor.newInstance(args);
					} catch (InstantiationException exception) {
						throw new RuntimeException(exception);
					} catch (IllegalAccessException exception) {
						throw new RuntimeException(exception);
					} catch (InvocationTargetException exception) {
						throw new RuntimeException(exception);
					}
				}
			};
		
		return fetch(anEC, recordInitializationValues, recordConstructor);
	}
	
	/**
	 * Use this method to fetch either by using an implementation of the RecordConstructor
	 * functional interface or by using a lambda expression as follows:
	 * 
	 * <h2>1. Define your custom record class</h2>
	 * <pre><code>
	 * public class Foo {
	 *    {@code NSMutableDictionary<String,Object>} data;
	 *    
	 *    // Constructor
	 *    public Foo(EOEditingContext ec, {@code NSMutableDictionary<String,Object>} row) {
	 *       data = row;
	 *    }
	 * }
	 * </code></pre>
	 * 
	 * <h2>2. Fetch the records</h2>
	 * 
	 * <pre><code>
	 * EOEditingContext editingContext = ERXEC.newEditingContext();
	 * ERXQuery query = ...;
	 * 
	 * // This assumes Java <= 7
	 * {@code ERXQuery.RecordConstructor<Foo>} recordConstructor =
	 *    new {@code ERXQuery.RecordConstructor<Foo>}(){
	 *    
	 *       {@literal @}Override
	 *       public Foo constructRecord(EOEditingContext ec, {@code NSMutableDictionary<String,Object>} row) {
	 *          return new Foo(ec, row);
	 *       }
	 *       
	 *    };
	 * 
	 * {@code NSArray<Foo>} foos = query.fetch(editingContext, recordConstructor);
	 * 
	 * // This assumes Java >= 8
	 * {@code NSArray<Foo>} foos = query.fetch(editingContext, (ec, row) -> new Foo(ec, row));
	 * </code></pre>
	 * 
	 * @param ec the editing context to use for the fetch
	 * @param recordInitializationValues values to add as record entries to result
	 * @param recordConstructor constructor for record entries
	 * @param recordClass class to use for record entries
	 * 
	 * @return list of fetched records
	 */
	public <T> NSArray<T> fetch(EOEditingContext ec, NSDictionary<String,Object> recordInitializationValues, RecordConstructor<T> recordConstructor) {
		EOSQLExpression sqlExpression = getExpression(ec);
		return fetch(sqlExpression, selectAttributes, ec, recordInitializationValues, recordConstructor);
	}

	/**
	 * Convenience method to return fetch(ec, NSDictionary.emptyDictionary(), recordConstructor)
	 * 
	 * @param ec the editing context to use for the fetch
	 * @param recordConstructor constructor for record entries
	 * 
	 * @return list of fetched records
	 */
	public <T> NSArray<T> fetch(EOEditingContext ec, RecordConstructor<T> recordConstructor) {
		NSDictionary<String,Object> recordInitializationValues = NSDictionary.emptyDictionary();
		return fetch(ec, recordInitializationValues, recordConstructor);
	}
	
	/**
	 * Core fetch method. Given the EOSQLExpression built to fetch the selectAttributes
	 * this method fetches the results. As each row result is fetched this method calls
	 * the recordConstructor with the editing context and mutable dictionary containing
	 * the row values and the entries from the initValues dictionary. The record
	 * constructor should return an instance of T. The T instances are then placed
	 * into an array that this method returns, i.e. {@code NSArray<T>}
	 * 
	 * @param expression the SQL expression for the fetch
	 * @param fetchAttributes attributes to fetch
	 * @param ec the editing context to use for the fetch
	 * @param initValues values to add as record entries to result
	 * @param recordConstructor constructor for record entries
	 * 
	 * @return list of fetched records
	 */
	protected <T> NSArray<T> fetch(
			final EOSQLExpression expression,
			final NSArray<EOAttribute> fetchAttributes,
			final EOEditingContext ec,
			final NSDictionary<String,Object> initValues,
			final RecordConstructor<T> recordConstructor
		)
	{
		// Array to hold fetched records
		final NSMutableArray<NSMutableDictionary<String, Object>> rows = new NSMutableArray<>();
		
		// Create channel action anonymous class for evaluating SQL and fetching records
		ChannelAction action = new ERXEOAccessUtilities.ChannelAction() {
			@Override
			protected int doPerform(EOAdaptorChannel channel) {
				// Record starting time
				long start = new NSTimestamp().getTime();
				channel.evaluateExpression(expression);

				// Compute elapsed time
				long end = new NSTimestamp().getTime();
				queryEvaluationTime = (end - start) / 1000.0;
				
				// Log elapsed time
				log.debug("Expression evaluation time = {} seconds.\n\n", queryEvaluationTime);
				
				// Use the names of the fetch attributes for the keys in the
				// row dictionaries when fetching
				setupAdaptorChannelEOAttributes(channel, fetchAttributes);
				
				// Fetch results 
				try {
					boolean hasInitValues = initValues.count() > 0;
					NSMutableDictionary<String, Object> row = channel.fetchRow();
					while (row != null) {
						if (hasInitValues) {
							row.addEntriesFromDictionary(initValues);
						}
						rows.add(row);
						
						// If a fetch limit was specified then exit fetch-loop as soon 
						// as the limit is reached
						if (clientFetchLimit > 0 && rows.count() >= clientFetchLimit) {
							break;
						}
						row = channel.fetchRow();
					} 
				} catch (Throwable t) {
					log.error("Error occurred while fetching rows: ", t);
					throw new RuntimeException(t);
				} finally {
					channel.cancelFetch();
				}
				return rows.count();
			}
		};
		
		// Perform the action to evaluate the SQL and fetch the records
		action.perform(ec, expression.entity().model().name());
		
		// Array to hold fetched records
		final NSMutableArray<T> records = new NSMutableArray<>();
		boolean removeForeignKeysFromRowValues = ERXProperties.booleanForKeyWithDefault("er.extensions.eof.ERXQuery.removeForeignKeysFromRowValues", true);
		for (NSMutableDictionary<String, Object> row : rows) {
			// Replace any foreign keys with their corresponding relationship keys
			// and the enterprise object as the value.
			for (RelationshipKeyInfo relKeyInfo : relationshipKeysSet.allObjects()) {
				Object eo = null;
				String entityName = relKeyInfo.entityName();
				String relationshipKey = relKeyInfo.relationshipKeyPath();
				String foreignKey = relKeyInfo.sourceAttributeKeyPath();
				Object primaryKeyValue = row.objectForKey(foreignKey);
				if (primaryKeyValue != NSKeyValueCoding.NullValue) {
					eo = ERXEOControlUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyValue, null, refreshRefetchedObjects);
					row.setObjectForKey(eo, relationshipKey);
				}
				if (removeForeignKeysFromRowValues) {
					row.removeObjectForKey(foreignKey);
				}
			}
			T obj = recordConstructor.constructRecord(ec, row); 
			records.addObject(obj);
		}

		return records;
	}
	
	protected static void setupAdaptorChannelEOAttributes(EOAdaptorChannel adaptorChannel, NSArray<EOAttribute> selectAttributes) {
		// Have the adaptor provide the attributes to fetch the results.  These attributes
		// have weird names.  Here we borrow a technique from David Scheck shared on the
		// webobjects mailing list.  The technique consists in renaming the attributes
		// provided by the adaptor channel and name it the same as the corresponding
		// attribute used by ERXQuery.  The attribute names used by ERXQuery are more
		// meaningful and what the developer expects to see in the row dictionary
		// passed in to the record constructor.
		if (selectAttributes != null && selectAttributes.count() > 0) {
			
			// Rename the EOAttributes provided by the adaptor
			NSArray<EOAttribute> adaptorSelectAttributes = adaptorChannel.describeResults();
			int count = adaptorSelectAttributes.count();
			for (int i = 0; i < count; i++) {
				EOAttribute adaptorSelectAttribute = adaptorSelectAttributes.objectAtIndex(i);
				EOAttribute selectAttribute = selectAttributes.objectAtIndex(i);
				adaptorSelectAttribute.setName(selectAttribute.name());
				String externalType = selectAttribute.externalType();
				String className = selectAttribute.className();
				String valueType = selectAttribute.valueType();
				if (externalType != null) adaptorSelectAttribute.setExternalType(externalType);
				if (className != null) adaptorSelectAttribute.setClassName(className);
				if (valueType != null) adaptorSelectAttribute.setValueType(valueType);
			}
			adaptorChannel.setAttributesToFetch(adaptorSelectAttributes);
		}
	}
	
	/**
	 * Convenience method for fetching a single attribute from a query resulting in zero
	 * or one record. For example:
	 * 
	 * Number count = (Number) ERXQuery.create().selectCount().from(entity).where(qual).fetchValue();
	 * 
	 * @return The value or null when the query returns zero records or an NSKeyValueCoding.NullValue.
	 */
	public Object fetchValue() {
		NSArray<NSDictionary<String, Object>> records = fetch();
		int count = records.count();
		
		if (count == 0) {
			return null;
		}
		
		if (count > 1) {
			throw new RuntimeException(getClass().getSimpleName() 
					+ " fetchValue() must be used with a query that returns one or zero records." 
					+ " This query returned " + count + " records.");
		}
		
		NSDictionary<String, Object> rec = records.lastObject();
		NSArray<Object> values = rec.allValues();
		count = values.count();
		if (count != 1) {
			throw new RuntimeException(getClass().getSimpleName() 
					+ " fetchValue() must be used with a query that fetches a single attribute." 
					+ " This query fetched " + count + " attributes.");
		}
		Object value = values.lastObject();
		if (value == NSKeyValueCoding.NullValue) {
			return null;
		}
		return value;
	}
	
	/** 
	 * Sets the table alias to use for a given relationship name. For example, if
	 * the query selects from CLAIM and the main query qualifier joins to other tables
	 * including the LINE_ITEM table via the lineItems relationship. If you don't
	 * specify a table alias for the lineItems relationship then it would use whatever
	 * EOF comes up with, for example T3. If you wanted X1 to be used as the table
	 * alias then simply call this method with "lineItems" as the relationship name
	 * and "X1" as the table alias.
	 * 
	 * @param relationshipName name of relationship
	 * @param alias alias name to use
	 * 
	 * @return this query
	 */
	public ERXQuery usingRelationshipAlias(String relationshipName, String alias) {
		relationshipAliases.setObjectForKey(alias, relationshipName);
		return this;
	}
	
	
	//
	// HELPER METHODS
	//
	
	/** 
	 * Returns a complete select expression as follows:
	 * <pre>
	 *      SELECT ...
	 *      FROM ...
	 *      WHERE ...
	 *      GROUP BY ...
	 *      HAVING ...
	 *      ORDER BY ...
	 * </pre>
	 * 
	 * The {@code WHERE} clause is constructed from the qualifier (if any) passed
	 * into the {@code where()} method and any required joins necessary to access
	 * any referenced properties.
	 * <p>
	 * The {@code GROUP BY} clause is constructed from the grouping attributes
	 * specified by calling any of the {@code groupBy()} methods.
	 * <p>
	 * The {@code HAVING} clause is constructed if a qualifier is specified by
	 * calling the {@code having()} method.
	 * <p>
	 * The {@code ORDER BY} clause is constructed from attributes passed in to
	 * the {@code orderBy()} method.
	 * 
	 * @param ec the editing context to use for SQL construction
	 * 
	 * @return SQL expression
	 */
	public EOSQLExpression getExpression(EOEditingContext ec) {
		// Establish the editing context.  This is important as some of the
		// helper methods assume the editingContext i-var has been set.  For
		// example EOSQLExpression factory)
		if (ec != null) {
			editingContext = ec;
		} else {
			editingContext = ERXEC.newEditingContext();
		}
		
		// Populate the selectAttributes and groupingAttributes arrays
		computeSelectAndGroupingAttributes();
		
		// Incorporate any entity restricting qualifiers (if any) into the mainSelectQualifier
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.eof.ERXQuery.useEntityRestrictingQualifiers", true)) {
			// Get expression similar to what will be used to build the SQL
			EOQualifier restrictingQualifierForReferencedEntities = restrictingQualifierForReferencedEntities();
			if (restrictingQualifierForReferencedEntities != null) {
				mainSelectQualifier = ERXQ.and(mainSelectQualifier, restrictingQualifierForReferencedEntities);
			}
		}
		
		// Get initial expression that will be used to build the sql string.  Notice that
		// the sort orderings is null.  This avoids an exception when orderings includes
		// ad hoc attributes that are not physically attached to an entity.  We'll build
		// the GROUP BY clause later in a different manner.
		EOFetchSpecification spec = new EOFetchSpecification(mainEntity.name(), mainSelectQualifier, null /* orderings */);
		
		EOSQLExpressionFactory factory = ERXQuery.sqlExpressionFactory(mainEntity, ec);
		EOSQLExpression e = factory.selectStatementForAttributes(selectAttributes, false, spec, mainEntity);

		// Start building the SELECT... FROM ... 
		StringBuilder columnList = new StringBuilder();
		if (queryHint != null) {
			columnList.append(" " + queryHint + " ");
		}
		if (usesDistinct && !isCountingStatement) {
			columnList.append("DISTINCT ");
		}

		if (isCountingStatement) {
			NSArray<String> pKeyNames = mainEntity.primaryKeyAttributeNames();
			if (usesDistinct && pKeyNames.count() == 1) {
				EOAttribute attribute = mainEntity.attributeNamed(pKeyNames.lastObject());
				columnList.append("COUNT(DISTINCT t0." + attribute.columnName() + " )");
			} else {
				columnList.append("COUNT(*)");
			}
			
		} else {
			columnList.append(e.listString());
		}
		
		// Add table list
		String tableList = e.tableListWithRootEntity(mainEntity);
		
		// Add WHERE clause if necessary
		String joinClauseString = e.joinClauseString();
		
		String qualClauseString = e.whereClauseString();
		
		// Initial code assembled the SQL but was not compatible with FrontBase uses of sqj92 join statements. 
		// This code uses the database plugin EOSQLExpression to assemble the first parts of the statement.
		StringBuilder sql = new StringBuilder();
		sql.append(e.assembleSelectStatementWithAttributes(selectAttributes, false, spec.qualifier(), spec.sortOrderings(), "SELECT ", columnList.toString(), tableList, qualClauseString, joinClauseString, "", null));
		
		if (groupingAttributes.count() > 0) {
			sql.append("\n");
			sql.append("GROUP BY");
			sql.append("\n\t");
			// Add the sql for each grouping attribute
			Enumeration<EOAttribute> enumeration = groupingAttributes.objectEnumerator();
			while (enumeration.hasMoreElements()) {
				EOAttribute a = enumeration.nextElement();
				sql.append(sqlStringForAttribute(e, a));
				if (enumeration.hasMoreElements()) {
					sql.append(", ");
				}
			}
		}
		
		// Append HAVING clause
		
		EOSQLExpression havingExpression = null;
		if (havingQualifier != null) {
			// Add any ad hoc attributes referenced by the havingQualifier to mainEntity
			// so that we can generate the SQL for the having qualifier otherwise EOF
			// will throw an exception on us when it calls sqlStringForSQLExpression(q,e)
			// on each qualifier in the qualifier graph and one of them cannot get to
			// the attribute by looking up q's key in the e.entity().
			
			// First determine which keys in the havingQualifier reference ad hoc attributes
			NSSet<String> havingQualifierKeys = havingQualifier.allQualifierKeys();
			NSMutableArray<EOAttribute> toBeAdded = new NSMutableArray<>();
			for (String aKey : havingQualifierKeys) {
				EOAttribute a = attributesByName.objectForKey(aKey);
				if (a instanceof ERXQueryEOAttribute) {
					toBeAdded.add(a);
				}
			}
			
			// Modify entity by running an anonymous EntityModificationAction
			new EntityModificationAction() {
				
				@Override
				protected void modifyEntity(EOEntity entity) {
					if (toBeAdded.count() == 0) {
						return;
					}
					// Remember current class properties
					NSArray<EOProperty> classProperties = mainEntity.classProperties();
					
					// Add the attributes
					for (EOAttribute a : toBeAdded) {
						entity.addAttribute(a);
					}
					
					// The attributes added are all ad hoc attributes and
					// we don't want them as class properties.  Therefore,
					// restore original class properties.
					entity.setClassProperties(classProperties);
				}
			}.run(editingContext, mainEntity);
			
			// The attributes toBeAdded have been added
			NSMutableArray<EOAttribute> addedAttributes = toBeAdded;
			
			
			// Now create an expression to generate SQL for the HAVING clause
			try {
				EOFetchSpecification havingSpec = new EOFetchSpecification(mainEntity.name(), havingQualifier, null);
				havingExpression = factory.selectStatementForAttributes(selectAttributes, false, havingSpec, mainEntity);
			} catch (Throwable t) {
				throw new RuntimeException("Error generating SQL for havingQualifier: " + havingQualifier, t);
			} finally {
				new EntityModificationAction() {
					
					@Override
					protected void modifyEntity(EOEntity entity) {
						// Remove any attributes that were added to the entity
						for (EOAttribute a : addedAttributes) {
							entity.removeAttribute(a);
						}
					}
					
				}.run(editingContext, mainEntity);
				
			}
			
			// Append HAVING clause
			sql.append("\n");
			sql.append("HAVING");
			sql.append("\n\t");
			sql.append(havingExpression.whereClauseString());
		}
		
		// Append ORDER BY clause
		
		/* 
		
		// This was the original code but it has a problem with ad hoc attributes where
		// EOF throws an exception saying that attribute for key path is not reachable from
		// the entity.  That happens because ad hoc attributes are not physically attached
		// to the entity.  You cannot get to the attribute by looking up the ordering's key
		// in the entity, which is what EOF does.  We do this differently, i.e. look for the
		// EOAttribute in the select attributes that matches the ordering key.  Then we
		// use that attribute to generate the SQL for it.

		String orderByString = e.orderByString();
		if (orderByString != null && orderByString.length() > 0) {
			sql.append("\n");
			sql.append("ORDER BY");
			sql.append("\n\t");
			sql.append(orderByString);
		}
		
		*/
		
		if (orderings.count() > 0) {
			sql.append("\n");
			sql.append("ORDER BY");
			sql.append("\n\t");
			// Add the sql for each ordering attribute
			Enumeration<EOSortOrdering> orderingsEnumeration = orderings.objectEnumerator();
			while (orderingsEnumeration.hasMoreElements()) {
				EOSortOrdering ordering = orderingsEnumeration.nextElement();
				String orderingKey = ordering.key();
				EOAttribute orderingAttribute = null;
				for (EOAttribute a : selectAttributes) {
					if (orderingKey.equals(a.name())) {
						orderingAttribute = a;
						break;
					}
				}
				// Append the SQL for the ordering attribute
				sql.append(sqlStringForOrderingAttribute(e, orderingAttribute, ordering.selector()));
				
				if (orderingsEnumeration.hasMoreElements()) {
					sql.append(",\n\t");
				}
			}
		}
		
		// At this point the sql string is almost complete.  We just need to replace
		// table aliases by the ones desired by the caller.  For example, if T3 was
		// used for the lineItems relationship and the caller wants X1 to be used
		// instead then we need to replace all occurrences of T3 by X1.
		
		String sqlString = sql.toString();

		if (relationshipAliases.count() > 0) {
			// From the WebObjects documentation:
			
			// aliasesByRelationshipPath() returns a dictionary of table aliases.
			// The keys of the dictionary are relationship paths -- "department" and
			// "department.location", for example. The values are the table aliases
			// for the corresponding table -- "t1" and "t2", for example. The dictionary
			// always has at least one entry: an entry for the EOSQLExpression's entity. 
			// The key of this entry is the empty string ("") and the value is "t0". The 
			// dictionary returned from this method is built up over time with successive
			// calls to sqlStringForAttributePath.

			NSDictionary<String,String> aliasesByRelationshipPath = e.aliasesByRelationshipPath();
			for (String relationshipPath : relationshipAliases.allKeys()) {
				String aliasUsed = aliasesByRelationshipPath.objectForKey(relationshipPath);
				String aliasDesired = relationshipAliases.objectForKey(relationshipPath);
				sqlString = sqlString.replaceAll("\\b" + aliasUsed, aliasDesired);
			}
		}
		
		// Now build the new expression using the SQL string built from the first
		// expression and copy the bindings over from the first expression
		EOSQLExpression mainExpression = factory.expressionForEntity(mainEntity);
		mainExpression.setStatement(sqlString);
		
		NSArray<NSDictionary<String,?>> bindVariableDictionaries = e.bindVariableDictionaries();
		for (NSDictionary<String,?> binding : bindVariableDictionaries) {
			mainExpression.addBindVariableDictionary(binding);
		}
		
		// Copy the bindings from the havingExpression
		if (havingExpression != null) {
			bindVariableDictionaries = havingExpression.bindVariableDictionaries();
			for (NSDictionary<String,?> binding : bindVariableDictionaries) {
				mainExpression.addBindVariableDictionary(binding);
			}
		}
		
		// If we should not use bind variables then replace the bind variable 
		// place holders in the SQL by their values
		if (!useBindVariables) {
			String sqlWithBindingsInline = sqlWithBindingsInline(mainExpression.statement(), mainExpression);
			mainExpression = factory.expressionForEntity(mainEntity);
			mainExpression.setStatement(sqlWithBindingsInline);
		}	
		
		// See if you have to add SELECT * FROM ( original select SQL ) WHERE ROWNUM <= fetchLimit 
		if (serverFetchLimit > 0) {
			String statementWithLimitClause = addLimitClause(mainEntity, mainExpression.statement(), serverFetchLimit);
			mainExpression.setStatement(statementWithLimitClause);
		}
		
		return mainExpression;
	}
	
	/**
	 * Turns the SQL statement into something like this:
	 * 
	 * <pre><code>"SELECT * FROM ( " + statement + " ) WHERE ROWNUM <= " + limit;</code></pre>
	 * 
	 * @param entity entity on which to fetch
	 * @param statement SQL statement
	 * @param limit max number of rows in result
	 * 
	 * @return SQL string
	 */
	protected String addLimitClause(EOEntity entity, String statement, int limit) {
		// This works for ORACLE and I think it is better for my needs that what ERXSQLHelper does. 
		if ("oracle".equals(databaseProductName(entity))) {
			return wrapped("SELECT * FROM ( ",  statement, " ) WHERE ROWNUM <= " + limit);
		}
		// Use ERXSQLHelper for all the other database products
		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(entity);
		return sqlHelper.limitExpressionForSQL(null, null, statement, 0, limit);
	}
	
	protected String wrapped(String leftHandSide, String statement, String rightHandSide) {
		String sql = statement;
		
		// Assumes each clause in its own line
		NSArray<String> lines = NSArray.componentsSeparatedByString(sql, "\n");
		return leftHandSide + "\n   " + lines.componentsJoinedByString("\n   ") + "\n" + rightHandSide;
	}

	/**
	 * Returns the array containing the EOAttributes that ERXQuery used to
	 * fetch the results. This must be called after the results have been
	 * fetched or after calling the getExpression(editingContext) method.
	 * These attributes normally includes attributes specified by the
	 * select() or the groupBy() methods.
	 * 
	 * @return attributes that are fetched by this query
	 */
	public NSArray<EOAttribute> selectAttributes() {
		return selectAttributes;
	}

	//
	// HELPER PRIVATE METHODS
	//
	
	protected void computeSelectAndGroupingAttributes() {
		// Initialize arrays for storing the select attributes, 
		// grouping attributes and sort orderings
		selectAttributes = new NSMutableArray<>(20);
		groupingAttributes = new NSMutableArray<>(20);
		
		// This keeps track of EOAttribute objects used
		attributesByName = new NSMutableDictionary<>();
		
		// This is a set of RelationshipKeyInfo objects that keeps track
		// of fetch keys encountered that correspond to relationships,
		// i.e. "customer.shippingAddress".  The RelationshipKeyInfo stores
		// the relationship key path, i.e. "customer.shippingAddress" as the
		// foreign key path, i.e. "customer.shippingAddressID" and the
		// entity of the destination enterprise object.
		relationshipKeysSet = new NSMutableSet<>();
		
		// Add attributes to select
		for (EOAttribute a : adHocAttributes) {
			selectAttributes.addObject(a);
			// Keep track of which ones we've used
			attributesByName.setObjectForKey(a, a.name());
		}
		// Keep track of attributes to group by
		for (EOAttribute a : adHocGroupings) {
			groupingAttributes.add(a);
			// Keep track of which ones we've used
			attributesByName.setObjectForKey(a, a.name());
		}

		// Get the EOAttributes for the grouping keys
		for (String key : groupingKeys) {
			EOAttribute eoattribute = existingOrNewAttributeForKey(key);
			groupingAttributes.addObject(eoattribute);
			selectAttributes.addObject(eoattribute);
		} /* for (key : groupings) */
		
		// Make sure orderings are select attributes
		for (EOSortOrdering ordering : orderings) {
			String orderingKey = ordering.key();
			EOAttribute orderingAttribute = null;
			for (EOAttribute a : selectAttributes) {
				if (orderingKey.equals(a.name())) {
					orderingAttribute = a;
					break;
				}
			}
			// If no attribute for this ordering key then create one
			// and add it to the select attributes
			if (orderingAttribute == null) {
				orderingAttribute = existingOrNewAttributeForKey(orderingKey);
				selectAttributes.add(orderingAttribute);
				
				// Now that we added it to the select attributes we have to check
				// to see if it also needs to be added to the groupingAttributes.
				// For example, if we are grouping by "patient" and ordering key
				// is "patient.lastName" then we need to add it to the grouping
				// attributes in order to generate correct SQL.
				for (String gKey : groupingKeys) {
					// Example: if ordering key is "patient.lastName" and grouping key is "patient"
					if (orderingKey.length() > gKey.length() && orderingKey.startsWith(gKey)) {
						groupingAttributes.add(orderingAttribute);
					}
				}
			}
		}
		// Get the EOAttributes for the keys to fetch
		for (String key : fetchKeys) {
			EOAttribute eoattribute = existingOrNewAttributeForKey(key);
			if (selectAttributes.containsObject(eoattribute) == false) {
				selectAttributes.addObject(eoattribute);
			}
		} /* for (key : columns) */

		// If building a counting statement then there are no select attributes but we need to have
		// at least one select attribute in order to build an EOSQLExpression otherwise the 
		// EOSQLExpressionFactory method selectStatementForAttributes() throws an exception.
		if (selectAttributes.count() == 0 && isCountingStatement) {
			selectAttributes = new NSMutableArray<>(mainEntity.primaryKeyAttributes());
		}
	}
	
	/**
	 * Inner class to keep track of relationship keys. Relationships keys
	 * are key paths where all the keys are relationships, i.e. order.customer.
	 * ERXQuery fetches the foreign keys and then creates object faults that it
	 * adds automatically to the row dictionary that it passes in to the record
	 * constructor.
	 */
	private static class RelationshipKeyInfo {
		private String _entityName;
		private String _relationshipKeyPath;
		private String _sourceAttributeKeyPath;

		public RelationshipKeyInfo(String relationshipKeyPath, String sourceAttributeKeyPath, EOEntity entity) {
			this._relationshipKeyPath = relationshipKeyPath;
			this._sourceAttributeKeyPath = sourceAttributeKeyPath;
			this._entityName = entity.name();
		}

		public String entityName() {
			return _entityName;
		}

		public String relationshipKeyPath() {
			return _relationshipKeyPath;
		}

		public String sourceAttributeKeyPath() {
			return _sourceAttributeKeyPath;
		}

		@Override
		public int hashCode() {
			return (_entityName + _relationshipKeyPath + _sourceAttributeKeyPath).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RelationshipKeyInfo) {
				RelationshipKeyInfo relKeyInfo = (RelationshipKeyInfo) obj;
				return relKeyInfo == this || 
						(		relKeyInfo.entityName().equals(_entityName) 
								&& relKeyInfo.relationshipKeyPath().equals(_relationshipKeyPath)
								&& relKeyInfo.sourceAttributeKeyPath().equals(_sourceAttributeKeyPath)
						);
			}
			return false;
		}
	}
	
	/**
	 * Called by getExpression() to get the attribute or create an
	 * ad-hoc attribute for a key. The keyPath can be relationship key path,
	 * i.e. "customer.shippingAddress". This method keeps track of ad hoc
	 * attributes created so that if it gets called with the same key path
	 * it returns the previously created attribute.
	 * 
	 * @param keyPath a key path
	 * 
	 * @return corresponding attribute
	 */
	protected EOAttribute existingOrNewAttributeForKey(String keyPath) {
		// ERXQuery.destinationProperty() below returns the property corresponding
		// to the last component in the key path.  This could be either an EOAttribute
		// or an EORelationship. For example, "customer.shippingAddress" returns the
		// shippingAddress relationship.  On the other hand, a key path of "customer.birthDate"
		// would return the birthDate EOAttribute from the Customer entity.
		EOProperty eoproperty = ERXQuery.destinationProperty(mainEntity, keyPath);
		if (eoproperty == null) {
			throw unknownPropertyException(keyPath);
		}
		// Parse the keys in the key path
		NSMutableArray<String> keys = new NSMutableArray<>(keyPath.split("\\."));
		
		// If destination property is an EOAttribute
		if (eoproperty instanceof EOAttribute) {
			EOAttribute eoattribute = null;
			if (keys.count() == 1) {
				// If key path contains a single key, i.e. "birthDate" then the attribute is
				// the destination property, which is just the existing attribute on
				// the main entity.
				eoattribute = (EOAttribute) eoproperty;
				attributesByName.setObjectForKey(eoattribute, eoattribute.name());
			} else {
				// The key path has multiple keys, i.e. "patient.birthDate", so let's create
				// an ad hoc attribute to reach the destination attribute.
				String attributeName = keyPath;
				String definition = keyPath;
				EOAttribute destinationAttribute = (EOAttribute) eoproperty;
				eoattribute = attributesByName.objectForKey(attributeName);
				if (eoattribute == null) {
					eoattribute = ERXQueryEOAttribute.create(mainEntity, attributeName, definition, destinationAttribute);
					attributesByName.setObjectForKey(eoattribute, attributeName);
				}
			}
			return eoattribute;
		}
		
		// Else destination property is an EORelationship
		EORelationship eorelationship = (EORelationship) eoproperty;
		
		// keyPath is a relationship key path
		String relationshipKeyPath = keyPath;
		
		// However, for the query we need to fetch the foreign key which we will later use
		// to create the enterprise object fault from it.
		EOAttribute sourceAttribute = eorelationship.sourceAttributes().lastObject();
		
		// Compute the key path to the relationship's source attribute 
		String sourceAttributeKeyPath;
		
		// if the key path has a single key, i.e. "customer" then simply use the existing
		// source attribute from the relationship, i.e. "customerID".  On the other hand
		// if key path has multiple keys, i.e. "customer.shippingAddress" then compute
		// the source attribute key path, i.e. "customer.shippingAddressID" and create
		// an ad hoc attribute using the source attribute key path as the name of the
		// attribute and the definition. 
		keys.removeLastObject();
		EOAttribute eoattribute;
		if (keys.count() == 0) {
			eoattribute = sourceAttribute;
			sourceAttributeKeyPath = sourceAttribute.name();
			attributesByName.setObjectForKey(eoattribute, eoattribute.name());
		} else {
			keys.addObject(sourceAttribute.name());
			sourceAttributeKeyPath = keys.componentsJoinedByString(".");
			String attributeName = sourceAttributeKeyPath;
			String definition = sourceAttributeKeyPath;
			
			// Look to see if one has been created first
			eoattribute = attributesByName.objectForKey(attributeName);
			if (eoattribute == null) {
				eoattribute = ERXQueryEOAttribute.create(mainEntity, attributeName, definition, sourceAttribute);
				attributesByName.setObjectForKey(eoattribute, eoattribute.name());
			}
		}
		
		EOEntity destinationEntity = eorelationship.destinationEntity();
		RelationshipKeyInfo relationshipKeyInfo = new RelationshipKeyInfo(relationshipKeyPath, sourceAttributeKeyPath, destinationEntity);
		relationshipKeysSet.addObject(relationshipKeyInfo);
		
		// Return the ad hoc attribute that we created to fetch the foreign key
		return eoattribute;
	}
	
	protected RuntimeException unknownPropertyException(String keyPath) {
		return new RuntimeException("Unable to obtain property for key path '" 
				+ keyPath + "' starting on the " + mainEntity.name() + " entity.");
	}
	
	/** 
	 * Returns the destination entity for this report attribute. For example, if keyPath is
	 * provider.specialtyCategory then this method would return the SpecialtyCategory entity.
	 * 
	 * @param rootEntity starting entity
	 * @param keyPath a key path
	 * 
	 * @return entity keyPath points to
	 */
	public static EOEntity destinationEntity(EOEntity rootEntity, String keyPath) {
		EOEntity entity = rootEntity;
		StringTokenizer t = new StringTokenizer(keyPath, ".");
		
		while (t.hasMoreTokens()) {
			String key = t.nextToken();
			EORelationship relationship = entity.anyRelationshipNamed(key);
			if (relationship != null) {
				entity = relationship.destinationEntity();
			}
		}
		
		return entity;
	}
	
	/**
	 * Returns whether the property (either EOAttribute or EORelationship) referenced by
	 * the last component in the key path.
	 * 
	 * @param rootEntity starting entity
	 * @param keyPath a key path
	 * 
	 * @return property keyPath points to
	 */
	public static EOProperty destinationProperty(EOEntity rootEntity, String keyPath) {
		EOEntity entity = rootEntity;
		String[] keys = keyPath.split("\\.");
		EOAttribute attribute = null;
		EORelationship relationship = null;
		
		for (String key : keys) {
			relationship = entity.anyRelationshipNamed(key);
			if (relationship != null) {
				entity = relationship.destinationEntity();
				attribute = null;
			} else {
				attribute = entity.anyAttributeNamed(key);
			}
		}
		
		if (attribute != null) {
			return attribute;
		} else if (relationship != null) {
			return relationship;
		} else {
			return null;
		}
	}
	
	/**
	 * Returns a new EOSQLExpressionFactory for the entity and editing context specified.
	 * 
	 * @param anEntity an entity
	 * @param ec an editing context
	 * 
	 * @return expression factory for given entity
	 */
	protected static EOSQLExpressionFactory sqlExpressionFactory(EOEntity anEntity, EOEditingContext ec) {
		EOModel model = anEntity.model();
		EODatabaseContext databaseContext = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
		return databaseContext.adaptorContext().adaptor().expressionFactory();
	}
	
	/** 
	 * Returns a qualifier by combining the restricting qualifiers (if any) in
	 * the entities referenced. The resulting qualifier is rooted at mainEntity.
	 * When this method is called the editingContext, mainEntity, mainSelectQualifier
	 * and selectAttributes i-vars must be set.
	 * 
	 * @return the restricting qualifier
	 */
	protected EOQualifier restrictingQualifierForReferencedEntities() {
		// Get expression similar to what will be used to build the SQL
		EOFetchSpecification spec = new EOFetchSpecification(mainEntity.name(), mainSelectQualifier, null);
		EOSQLExpression e = sqlExpressionFactory(mainEntity, editingContext).selectStatementForAttributes(selectAttributes, false, spec, mainEntity);
		
		// Array to hold the restricting qualifiers for each referenced entity 
		NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<>();
		
		// See what relationship paths are being traversed and check for
		// destination entities having a restricting qualifier.  The
		// aliasesByRelationshipPath().allKeys() returns an array of
		// strings like this:
		// ("", "claimWorkflowReasons", "claimWorkflowReasons.workflowReason")
		//
		NSArray<String> relationshipPaths = e.aliasesByRelationshipPath().allKeys();
		for (String relationshipPath : relationshipPaths) {
			EOEntity destinationEntity = null;
			if (relationshipPath.length() == 0) continue;
			destinationEntity = ERXQuery.destinationEntity(mainEntity, relationshipPath);
			EOQualifier restrictingQualifier = destinationEntity.restrictingQualifier();
			if (restrictingQualifier != null) {
				ERXKey<Object> relationshipPathKey = new ERXKey<>(relationshipPath);
				qualifiers.addObject(relationshipPathKey.prefix(restrictingQualifier));
			}
		}
		
		if (qualifiers.count() == 1) {
			return qualifiers.objectAtIndex(0);
		} else if (qualifiers.count() > 1) {
			return ERXQ.and(qualifiers);
		}
		
		return null;
	}
	
	/**
	 * Returns the SQL string corresponding to attribute a. The EOSQLExpression e
	 * must correspond to the expression being built to which must include attribute
	 * a as one of the select the attributes.
	 * 
	 * @param e an SQL expression
	 * @param a attribute for SQL string
	 * 
	 * @return SQL string
	 */
	protected String sqlStringForAttribute(EOSQLExpression e, EOAttribute a) {
		String readFormat = a.readFormat();
		if (readFormat != null) {
			return e.formatSQLString(e.sqlStringForAttribute(a), readFormat);
		}
		String attrSql = e.sqlStringForAttribute(a);
		
		// Strip out any column aliases hardcoded into the attribute name.
		// Note that, to be stripped, the AS keyword, in caps, must be used.
		int i = attrSql.indexOf(" AS ");
		if (i > 0) {
			return attrSql.substring(0, i);
		}
		
		return attrSql;
	}
	
	/**
	 * Returns the SQL string for the ordering of attribute a using the specified selector.
	 * The EOSQLExpression e must correspond to the expression being built which must include
	 * attribute a as one of the select attributes.
	 * 
	 * @param e an SQL expression
	 * @param orderingAttribute attribute to sort by
	 * @param selector a sort ordering selector to use
	 * 
	 * @return SQL string
	 */
	public String sqlStringForOrderingAttribute(EOSQLExpression e, EOAttribute orderingAttribute, NSSelector selector) {
		EOAttribute a = orderingAttribute;
		String sqlString = sqlStringForAttribute(e, a);
		
		if (selector == EOSortOrdering.CompareCaseInsensitiveAscending) {
			if (a.adaptorValueType() == 1) {
				return "UPPER(" + sqlString + ") ASC";
			}
			return sqlString + " ASC";
			
		} else if (selector == EOSortOrdering.CompareCaseInsensitiveDescending) { 
			if (a.adaptorValueType() == 1) {
				return "UPPER(" + sqlString + ") DESC";
			}
			return sqlString + " DESC";
			
		} else if (selector == EOSortOrdering.CompareAscending) {
			return sqlString + " ASC";
		} else if (selector == EOSortOrdering.CompareDescending) {
			return sqlString + " DESC";
		} else {
			return "(" + sqlString + ")";
		}
	}
	
	
	/**
	 * Returns the SQL for the EOSQLExpression specified but with the place holder 
	 * characters (?) replaced with their corresponding value from the bindings and
	 * formatted for in-line use.
	 * 
	 * @param sql SQL string to convert
	 * @param expression an SQL expression
	 * 
	 * @return SQL string
	 */
	protected String sqlWithBindingsInline(String sql, EOSQLExpression expression) {
		Pattern p = Pattern.compile("('[^']*')|(([,]?+)([\\\\?]{1}+))");
		Matcher m = p.matcher(sql);
		StringBuffer inlineSql = new StringBuffer();
		
		NSArray<NSDictionary<String,?>> bindVariableDictionaries = expression.bindVariableDictionaries();
		for (NSDictionary<String,?> binding : bindVariableDictionaries) {
			// Get the binding attribute, value and formatted value for inline use 
			EOAttribute attribute = (EOAttribute) binding.objectForKey(EOSQLExpression.BindVariableAttributeKey);
			Object value = binding.objectForKey(EOSQLExpression.BindVariableValueKey);
			String formattedValue = formatValueForAttributeForInlineUse(expression, value, attribute);
			
			// Search until placeholder is replaced
			
			while (m.find()) {
				String singleQuoteLiteral = m.group(1);
				if (singleQuoteLiteral != null) {
					// Append single quote literal which may include a ? character and
					// continue to look for a legitimate ? placeholder character for 
					// the binding's value
					m.appendReplacement(inlineSql, Matcher.quoteReplacement(singleQuoteLiteral));
				} else {
					// A legitimate ? placeholder character was found which may be optionally
					// preceded with a comma.  Put the comma back in there if any ($3) followed
					// by the formatted binding value.
					String replacement = "$3" + Matcher.quoteReplacement(formattedValue);
					m.appendReplacement(inlineSql, replacement);
					break; // <--- EXIT: We're done searching/replacing the placeholder for this binding
				}
			}
		}
		m.appendTail(inlineSql);
		
		return inlineSql.toString();
	}
	
	/**
	 * Returns the SQL for the EOSQLExpression specified but with the place holder 
	 * characters (?) replaced with their corresponding value from the bindings and
	 * formatted for inline use.
	 * 
	 * @param sql SQL string to convert
	 * @param expression an SQL expression
	 * 
	 * @return SQL string
	 */
	protected String sqlWithBindingsInline2(String sql, EOSQLExpression expression) {
		StringBuilder newSql = new StringBuilder(sql.length() + 100);
		
		NSArray<NSDictionary<String,?>> bindVariableDictionaries = expression.bindVariableDictionaries();
		
		char chars[] = sql.toCharArray();
		int offset = 0;
		
		for (NSDictionary<String,?> binding : bindVariableDictionaries) {
			
			// Get the binding value and attribute
			Object value = binding.objectForKey(EOSQLExpression.BindVariableValueKey);
			EOAttribute attribute = (EOAttribute) binding.objectForKey(EOSQLExpression.BindVariableAttributeKey);
			
			// Format the value for the binding
			
			String formattedValue = formatValueForAttributeForInlineUse(expression, value, attribute);
			
			// Append sql up to the to bind variable place holder
			while (offset < chars.length && chars[offset] != '?') {
				newSql.append(chars[offset]);
				offset++;
			}
			
			// Now append the formatted value instead of the bind variable place holder
			
			newSql.append(formattedValue);
			offset++;
		}
		
		// Now append the remaining sql
		
		while (offset < chars.length) {
			newSql.append(chars[offset]);
			offset++;
		}
		
		return newSql.toString();
	}
	
	/**
	 * Uses the EOSQLExpression provided to get the SQL string for value and
	 * corresponding attribute. This method is similar to EOSQLExpression's 
	 * sqlStringForValue(Object value, String keyPath) but this one does not
	 * attempt to get to the attribute from the key path as we already have
	 * the attribute.
	 * 
	 * @param e an SQL expression
	 * @param att an attribute
	 * @param value value to use with attribute
	 * 
	 * @return SQL string
	 */
	public String sqlStringForAttributeValue(EOSQLExpression e, EOAttribute att, Object value) {
		if (value != NSKeyValueCoding.NullValue 
				&& (((e.useBindVariables()) && (e.shouldUseBindVariableForAttribute(att))) || (e.mustUseBindVariableForAttribute(att)))) {
			
			NSMutableDictionary<String, Object> binding = e.bindVariableDictionaryForAttribute(att, value);
			e.addBindVariableDictionary(binding);
			return (String)binding.objectForKey("BindVariablePlaceholder");
		}
		return e.formatValueForAttribute(value, att);
	}
	
	/**
	 * Formats the value for inline use. For example, the string "I'm smart" would be formatted
	 * as "'I''m smart'". Similarly a NSTimestamp value would be converted to something like
	 * this "TO_DATE('1967-12-03 00:15:00','YYYY-MM-DD HH24:MI:SS')". Supported values are null,
	 * String, NSTimestamp, Boolean, Integer, Number. Other values are converted
	 * by calling toString().
	 * 
	 * @param sqlExpression an SQL expression
	 * @param value value to use with attribute
	 * @param attribute an attribute
	 * 
	 * @return SQL string
	 */
	protected String formatValueForAttributeForInlineUse(EOSQLExpression sqlExpression, Object value, EOAttribute attribute) {
		String formattedValue;
		
		if (value == null || value == NSKeyValueCoding.NullValue) {
			formattedValue = "NULL";
		} else {
			// First try to see if the EOSQLExpression's formatValueForAttribute()
			// knows how to format the value for the corresponding attribute
			formattedValue = sqlExpression.formatValueForAttribute(value, attribute);
			
			// If the formattedValue is "NULL" then the formatValueForAttribute() did not
			// do its job and we'll do the best we can here
			if (formattedValue == null || formattedValue.equals("NULL")) {
				if (value instanceof String) {
					formattedValue = sqlExpression.formatStringValue((String)value);
				} else if (value instanceof NSTimestamp) {
					NSTimestamp timestamp = (NSTimestamp) value;
					formattedValue = formattedTimestampForInlineUse(sqlExpression, timestamp, attribute);
				} else if (value instanceof Boolean) {
					boolean boolValue = (Boolean) value;
					// If stored in the database as a string then format as string
					// otherwise format as a number 1 or 0.
					if (attribute.externalType().toLowerCase().contains("char")) {
						formattedValue = "'" + boolValue +"'";
					} else if (boolValue) {
						formattedValue = EOSQLExpression.sqlStringForNumber(1);
					} else {
						formattedValue = EOSQLExpression.sqlStringForNumber(0);
					}
				} else {
					formattedValue = value.toString();
				}
			}
		}
		
		log.debug("{} formatted value {} for inline use as {}", this.getClass().getSimpleName(), value, formattedValue);
		
		return formattedValue;
	}
	
	protected String databaseProductName(EOEntity entity) {
		JDBCAdaptor adaptor = (JDBCAdaptor) EOAdaptor.adaptorWithModel(entity.model());
		JDBCPlugIn plugin = adaptor.plugIn();
		return plugin.databaseProductName().toLowerCase();
	}
	
	protected String formattedTimestampForInlineUse(EOSQLExpression sqlExpression, NSTimestamp timestamp, EOAttribute attribute) {
		EOEntity entity = attribute.entity();
		String databaseProductName = databaseProductName(entity);
		//NSTimestampFormatter formatter = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedValue = formatter.format(timestamp);
		
		// Only oracle has been tested - the rest were guessed by searching
		// the web on how to convert string to date / timestamp in <<databaseProductName>>
		
		// Now wrap the formatted value with the string-to-date function
		// corresponding to the database product being used
		switch (databaseProductName) {
		case "oracle":
			// See http://docs.oracle.com/cd/B19306_01/server.102/b14200/functions183.htm
			formattedValue = "TO_DATE('" + formattedValue + "', 'YYYY-MM-DD HH24:MI:SS')";
		case "postgresql":
			// See https://www.postgresql.org/docs/7.4/static/functions-formatting.html
			formattedValue = "TO_DATE('" + formattedValue + "', 'YYYY-MM-DD HH24:MI:SS')";
			break;
		case "mysql":
			// See https://dev.mysql.com/doc/refman/5.5/en/date-and-time-functions.html#function_str-to-date
			formattedValue = "STR_TO_DATE('" + formattedValue + "', '%Y-%m-%d %H:%i:%s')";
			break;
		case "h2":
			// See from http://www.h2database.com/html/functions.html
			formattedValue = "PARSEDATETIME('" + formattedValue + "', '" + formatter.toPattern() + "')";
			break;
		case "derby":
			// I got this from http://community.teradata.com/t5/UDA/convert-varchar-to-timestamp/td-p/32302
			formattedValue = "CAST('" + formattedValue + "' AS TIMESTAMP(0) FORMAT 'YYYY-MM-DDBHH:MI:SS')";
			break;
		case "openbase":
		case "frontbase":
			// This is a wild guess.... I did not find anything on the web for these two database products
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			formattedValue = "'" + formatter.format(timestamp) + "'";
			break;
		case "microsoft":
			// See https://msdn.microsoft.com/en-us/library/ms180878(SQL.100).aspx#ISO8601Format
			// See https://msdn.microsoft.com/en-us/library/ms187928(v=sql.90).aspx
			// See http://stackoverflow.com/questions/207190/sql-server-string-to-date-conversion
			formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			formattedValue = "CAST('" + formattedValue + "' AS datetime2)";
			break;
		default:
			throw new RuntimeException(
					"Please add support to ERXQuery's for formatting NSTimestamp values for database product " 
					+ databaseProductName
				);
		}

		return formattedValue;
	}


	//
	// The RecordConstructor functional interface
	//


	/**
	 * Functional interface for constructing a record from a dictionary
	 * with the data fetched from the database.
	 * 
	 * @param <T> class type that is used to convert a fetched record into a Java object
	 */
	public static interface RecordConstructor<T> {
		public abstract T constructRecord(EOEditingContext ec, NSMutableDictionary<String,Object> row);
	}
	
	/**
	 * This is the default constructor used by the fetch() and fetch(EOEditingContext)
	 * methods. It simply returns the row dictionary passed in.
	 */
	public static class DefaultRecordConstructor implements RecordConstructor<NSDictionary<String,Object>> {
		@Override
		public NSDictionary<String, Object> constructRecord(EOEditingContext ec, NSMutableDictionary<String, Object> row) {
			return row;
		}
	}



	/**
	 * Convenience class for fetching data into records conforming to key value coding.  What is
	 * special about this record is its handling of key-value-coding.  For example, when fetching
	 * keys that represent a key path to another object, i.e. "a.b.c", then the records fetched
	 * end up with C objects as values for the key named "a.b.c".  If you use regular dictionaries
	 * to fetch these records then you would have to use valueForKey() or objectForKey() to retrieve
	 * the C object stored under the key "a.b.c".  You would then be able to apply a key path to C.
	 * For example, instead of this boilerplate:
	 * 
	 * <pre>
	 * String name = "";
	 * C c = null;
	 * Object value = rec.objectForKey("a.b.c");
	 * if (value != NSKeyValueCoding.NullValue) {
	 *     c = (C) value;
	 *     name = (String) c.valueForKeyPath("x.y.name");
	 * }
	 * </pre>
	 * 
	 * you can use regular valueForKeyPath() like this:
	 * 
	 * <pre>
	 * String name = (String) rec.valueForKeyPath("a.b.c.x.y.name");
	 * </pre>
	 * 
	 * Values of NSKeyValueCoding.NullValue are automatically translated to null and you can use
	 * the valueForKeyPathWithDefault() method to translate null to a default value and get rid of
	 * casting the value like this:
	 * 
	 * <pre>
	 * String name = rec.valueForKeyPathWithDefault("a.b.c.x.y.name", "");
	 * </pre>
	 */
	public static class Record implements NSKeyValueCoding, NSKeyValueCodingAdditions {
		private NSMutableDictionary<String,Object> data;
		private String[] _sortedKeys;
		
		public Record(EOEditingContext context, NSMutableDictionary<String,Object> row) {
			data = row;
		}
		
		private String[] sortedKeys() {
			if (_sortedKeys == null) {
				Object[] objs = data.allKeys().toArray();
				String[] keys = new String[objs.length];
				for (int i = 0; i < objs.length; i++) {
					keys[i] = objs[i].toString();
				}
				// Sort by key length with longer keys first, i.e. "a.b.c.d", "x.y", etc.
				// This is necessary for the smart algorithm in valueForKeyPath() where
				// multiple keys in the key path are a single key in the data dictionary
				// and the value returned from the dictionary gets applied the remaining
				// key path using valueForKeyPath. 
				Arrays.sort(keys, (a,b) -> b.length() - a.length());
				
				_sortedKeys = keys;
			}
			return _sortedKeys;
		}
		
		//
		// NSKeyValueCoding
		//
		
		@Override
		public void takeValueForKey(Object value, String key) {
			if (value == null) {
				data.setObjectForKey(NSKeyValueCoding.NullValue, key);
			} else {
				data.setObjectForKey(value, key);
			}
		}

		@Override
		public Object valueForKey(String key) {
			Object value = data.objectForKey(key);
			
			// Translate NullValue placeholder to java null
			if (value == NSKeyValueCoding.NullValue) {
				return null;
			}
			
			return value;
		}
		
		//
		// NSKeyValueCodingAdditions
		//
		
		@Override
		public void takeValueForKeyPath(Object value, String keyPath) {
			for (String aKey : sortedKeys()) {
				if (aKey.length() < keyPath.length() && keyPath.startsWith(aKey)) {
					Object obj = valueForKey(aKey);
					String remainingKeyPath = keyPath.substring(aKey.length() + 1);
					NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(obj, value, remainingKeyPath);
					return;
				}
			}
			String key = keyPath;
			takeValueForKey(value, key);
		}
		
		@Override
		public Object valueForKeyPath(String keyPath) {
			for (String aKey : sortedKeys()) {
				if (aKey.length() < keyPath.length() && keyPath.startsWith(aKey)) {
					Object obj = valueForKey(aKey);
					String remainingKeyPath = keyPath.substring(aKey.length() + 1);
					Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, remainingKeyPath);
					if (value == NSKeyValueCodingAdditions.NullValue) {
						value = null;
					}
					return value;
				}
			}
			return valueForKey(keyPath);
		}
		
		// Convenience method
		
		@SuppressWarnings("unchecked")
		public <V> V valueForKeyPathWithDefault(String keyPath, V defaultValue) {
			Object v = valueForKeyPath(keyPath);
			if (v == null) {
				return defaultValue;
			}
			return (V) v;
		}
		
		@Override
		public String toString() {
			return "<" + ERXQuery.class.getSimpleName() + "." + getClass().getSimpleName() 
					+ ": " + data + ">";
		}
	}

	//
	// The EntityModificationAction class
	//

	public static abstract class EntityModificationAction {
		
		protected abstract void modifyEntity(EOEntity entity);
		
		public void run(EOEditingContext ec, EOEntity entity) {
			ec.lock();
			try {
				String modelName = entity.model().name();
				EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);
				dbc.lock();
				try {
					 modifyEntity(entity);
				} finally {
					dbc.unlock();
				}
			} finally {
				ec.unlock();
			}
		}
	}


	// Statistics

	public double queryEvaluationTime() {
		return queryEvaluationTime;
	}

}
