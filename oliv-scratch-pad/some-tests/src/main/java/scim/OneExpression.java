package scim;

public class OneExpression {

    public enum SCIMOperators { // TODO Add IgnoreCase for String ops?
        CO("co"), // Contains
        EQ("eq"), // Equals
        NE("ne"), // Not Equals
        SW("sw"), // Starts With
        PR("pr"), // Present (exists)
        GT("gt"), // Greater than
        GE("ge"), // Greater or equal
        LT("lt"), // Lower than
        LE("le"); // Lower or equal

        private final String op;

        SCIMOperators(String op) {
            this.op = op;
        }

        public String op() {
            return this.op;
        }

        public static SCIMOperators getFromOp(String op) {
            for (SCIMOperators scimOp : SCIMOperators.values()) {
                if (scimOp.op().equals(op)) {
                    return scimOp;
                }
            }
            return null;
        }
    }

    private String field;
    private SCIMOperators op;
    private String value;

    public static class OneExpressionBuilder {
        private OneExpression expression;

        public OneExpressionBuilder() {
            this.expression = new OneExpression();
        }

        public OneExpressionBuilder field(String field) {
            this.expression.field = field;
            return this;
        }

        public OneExpressionBuilder op(String op) {
            this.expression.op = SCIMOperators.getFromOp(op);
            return this;
        }

        public OneExpressionBuilder value(String value) {
            this.expression.value = value;
            return this;
        }

        public OneExpression build() {
            return this.expression;
        }
    }

    public String getField() {
        return this.field;
    }

    public SCIMOperators getOp() {
        return this.op;
    }

    public String getValue() {
        return this.value;
    }

    public static OneExpressionBuilder builder() {
        return new OneExpressionBuilder();
    }

    @Override
    public String toString() {
        return String.format("%s %s%s", field, op.op(), (value == null ? "" : (" " + String.format("'%s'", value))));
    }
}
