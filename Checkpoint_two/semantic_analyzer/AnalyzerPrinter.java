package semantic_analyzer;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class AnalyzerPrinter {

    private BufferedWriter writer;
    private boolean toFile;
    private static final int  SPACES = 4;

    public AnalyzerPrinter(String filename, boolean toFile){

        this.toFile = toFile;

        if (toFile){
            try {
                this.writer = new BufferedWriter(new FileWriter(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void indent(int level){

        if (toFile){
            
            try {
                for( int i = 0; i < level * SPACES; i++ ) writer.write( " " );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
        }
    }

    public void printErr(String error){
        System.err.println(error);
    }

    public void printMsg(String msg){
        if (toFile){
            try {
                writer.write(msg);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println(msg);
        }
    }

    public void printSymbol(String msg, int level){

        indent(level);
        if (toFile){
            try {
                writer.write(msg);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.println(msg);
        }

    }

}
