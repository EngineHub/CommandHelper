package com.laytonsmith.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class provides an easy way to specify argument layouts, which the optimizer
 * and type hinting system use to verify scripts during compilation and runtime.
 * @author layton
 */
public class TypeHinting {
    
    List<TypeHintingEntry> types = new ArrayList<TypeHintingEntry>();
    public String toString(){
        StringBuilder b = new StringBuilder();
        for(TypeHintingEntry the : types){
            b.append(the.toString()).append(" ");
        }
        return b.toString().trim();
    }
    
    private class TypeHintingEntry{
        List<HintEntry> types = new ArrayList<HintEntry>();
        public TypeHintingEntry(List<HintEntry> types){
            this.types = types;
        }
        
        public String toString(){
            StringBuilder b = new StringBuilder();
            for(HintEntry h : types){
                b.append(h.toString()).append(" ");
            }
            return b.toString().trim();
        }
    }
    
    //Marker class
    public static class Hint{}
    
    /**
     * Combines the modifiers and type into one class
     */
    private static class HintEntry{
        private Type type;
        private List<Modifier> modifiers;
        private HintEntry(Type type, Modifier ... modifiers){
            this.type = type;
            this.modifiers = Arrays.asList(modifiers);
        }
        
        public boolean fullyMatches(Type t){
            //TODO: This needs to also check array keys, if applicable
            return type.i == t.i;
        }
        
        public boolean isOptional(){
            for(Modifier m : modifiers){
                if(m.i == 1){
                    return true;
                }
            }
            return false;
        }
        
        public boolean isVararg(){
            for(Modifier m : modifiers){
                if(m.i == 2){
                    return true;
                }
            }
            return false;
        }
        
        public String toString(){
            StringBuilder b = new StringBuilder();
            for(Modifier m : modifiers){
                b.append(m.toString()).append(" ");
            }
            b.append(type.toString());
            return b.toString().trim();
        }
    } 
    /**
     * The type is the actual data type required.
     */
    public static class Type extends Hint{
        public final static Type MIXED = new Type(1, "MIXED");
        public final static Type ARRAY = new Type(2, "ARRAY");
        public static Type ARRAY(String ... keys){
            Type m = new Type(2, "ARRAY");
            m.keys = keys;
            return m;
        }
        public static Type ARRAY(int minLength, int maxLength){
            Type m = new Type(2, "ARRAY");
            m.keys = null;
            m.minArrayLength = minLength;
            m.maxArrayLength = maxLength;
            return m;
        }
        public static Type ARRAY(int length){
            return ARRAY(length, length);
        }
        public final static Type INT = new Type(4, "INT");
        public final static Type FLOAT = new Type(8, "FLOAT");
        public final static Type NUMBER = new Type(16, "NUMBER");
        public final static Type STRING = new Type(32, "STRING");
        public final static Type VOID = new Type(64, "VOID");
        public final static Type BOOLEAN = new Type(128, "BOOLEAN");
        public final static Type NULL = new Type(256, "NULL");
        public final static Type IVARIABLE = new Type(512, "IVARIABLE");
        public static Type OBJECT(String name){
            Type m = new Type(1024, "OBJECT");
            m.object = name;
            return m;
        }
        
        private String object;
        private String toString;
        private String [] keys;
        private int minArrayLength;
        private int maxArrayLength;
        private int i;
        
        private Type(int i, String name){
            this.i = i;
            this.toString = name;
        }
        
        public String toString(){
            return toString;
        }
    }
    
    /**
     * A modifier modifies the state of the next Type specified. For instance:
     * (OPTIONAL, STRING) is valid, and the string is marked as optional.
     * There can only be one VARARGS in an entire sequence.
     *  
     */
    public static class Modifier extends Hint{
        public final static Modifier OPTIONAL = new Modifier(1, "OPTIONAL");
        public final static Modifier VARARGS = new Modifier(2, "VARARGS");
        String object = null;
        String toString = null;
        int i = 0;
        private Modifier(int i, String name){
            this. i = i;
            this.toString = name;
        }
        
        public String toString(){
            return toString;
        }
    }
    
    /**
     * Generates a new TypeHinting object, and fills it in with one sequence.
     * You may append other sequences by calling append as many times as needed.
     * The following rules apply when creating a sequence:
     * There may be a run of optional arguments, and they may appear anywhere in the
     * sequence. However, you cannot start a series of optional arguments, break back into
     * required arguments, then jump back into optional. If only some optional arguments are
     * provided by the user, they are filled in left to right. Varargs are considered optional,
     * meaning they can have 0+ arguments.
     * @param types
     * @return 
     */
    public static TypeHinting Generate(Hint ... types){
        return new TypeHinting().append(types);
    }
    
