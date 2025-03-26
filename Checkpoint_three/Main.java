import java.io.*;
import absyn.*;
import Symbol.SemanticAnalyzer;
import Symbol.SymbolTable;
import Symbol.AnalyzerPrinter;
import codegen.CodeGenerator;

class Main {
	public static final boolean DEBUG = true;
    public static final boolean SHOW_TREE = true;
    public static final boolean RUN_SEMANTIC_ANALYSIS = true;

    public static void main(String[] argv) {
        boolean aArg = false;
        boolean sArg = false;
        boolean cArg = false;

        String aFilename = modifyFileName(argv[0], ".abs");
        String sFilename = modifyFileName(argv[0], ".sym");
        String cFilename = modifyFileName(argv[0], ".tm");

        aArg = checkForArg("-a", argv);
        sArg = checkForArg("-s", argv);
        cArg = checkForArg("-c", argv);

        if (sArg) aArg = true;

        AnalyzerPrinter aPrinter = new AnalyzerPrinter(aFilename, aArg);
        AnalyzerPrinter sPrinter = new AnalyzerPrinter(sFilename, sArg);

        try {
            parser p = new parser(new Lexer(new FileReader(argv[0])));
            Absyn result = (Absyn) (p.parse().value);

            if (result != null) {
                if (SHOW_TREE) {
                    AbsynVisitor visitor = new ShowTreeVisitor(aPrinter);
                    result.accept(visitor, 0);
                    if (aArg) aPrinter.close();
                }

                if (p.valid) {
                    if (RUN_SEMANTIC_ANALYSIS || cArg) {
                        sPrinter.printMsg("\nStarting Semantic Analysis...");
                        SemanticAnalyzer analyzer = new SemanticAnalyzer(sPrinter);
                        analyzer.analyze((DecList) result);
                        sPrinter.printMsg("Semantic Analysis Completed.");
                        if (sArg) sPrinter.close();
                    }

                    if (cArg) {
                        SymbolTable table = new SymbolTable(null);
                        CodeGenerator generator = new CodeGenerator(table, cFilename);
                        generator.generate((DecList) result);
                    }
                } else {
                    System.out.println("Errors were found in parsing, semantic analysis not started.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static boolean checkForArg(String argCheck, String[] argv){
		for (String arg : argv) {
            if(argCheck.equals(arg)){
				System.out.println("Found:" + arg);
				return true;
			}
        }
		return false;
	}

	public static String modifyFileName(String fileName, String Extension){
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex != -1) {
			System.out.println("Changing to file" + fileName.substring(0, dotIndex) + Extension);
            return fileName.substring(0, dotIndex) + Extension;
        } else {
            System.out.println("Could not find file extension.");
			return "";
        }
	}
}
