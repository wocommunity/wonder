/**
 * Provides classes for performing value conversion between Neo4J {@link org.neo4j.graphdb.PropertyContainer} subtypes and ersatz objects.
 * <code>null</code> values in ersatz objects are not being stored in corresponding {@link org.neo4j.graphdb.PropertyContainer}s and
 * then on value retrieval if some EO attribute is not present then it's assumed to be <code>null</code>. Foreign key values for
 * entities stored using {@link org.neo4j.graphdb.Node} are replaced with Neo4J relationships and then on foreign key value retrieval
 * we simulate its existence by returning that relationship destination ID.
 */
package er.neo4jadaptor.ersatz.neo4j;