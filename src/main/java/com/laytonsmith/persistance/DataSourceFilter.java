package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a File, creates a data source filter, which can be 
 * @author layton
 */
public class DataSourceFilter {
    
    Map<Pattern, String> mappings = new HashMap<Pattern, String>();
    Map<Pattern, String> original = new HashMap<Pattern, String>();
    private static Pattern captureUsage = Pattern.compile("\\$(\\d+)");
    
    public DataSourceFilter(File file) throws FileNotFoundException, DataSourceException{
        try{
            process(FileUtility.read(file));
        } catch(DataSourceException e){
            throw new DataSourceException("Could not process filter file located at " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }
    
    public DataSourceFilter(String filters) throws DataSourceException{
        process(filters);
    }
    
    private void process(String filters) throws DataSourceException{
        Properties p = new Properties();
        try {
            StringReader sr = new StringReader(filters);
            p.load(sr);
        } catch (IOException ex) {
            //Won't happen.
        }
        //We now have the whole list of filters=connection. We need to parse out and
        //find the connection aliases and normal filters, then parse those individually.
        //First, find the aliases, so we can go ahead and distribute those out when
        //we get to them.
        Map<String, String> aliases = new HashMap<String, String>();
        for(String key : p.stringPropertyNames()){
            if(key.matches("\\$[^a-zA-Z_]+")){
                //Bad alias, bail                
                throw new DataSourceException("Aliases in your filters may not start with a digit.");
            }
            if(key.matches("\\$[a-zA-Z_][a-zA-Z0-9_]*")){
                //It's an alias
                aliases.put(key, p.getProperty(key));
            }
        }
        
        //Ok, now let's load up the actual connections.
        for(String key : p.stringPropertyNames()){
            if(!key.matches("\\$.*")){
                if(key.matches("[^a-zA-Z0-9_\\(\\)\\*]")){
                    //Bad character in the filter. Bail.
                    throw new DataSourceException("Invalid character in filter. Only"
                            + " the following characters are allowed: a-zA-Z0-9_()*"
                            + " Found this instead: " + key);
                }
                //We need to change * into [^\.]*? and ** into .*? and . into \.
                //Parenthesis are kept as is.
                String newKey = key.replaceAll("\\.", "\\\\.");
                StringBuilder b = new StringBuilder("^");
                for(int i = 0; i < newKey.length(); i++){
                    Character c1 = newKey.charAt(i);
                    Character c2 = null;
                    if(i + 1 < newKey.length()){
                        c2 = newKey.charAt(i + 1);
                    }
                    if(c1 == '*' && c2 != null && c2 == '*'){
                        //Double star
                        b.append(".*?");
                        i++;
                    } else if(c1 == '*'){
                        //Single star
                        b.append("[^\\.]*?");
                    } else {
                        //Some other character
                        b.append(c1);
                    }                    
                }
                b.append("$");
                String regexKey = b.toString();
                
                //This is the number of expected capture usages to be found in the
                //associated value
                int expected = 0;
                
                //Now, we need to look to make sure that parenthesis are correct.
                boolean inParenthesis = false;
                for(Character c : regexKey.toCharArray()){
                    if(c.equals('(')){
                        if(inParenthesis){
                            //Nope.
                            throw new DataSourceException("Invalid filter, new"
                                    + " capture started before closing the previous one (do"
                                    + " you have two left parenthesis in a row?) in: " + key);
                        }
                        expected++;
                        inParenthesis = true;
                    }
                    if(c.equals(')')){
                        if(!inParenthesis){
                            throw new DataSourceException("Invalid filter, capture"
                                    + " group ended, but one had not been started (do you have"
                                    + " an extra right parenthesis without a matching left?) in: " + key);
                        }
                        inParenthesis = false;
                    }
                }
                if(inParenthesis){
                    throw new DataSourceException("Invalid filter, capture group not ended (did you"
                            + " forget to close a left parenthesis?) in: " + key);
                }
                Pattern pattern = Pattern.compile(regexKey);
                
                //Ok, have the pattern, now lets see if the value is an alias
                
                String value = p.getProperty(key);
                String originalValue = value;
                //Used for more meaningful error messages below
                boolean isAlias = false;
                if(value.matches("\\$.*")){
                    if(!aliases.containsKey(value)){
                        throw new DataSourceException("Invalid alias: " + value + " is trying to be"
                                + " used, but has not been defined.");
                    } else {
                        value = aliases.get(value);
                        isAlias = true;
                    }                    
                }
                //Now, let's check to make sure that any captures are valid
                Matcher m = captureUsage.matcher(value);
                while(m.find()){
                    int i = Integer.parseInt(m.group(1));
                    if(i < 1 || i > expected){
                        //Show a very detailed error message.
                        throw new DataSourceException("Invalid capture group \"$" + i + "\". " +
                                (expected == 0?"No capture usages were expected":"Only"
                                + " " + expected + " capture" + (expected!=1?"s":"") + " were"
                                + " expected") + " for the connection: " + value 
                                + (isAlias?"(Defined as alias " + originalValue + ")":""));
                    }
                }
                
                //Is this pattern already in the mapping? If so, we need to throw an error.
                if(mappings.containsKey(pattern)){
                    throw new DataSourceException("Multiple definitions exist for the key: " + key);
                }
                
                //Ok, finally, one last validation, we want to make sure that the URI at least
                //looks valid enough. Dollar signs aren't valid though, so lets replace our otherwise
                //valid capture usages.
                try{
                    new URI(value.replaceAll("\\$\\d*", "_"));
                } catch(URISyntaxException e){
                    throw new DataSourceException("Invalid URI for " + value 
                            + (isAlias?"(Defined for alias " + originalValue + ")":"") + ".");
                }                
                //Alright. It's cool. Add it to the list.
                mappings.put(pattern, value);
                original.put(pattern, key);
            }
            //else it's an alias, and we've already dealt with it
        }
    }
    
    public URI getConnection(String [] key){
        return getConnection(StringUtils.Join(key, "."));
    }
    
    public URI getConnection(String key){
        List<Pattern> matches = new ArrayList<Pattern>();
        for(Pattern p : mappings.keySet()){
            if(p.matcher(key).matches()){
                matches.add(p);
            }
        }
        //Ok, we have a list of the actual matches, we have to narrow it down to the closest
        //match.
        Pattern closest = null;
        if(matches.isEmpty()){
            //Trivial case
            return null;
        } else if(matches.size() == 1){
            //Yay! Also a trivial case!            
            closest = matches.get(0);
        } else {
            int lowest = Integer.MAX_VALUE;
            for(Pattern p : matches){
                //The closest match is defined as a filter that, minus wild cards, matches more characters.
                //So, for instance, if the key is a.b.c.d, then this matches a.*.c.d better than a.*.*.d
                //The easiest way to detect this is to simply remove *() characters, and do a Levenshtein distance on the strings, and
                //whichever one is lowest, is the closest.
                String originalKey = original.get(p);
                int dist = StringUtils.LevenshteinDistance(key, originalKey.replaceAll("\\*", "").replaceAll("[\\(\\)]", ""));
                //TODO: Unfortunately, the properties file doesn't keep crap in order for us, so right now,
                //if there is a tie, it is undefined what will happen. Instead of letting this happen
                //without a warning, we want to issue a warning, though we will arbitrarily select one.
                if(dist == lowest){
                    CHLog.Log(CHLog.Tags.PERSISTANCE, "Two keys equally match for the key \"" + key 
                            + "\". Both " + original.get(closest) + " and " + original.get(p) 
                            + " match just as well. For the time being, this is an undefined result, but"
                            + " for this time, " + original.get(closest) + " is being selected.", Target.UNKNOWN);
                }
                if(dist < lowest){
                    closest = p;
                    lowest = dist;
                }
            }
        }
        
        try {
            if(closest == null){
                return null;
            }
            String uri = mappings.get(closest);
            Matcher m = closest.matcher(key);
            while(m.find()){
                //We need to replace the captures
                for(int i = 1; i <= m.groupCount(); i++){
                    uri = uri.replaceAll("\\$" + i, m.group(i));
                }
            }
            return new URI(uri);
        } catch (URISyntaxException ex) {
            //We already verified that this won't happen, so yeah.
            return null;
        }        
    }
}
