import java.io.*;
import absyn.*;
import Symbol.SemanticAnalyzer;
import Symbol.SymbolTable;

class Main {
    public final static boolean SHOW_TREE = true;
    public final static boolean RUN_SEMANTIC_ANALYSIS = true;

    public static void main(String argv[]) {
        try {
            System.out.println("Starting parser on file: " + argv[0]);

            parser p = new parser(new Lexer(new FileReader(argv[0])));
            Absyn result = (Absyn) (p.parse().value);

            if (result != null) {
                if (SHOW_TREE) {
                    AbsynVisitor visitor = new ShowTreeVisitor();
                    result.accept(visitor, 0);
                }

                if (RUN_SEMANTIC_ANALYSIS) {
                    System.out.println("\nStarting Semantic Analysis...");
                    SemanticAnalyzer analyzer = new SemanticAnalyzer();
                    analyzer.analyze((DecList) result);
                    System.out.println("Semantic Analysis Completed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
