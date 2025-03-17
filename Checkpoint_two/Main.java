import java.io.*;
import absyn.*;
import Symbol.SemanticAnalyzer;
import Symbol.SymbolTable;
import Symbol.AnalyzerPrinter;

class Main {
	public final static boolean DEBUG = false;
    public final static boolean SHOW_TREE = true;
    public final static boolean RUN_SEMANTIC_ANALYSIS = true;
	public static boolean A_ARGUMENT = false;
	public static boolean S_ARGUMENT = false;

    public static void main(String argv[]) {
        try {
			
			A_ARGUMENT = checkForArg("-a", argv);
			S_ARGUMENT = checkForArg("-s", argv);
            System.out.println("Starting parser on file: " + argv[0]);
			

            parser p = new parser(new Lexer(new FileReader(argv[0])));
            Absyn result = (Absyn) (p.parse().value);

            if (result != null) {
				if(DEBUG)
				{
					if (RUN_SEMANTIC_ANALYSIS) {
						if (SHOW_TREE){
							AnalyzerPrinter printer = new AnalyzerPrinter("", false);
							AbsynVisitor visitor = new ShowTreeVisitor(printer);
							result.accept(visitor, 0);
						}
						System.out.println("\nStarting Semantic Analysis...");
						AnalyzerPrinter printer = new AnalyzerPrinter("", false);
						SemanticAnalyzer analyzer = new SemanticAnalyzer(printer);
						analyzer.analyze((DecList) result);
						System.out.println("Semantic Analysis Completed.");
					}
				}
				else
				{
					if(A_ARGUMENT){
						String newFileName = modifyFileName(argv[0], ".abs");
						System.out.println("Sending Syntax Tree Output to file: " + newFileName);
						
						AnalyzerPrinter printer = new AnalyzerPrinter(newFileName, true);
						AbsynVisitor visitor = new ShowTreeVisitor(printer);

						result.accept(visitor, 0);
						
						printer.close();
					}
					else if (S_ARGUMENT) {
						System.out.println("\nStarting Semantic Analysis...");
						
						String newFileName = modifyFileName(argv[0], ".sym");
						System.out.println("Sending Semantic Analysis Output to file: " + newFileName);
						
						AnalyzerPrinter printer = new AnalyzerPrinter(newFileName, true);
						SemanticAnalyzer analyzer = new SemanticAnalyzer(printer);
						
						analyzer.analyze((DecList) result);
						System.out.println("Semantic Analysis Completed.");
						
						printer.close();
					}
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
