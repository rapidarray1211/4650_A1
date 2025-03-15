import java.io.*;
import absyn.*;
import semantic_analyzer.SemanticAnalyzer;
import semantic_analyzer.SymbolTable;
import semantic_analyzer.AnalyzerPrinter;

class Main {
    public static final boolean SHOW_TREE = true;
    public static final boolean RUN_SEMANTIC_ANALYSIS = true;

    public static void main(String[] argv) {
        int symbolCount;
        AnalyzerPrinter aPrinter = new AnalyzerPrinter("out.txt", false);


        try {
            System.out.println("Starting parser on file: " + argv[0]);

            parser p = new parser(new Lexer(new FileReader(argv[0])));
            Absyn result = (Absyn) (p.parse().value);

            if (result != null) {
                if (SHOW_TREE) {
                    AbsynVisitor visitor = new ShowTreeVisitor();
                    result.accept(visitor, 0);
                    symbolCount = visitor.getSymbolCount();
                }

                if (RUN_SEMANTIC_ANALYSIS) {
                    System.out.println("\nStarting Semantic Analysis...");
                    SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolCount,aPrinter);
                    analyzer.analyze((DecList) result);
                    System.out.println("Semantic Analysis Completed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
