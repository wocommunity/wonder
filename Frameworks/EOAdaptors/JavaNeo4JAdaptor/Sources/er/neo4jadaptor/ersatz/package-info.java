/**
 * Provides classes for performing value conversion between library specific values and neutral java values.
 * 
 * <p>
 * Neo4J adaptor makes use of a few libraries (specifically: Lucene, Neo4J and NextStep collection types) of which
 * each has its own limitations or ways to denote some value being not set (empty). To name some, NSDictionary
 * can't store <code>null</code> value (uses {@link com.webobjects.foundation.NSKeyValueCoding.Null} instead,
 * Neo4J uses no value at all for <code>null</code>, Lucene doesn't support timestamps. 
 * </p>
 * 
 * <p>
 * To work around cross-library communication we introduce record ersatz term which is a map of EO attribute to neutral java value,
 * where numbers are represented by suitable numeric classes, timestamps are represented by subclasses of {@link java.util.Date},
 * <code>null</code>s are represented by <code>null</code>s and ersatz object contains information on which EO attribute values
 * are EMPTY (so <code>null</code> is considered to be some value). Record ersatz are instances of {@link er.neo4jadaptor.ersatz.Ersatz}.
 * </p>
 * 
 * <p>
 * For each library there's custom {@link er.neo4jadaptor.ersatz.Ersatz} implementation that uses custom {@link er.neo4jadaptor.ersatz.Translator} to convert between neutral
 * and library specific values.
 * </p>
 * 
 */
package er.neo4jadaptor.ersatz;