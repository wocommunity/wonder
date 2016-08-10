/**
 * <p>
 * Classes to support {@link er.neo4jadaptor.query.neo4j_eval.EvaluatingFilter} to perform thorough node/relationship evaluation.
 * Main evaluation components are {@link er.neo4jadaptor.query.neo4j_eval.evaluators.Evaluator}s either
 * qualify or reject a candidate (boolean response). {@link er.neo4jadaptor.query.neo4j_eval.evaluators.Evaluator}s can be built
 * from other {@link er.neo4jadaptor.query.neo4j_eval.evaluators.Evaluator}s or can often use {@link er.neo4jadaptor.query.neo4j_eval.retrievers.Retriever}
 * to calculate some value if that value comparison is necessary for the evaluation process.
 * </p>
 * 
 * <p>
 * Evaluating some criteria might be slower or faster than evaluating another criteria, so there's term of evaluating {@link er.neo4jadaptor.query.neo4j_eval.Cost}
 * and {@link er.neo4jadaptor.query.neo4j_eval.HasCost} introduced.
 * </p>
 * 
 */
package er.neo4jadaptor.query.neo4j_eval;