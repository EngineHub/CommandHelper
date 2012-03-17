package com.laytonsmith.core.constructs;

import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Math;
import java.io.File;

/**
 *
 * @author layton
 */
public class CSlice extends Construct {
    private long start;
    private long finish;
    public CSlice(String slice, Target t) throws ConfigCompileException{
        super(slice, ConstructType.SLICE, t);
        String [] split = slice.split("\\.\\.");
        if(split.length > 2){
            throw new ConfigCompileException("Invalid slice notation! (" + slice + ")", t);
        }
        
        String sstart;
        String sfinish;
        if(split.length == 1){
            sstart = slice.trim().substring(0, slice.trim().length() - 2);
            sfinish = "-1";
        } else {
            if(slice.trim().startsWith("..")){
                sstart = "0";
                sfinish = slice.trim().substring(2);
            } else { 
            sstart = split[0];
            sfinish = split[1];
            }
        }
        try{
            start = Long.parseLong(sstart.trim());
            finish = Long.parseLong(sfinish.trim());
        } catch(NumberFormatException e){
            throw new ConfigRuntimeException("Expecting integer in a slice, but was given " + split[0] + " and " + split[1], t);
        }
    }
    
    public CSlice(long from, long to, Target t){
        super(from + ".." + to, ConstructType.SLICE, t);
        this.start = from;
        this.finish = to;
    }
    
    public long getStart(){
        return start;
    }
    
    public long getFinish(){
        return finish;
    }
}
