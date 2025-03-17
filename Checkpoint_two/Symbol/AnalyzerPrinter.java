package Symbol;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class AnalyzerPrinter {

    private BufferedWriter writer;
    private boolean toFile; //write to file or stdout
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

    public void indent(int level){

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

    //print to stderr
    public void printErr(String error){
        System.err.println(error);
    }

	public void printMsgNoNewLine(String msg){
		  if (toFile){
            try {
                writer.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            System.out.print(msg);
        }
		
	}
    //print message without level
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

    //print a single message at a level
    public void printLevel(String msg, int level){

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

    //get arrayList of strings and print
    public void printLevelList(List<String> strings, int level){

        for (String msg : strings){
            printLevel(msg, level);
        }
    }

    public void close(){
		if(writer != null)
		{
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

}
