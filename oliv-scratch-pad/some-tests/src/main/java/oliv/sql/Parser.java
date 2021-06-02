package oliv.sql;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Using Apache Calcite
 * See https://www.codota.com/code/java/methods/org.apache.calcite.sql.parser.SqlParser/parseQuery
 */
public class Parser {

    public static SqlNode parse(String sql) throws SqlParseException {
//        SqlParser.Config config = SqlParser.configBuilder()
//                .setLex(Lex.MYSQL_ANSI)
//                .build();
        SqlParser.Config config = SqlParser.config();
        SqlParser sqlParser = SqlParser.create(sql, config); // parserBuilder.build());
        return sqlParser.parseQuery();
    }

    private static String lPad(String str, String pad, int times) {
        String padded = str;
        for (int i=0; i<times; i++) {
            padded = pad + padded;
        }
        return padded;
    }

    private static void drillDownWhere(SqlBasicCall node, int level) {
        SqlNode[] operands = node.getOperands();
        SqlOperator operator = node.getOperator();
        System.out.println(lPad("+--------------", "  ", level));
        System.out.println(lPad("operator: " + operator.toString(), "  ", level));
        Arrays.stream(operands)
                .forEach(op -> {
                    if (op instanceof SqlBasicCall) {
                        drillDownWhere((SqlBasicCall) op, level + 1);
                    } else {
                        System.out.println(lPad("operand: " + op.toString(), "  ", level));
                    }
                });
        System.out.println(lPad("+--------------", "  ", level));
    }

    public static void main(String... args) {

        String sqlStatement = // "select col1, col2 from thisTable where col3 = 'AkeuCoucou'";
                "select \"col1\", \"col2\" from \"thisTable\" where (col3 = 'AkeuCoucou' and col4 < 123) or (col5 is null) ";
//                "update TableOne set col1 = 'A', col2 = 123, col3 = true where col4 = 'AkeuCoucou' ";
//                "upsert into TableOne values (col1 = 'A', col2 = 123, col3 = true) "; //  where col4 = 'AkeuCoucou'
//                "insert into TableOne (col1, col2, col3) values ('A', 12, true)";
//                "delete from TableOne where Akeu = 'Coucou'";
        try {
            SqlNode sqlNode = parse(sqlStatement);
            System.out.printf("Statement Kind: %s\n", sqlNode.getKind().toString());
            System.out.printf("\n", sqlNode);
            Set<SqlKind> select = Arrays.asList(SqlKind.SELECT).stream().collect(Collectors.toSet());

//            if (sqlNode instanceof SqlSelect) {
            if (sqlNode.isA(select)) {
                SqlSelect query = (SqlSelect) sqlNode;
                String from = query.getFrom().toString();
                System.out.println("FROM:" + from);
                SqlNode selectList = query.getSelectList();
                SqlNode where = query.getWhere();
                if (selectList != null && selectList instanceof SqlNodeList) {
                    SqlNodeList sqlNodeList = (SqlNodeList)selectList;
                    sqlNodeList.getList().stream().map(node -> String.format(" -> %s", node)).forEach(System.out::println);
                } else {
                    System.out.println("No select list...");
                }
                if (where != null && where instanceof SqlBasicCall) {
                    SqlBasicCall sqlWhere = (SqlBasicCall) where;
                    System.out.println("WHERE:");
                    drillDownWhere(sqlWhere, 0);
                } else {
                    System.out.println("No where clause...");
                }
            }

            System.out.println("\nDone");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
