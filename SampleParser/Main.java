/*
  Created by: Fei Song
  File Name: Main.java
  To Build: 
  After the Scanner.java, tiny.flex, and tiny.cup have been processed, do:
    javac Main.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. Main gcd.tiny

  where gcd.tiny is an test input file for the tiny language.
*/
import java.io.*;
import absyn.*;

class Main {
  public final static boolean SHOW_TREE = true;
  
  static public void main(String argv[]) {    
    /* Start the parser */
    try {
      System.out.println("Starting parser on file: " + argv[0]);
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);
      
      if (result == null) {
          System.out.println("Parsing returned NULL! Check input file.");
      } else {
          System.out.println("Parsing completed successfully.");
          System.out.println(result);
      }
      
      if (SHOW_TREE && result != null) {
         System.out.println("The abstract syntax tree is:");
         AbsynVisitor visitor = new ShowTreeVisitor();
         result.accept(visitor, 0); 
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
