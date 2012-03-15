package com.laytonsmith.tools;


import static com.laytonsmith.PureUtilities.TermColors.*;
import com.laytonsmith.PureUtilities.ZipMaker;
import java.io.File;
/**
 *
 * @author layton
 */
public class MSLPMaker {
    
    public static void start(String path){
        File start = new File(path);
        if(!start.exists()){
            System.err.println("The specified file does not exist!");
            return;
        }
        
        File output = new File(start.getParentFile(), start.getName() + ".mslp");
        if(output.exists()){
            pl("The file " + output.getName() + " already exists, would you like to overwrite? (Y/N)");
            String overwrite = prompt();
            if(!overwrite.equalsIgnoreCase("y")){
                return;
            }
        }
        
        ZipMaker.MakeZip(start, output.getName());
        
        pl("The mslp file has been created at " + output.getAbsolutePath());
    }
}
