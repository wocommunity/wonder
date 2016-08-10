/**
 * Classes for retrieving instances of Neo4J graph databases. 
 * From package client's perspective probably the only class that matters is {@link er.neo4jadaptor.database.pool.DatabasePool} singleton class.
 * Important thing to note is that if one is willing to use Neo4J HA cluster, enterprise version needs to be
 * present on the classpath (<code>neo4j-ha-<i>version</i>.jar</code>), which requires a different licensing.
 */
package er.neo4jadaptor.database.pool;