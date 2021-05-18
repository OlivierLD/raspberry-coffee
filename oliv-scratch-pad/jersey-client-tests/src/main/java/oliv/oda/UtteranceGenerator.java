package oliv.oda;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * "lower Sam's auto approval limit to $50"
 * "auto approve anything where the justification mentions 'Bacme'"
 * "disable auto-approval if my remaining budget is less than $10,000."
 *
 * "what is my auto approval limit?"
 *      "how much is my auto approval limit"
 *      "tell me my auto approval limit"
 *      "get my auto approval limit"
 *      "show my auto approval limit"
 *      "show me my auto approval limit"
 *      "what is my auto approval limit"
 *      "my auto approval limit"
 *      "auto approval limit"
 * "what is my auto approval limit for Sam?"
 * "what is the policy when expense amount is $80?"
 *
 * Decision Table columns:
 * IN -
 * Expense.Approver
 * Expense.Approvee
 * Expense.Employee Level
 * Expense.Remaining Budget
 * Expense.Amount
 * Expense.Type (Travel, Procurement, Onboarding)
 * Manager Justified (true|false)
 * OUT -
 * Strategy (Approve, Review, Reject)
 * Reason
 */
public class UtteranceGenerator {

    private final static String CVS_TAB_SEPARATOR = "\t";
    private final static String CVS_COMMA_SEPARATOR = ",";

    private static Function<String, String> quoteEscaper = in -> {
        String out = "\"" +
                in.replace("\"", "\"\"")
                        .replace(",", "\",\"") +
                "\"";
        return out;
    };

    private static Function<String, String> escaper = quoteEscaper;
//    private static Function<String, String> escaper = in -> in;

    private static String cvsSeparator = CVS_COMMA_SEPARATOR;
//    private static String cvsSeparator = CVS_TAB_SEPARATOR;

    private final static String OUTPUT_FILE_NAME = "utterances_v2.csv";
//    private final static String OUTPUT_FILE_NAME = "utterances_v1.csv";

    public static class Utterance {
        String mainUtterance;
        String[] parameters;

        String[] synonyms;

        String translation;

        public Utterance() {}
        public Utterance mainUtterance(String mainUtterance) {
            this.mainUtterance = mainUtterance;
            return this;
        }
        public Utterance parameters(String[] parameters) {
            this.parameters = parameters;
            return this;
        }
        public Utterance synonyms(String[] synonyms) {
            this.synonyms = synonyms;
            return this;
        }
        public Utterance translation(String translation) {
            this.translation = translation;
            return this;
        }
    }

