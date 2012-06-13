/**
 * <p>
 * Classes for handling search queries by using the concept of layers of filters - {@link er.neo4jadaptor.query.Filter} 
 * may filter out some record if it doesn't match search criteria, but may not. Results are passed to another filter
 * which might be more strict than the previous one and so on until we get {@link er.neo4jadaptor.query.Results} exaclty 
 * matching search criteria. Layers are collected in {@link er.neo4jadaptor.query.LayeringFilter} class.
 * </p>
 * 
 * <p>
 * One filter that returns exact results is {@link er.neo4jadaptor.query.neo4j_eval.EvaluatingFilter}, but it's the slowest
 * possible one so it should be used as the last resort, probably after initial filtering using faster methods.
 * </p>
 * 
 * <p>
 * For almost all cases nodes/relationships are evaluated lazily as the client code requests the next record.
 * </p>
 * 
 */
package er.neo4jadaptor.query;