package er.profiling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.webobjects.eoaccess.EOSQLExpression;

public class PFStatsChecker {
    public static Set<PFStatsNode> checkForErrors(PFStatsNode node) {
        node.clearErrors();

        Set<PFStatsNode> errorNodes = new HashSet<>();
        Map<String, List<PFStatsNode>> duplicates = new HashMap<String, List<PFStatsNode>>();
        checkForDupeSQL(node, duplicates);
        for (Map.Entry<String, List<PFStatsNode>> statement : duplicates.entrySet()) {
            if (statement.getValue().size() > 10) {
                String errorMessage = "repeated " + statement.getValue().size() + " times in this request";
                for (PFStatsNode errorNode : statement.getValue()) {
                    errorNode.addError(errorMessage);
                    errorNodes.add(errorNode);
                }
            }
        }

        return errorNodes;
    }

    protected static void checkForDupeSQL(PFStatsNode node, Map<String, List<PFStatsNode>> duplicates) {
        if ("SQL".equals(node.name()) && "evaluate".equals(node.type())) {
            String statement = ((EOSQLExpression) node.target()).statement();
            List<PFStatsNode> dupes = duplicates.get(statement);
            if (dupes == null) {
                dupes = new LinkedList<>();
                duplicates.put(statement, dupes);
            }
            dupes.add(node);
        }

        List<PFStatsNode> children = node.children();
        if (children != null) {
            for (PFStatsNode child : children) {
                checkForDupeSQL(child, duplicates);
            }
        }
    }

}