    public static List<Utterance> setData() {
        Utterance utterance_01 = new Utterance()
                .mainUtterance("what is my auto approval limit?")
                .parameters(new String[] {})
                .synonyms(new String[] {
                        "how much is my auto approval limit",
                        "tell me my auto approval limit",
                        "get my auto approval limit",
                        "show my auto approval limit",
                        "show me my auto approval limit",
                        "what is my auto approval limit",
                        "my auto approval limit",
                        "auto approval limit",
                })
                .translation("{ \"select\": [ \"Expense.Approvee\", \"Expense.Employee Level\", \"Expense.Remaining Budget\", \"Expense.Amount\", \"Expense.Type\" ], \"where\": { \"Strategy\": \"Approve\" } }");

        Utterance utterance_02 = new Utterance()
                .mainUtterance("what is my auto approval limit for {APPROVEE}?")
                .parameters(new String[] { "{APPROVEE}" })
                .synonyms(new String[] {
                        "how much is my auto approval limit for {APPROVEE}",
                        "tell me my auto approval limit for {APPROVEE}",
                        "get my auto approval limit for {APPROVEE}",
                        "get auto approval limit for {APPROVEE}",
                        "show my auto approval limit for {APPROVEE}",
                        "show me my auto approval limit for {APPROVEE}",
                        "show auto approval limit for {APPROVEE}",
                        "what is my auto approval limit for {APPROVEE}",
                        "my auto approval limit for {APPROVEE}",
                        "auto approval limit for {APPROVEE}",
                })
                .translation("{ \"select\": [ \"Expense.Employee Level\", \"Expense.Remaining Budget\", \"Expense.Amount\", \"Expense.Type\" ], \"where\": { \"Expense.Approvee\": \"{APPROVEE}\", \"Strategy\": \"Approve\" } }");

        Utterance utterance_03 = new Utterance()
                .mainUtterance("what is the policy when expense amount is ${AMOUNT}?")
                .parameters(new String[] { "{AMOUNT}" })
                .synonyms(new String[] {
                        "get the policy for amount ${AMOUNT}",
                        "show me policy for amount ${AMOUNT}"
                })
                .translation("{ \"select\": [ \"Strategy\" ], \"where\": { \"Expense.Amount\": {AMOUNT} } } }");

        Utterance utterance_04 = new Utterance()
                .mainUtterance("set my auto approval limit to ${NEW_AMOUNT}")
                .parameters(new String[] { "{NEW_AMOUNT}" })
                .synonyms(new String[] {
                        "update my auto approval limit to ${NEW_AMOUNT} for all my requests",
                        "set my auto approval limit to ${NEW_AMOUNT} for all my requests"
                })
                .translation("{ \"upsert\": { \"Expense.Amount\": {NEW_AMOUNT} }, \"where\": { \"Strategy\": \"Approve\" } }");

        Utterance utterance_05 = new Utterance()
                .mainUtterance("set my auto approval limit to ${NEW_AMOUNT} for {APPROVEE}")
                .parameters(new String[] { "{APPROVEE}", "{NEW_AMOUNT}" })
                .synonyms(new String[] {
                        "lower {APPROVEE}'s auto approval limit to ${NEW_AMOUNT}",
                        "update auto approval limit to ${NEW_AMOUNT} for {APPROVEE}",
                        "auto approve anything less than ${NEW_AMOUNT} for {APPROVEE}",
                        "auto approve anything from {APPROVEE} under ${NEW_AMOUNT}"
                })
                .translation("{ \"upsert\": { \"Expense.Amount\": {NEW_AMOUNT} }, \"where\": { \"Expense.Approvee\": \"{APPROVEE}\", \"Strategy\": \"Approve\" } }");

        Utterance utterance_06 = new Utterance()
                .mainUtterance("pause all auto approvals if I’m over {BUDGET}% of my budget")
                .parameters(new String[] { "{BUDGET}" })
                .synonyms(new String[] {
                        "I like to review all requests once I have exceeded {BUDGET}% of the budget",
                        "manual approval for all requests past {BUDGET}% of my budget",
                        "stop auto approvals past {BUDGET}% of budget allocation"
                })
                .translation("{ \"upsert\": { \"Strategy\": \"Review\" }, \"where\": { \"Expense.Remaining Budget\": {BUDGET} } }");

        Utterance utterance_07 = new Utterance()
                .mainUtterance("Start auditing all my approvals exceeding ${APP_AMOUNT} after {BUDGET}% of the org budget is used")
                .parameters(new String[] { "{APP_AMOUNT}", "{BUDGET}" })
                .synonyms(new String[] {})
                .translation("{ \"upsert\": { \"Strategy\": \"Audit\" }, \"where\": { \"Expense.Remaining Budget\": {BUDGET}, \"Expense.Amount\": {APP_AMOUNT} } }");

        return Arrays.asList(utterance_01,
                utterance_02,
//                utterance_03,
                utterance_04,
                utterance_05,
                utterance_06,
                utterance_07);
    }