    /**
     * Appends a sequence to this TypeHinting object. It returns itself, so you
     * can easily chain appends. 
     * @param types
     * @return 
     */
    public TypeHinting append(Hint ... types){
        List<HintEntry> list = new ArrayList<HintEntry>();
        List<Modifier> modifiers = new ArrayList<Modifier>();
        Boolean optionalSeries = null;
        boolean nextIsOptional = false;
        for(Hint h : types){
            if(h instanceof Modifier){
                if(optionalSeries != null && optionalSeries == false){
                    throw new Error("The optional sequence has already been closed");
                }
                optionalSeries = true;
                nextIsOptional = true;
                modifiers.add((Modifier)h);
            } else if(h instanceof Type){
                HintEntry he = new HintEntry((Type)h, modifiers.toArray(new Modifier[]{}));
                modifiers = new ArrayList<Modifier>();
                list.add(he);
                if(nextIsOptional == false && optionalSeries != null && optionalSeries == true){
                    optionalSeries = false;                    
                }
                nextIsOptional = false;                
            }
        }
        TypeHintingEntry e = new TypeHintingEntry(list);
        this.types.add(e);
        return this;
    }
    
    /**
     * Given a sequence, says if it matches any of the specified sequences. It is
     * assumed that IVariables are intended at this point, and it will not pull out
     * the IVariable's ivar() to compare. Note that modifiers are not allowed here,
     * this is for a concrete list.
     * @param types
     * @return 
     */
    public boolean matches(Type ... types){
        for(TypeHintingEntry e : this.types){            
            //Walk through our arguments passed in, matching as we go.
            //Compare the arguments. If they match, consume the argument on the list.
            //If they do not match, check if this is optional. If not, look behind one.
            //If that one is varargs, and otherwise matches this, consume.
            //If still not, continue, it doesn't match this sequence.
            //If we reach the end of our list, the remaining sequence must be fully optional
            //for this to be valid. If not, continue. Otherwise, return true.
            List<Type> list = Arrays.asList(types);
            List<HintEntry> sequence = new ArrayList<HintEntry>(e.types);
            int listIndex = 0;
            int sequenceIndex = 0;
            while(true){
                if(listIndex >= list.size()){
                    //If the rest of the sequence is optional (or is now fully consumed) we're
                    //finished, and it's a match. Otherwise, it's a failure.
                    while(sequenceIndex < sequence.size()){
                        if(sequence.get(sequenceIndex).isOptional()){
                            //So far so good
                            sequenceIndex++;
                        } else {
                            //Nope, we're missing arguments. Break!
                            break;
                        }
                    }
                    return true;
                }
                //First check if this sequence is var args. If so, we need to consume as many
                //as we can in the list. Let's check backwards and see how many we could possibly skip
                if(sequence.get(sequenceIndex).isVararg()){
                    int mustRemain = sequence.size() - sequenceIndex - 1;
                    boolean keepSkipping = true;                    
                    while(keepSkipping){
                        if(list.size() - listIndex > mustRemain && list.size() - 1 > listIndex){
                            if(sequence.get(sequenceIndex).fullyMatches(list.get(listIndex))){
                                listIndex++;                            
                            } else {
                                //Failure. Break! This will only set it to break out of this
                                //while, but we're leaving keepSkipping true, which will
                                //break us out one more time below
                                break;
                            }
                        } else {
                            //We got to the end of the list, or we got to the end of the amount we need
                            //to skip, so stop skipping now, and let the next bit take over
                            keepSkipping = false;
                        }
                    }
                    if(keepSkipping){
                        //Break up one more time
                        break; 
                    }
                }
                //If these both match, carry on
                if(sequence.get(sequenceIndex).fullyMatches(list.get(listIndex))){
                    listIndex++;
                    sequenceIndex++;                    
                    continue;
                }
                //Ok, so they aren't the same type. Is the sequence entry optional?
                if(sequence.get(sequenceIndex).isOptional()){
                    //Yes it is, so bump the sequence index, and carry on.
                    sequenceIndex++;
                    continue;
                } else {
                    //Ok, well, it's not var arg either, so we're out of things to try. It's not a match. Break!
                    break;
                }
            }
            
        }
        return false;
    }
    
    //Don't allow direct instantiation
    private TypeHinting(){}
}