    private static void replaceInString(PrintStream out, String original, List<String> prmNames, List<List<String>> prmValues) {

        final int idx = 0;
        List<String> valueListForThisPrm = prmValues.get(idx);

        valueListForThisPrm.forEach(val -> {
//                System.out.println("Patching [" + original + "], prmIdx = " + idx + " prmSize:" + prmNames.size() +
//                        ", patching " + prmNames.get(idx) + " with " + val);
            String patch = original.replace(prmNames.get(idx), val);
//                System.out.println("Patch [" + patch + "] prmIdx = " + idx + " prmSize:" + prmNames.size() + ", patching " + prmNames.get(idx));
            if (prmNames.size() > 1) {
                List<String> newPrmNames = prmNames.subList(1, prmNames.size());
                List<List<String>> newPrmValues = prmValues.subList(1, prmValues.size());
                replaceInString(out, patch, newPrmNames, newPrmValues);
            }
            if (prmNames.size() == 1) {
                out.println(patch);
            }
        });
    }

    public static void main__(String... args) {
        String utterance = "set my auto approval limit to ${NEW_AMOUNT} for {APPROVEE}";
        List<String> prmNames = Arrays.asList("{NEW_AMOUNT}", "{APPROVEE}");
        List<List<String>> prmValues = List.of(
                List.of("50", "100", "110", "120"),
                List.of("Sam", "Phil", "Mark")
        );
        replaceInString(System.out, utterance, prmNames, prmValues);

//        String raw = "{ \"upsert\": { \"Strategy\": \"Audit\" }, \"where\": { \"Expense.Remaining Budget\": {BUDGET}, \"Expense.Amount\": {APP_AMOUNT} } }";
        String raw = "{ \"select\": [ \"Expense.Employee Level\", \"Expense.Remaining Budget\", \"Expense.Amount\", \"Expense.Type\" ], \"where\": { \"Expense.Approvee\": \"{APPROVEE}\", \"Strategy\": \"Approve\" } }";
        System.out.println(raw);
        String reworked = "\"" + raw.replace("\"", "\"\"").replace(",", "\",\"") + "\"";
        System.out.println(reworked);
    }

    public static void main(String... args) throws Exception {

        OutputStream output = new FileOutputStream(OUTPUT_FILE_NAME);
        PrintStream out = new PrintStream(output);
//        PrintStream out = System.out;

        Map<String, List<String>> replacements = Map.of(
                "{APPROVEE}", List.of("Sam", "Phil", "Mark", "John", "Jack"),
                "{AMOUNT}", List.of("80", "81", "82", "83", "84", "85"),
                "{NEW_AMOUNT}", List.of("50", "100", "110", "120"),
                "{APP_AMOUNT}", List.of("1000", "2000", "3000"),
                "{BUDGET}", List.of("10", "15", "20", "25", "30", "53", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90")
        );

        List<Utterance> utterances = setData();

        out.println("Utterance" + cvsSeparator + "JSON Query");

        utterances.forEach(utterance -> {
            String main = utterance.mainUtterance;
            if (utterance.parameters.length == 0) {
                out.println(main + cvsSeparator + escaper.apply(utterance.translation));
            } else {
                List<List<String>> values = new ArrayList<>();
                Arrays.asList(utterance.parameters).forEach(prm -> {
                    if (replacements.get(prm) != null) {
                        values.add(replacements.get(prm));
                    }
                });
                replaceInString(out, main + cvsSeparator + escaper.apply(utterance.translation), Arrays.asList(utterance.parameters), values);
            }
            Arrays.asList(utterance.synonyms).forEach(synonym -> {
                if (utterance.parameters.length == 0) {
                    out.println(synonym + cvsSeparator + escaper.apply(utterance.translation));
                } else {
                    List<List<String>> values = new ArrayList<>();
                    Arrays.asList(utterance.parameters).forEach(prm -> {
                        if (replacements.get(prm) != null) {
                            values.add(replacements.get(prm));
                        }
                    });
                    replaceInString(out, synonym + cvsSeparator + escaper.apply(utterance.translation), Arrays.asList(utterance.parameters), values);
                }
            });
            out.println("");
        });
    }
}
