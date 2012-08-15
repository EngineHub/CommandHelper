package com.laytonsmith.PureUtilities;

/**
 * This class converts a simple path into an XPath object. The *single* difference between
 * simple paths and XPath is that indexes are 0 based, not 1 based. To prevent confusion however,
 * ALL simple paths must start with a $.
 * @author lsmith
 */
public class SimplePath {
	/**
	 * Converts a simple path into an XPath String, which can then be used
	 * like normal. See the class documentation for how to make a simple path. If
	 * the string passed in does not start with a '$', it is simply returned. Otherwise,
	 * all numeric based indexes are converted from 0 base to 1 base.
	 * @param simplePath
	 * @return 
	 */
	public static String Interpret(String simplePath){
		if(!simplePath.startsWith("$")){
			return simplePath;
		} else {
			StringBuilder buffer = new StringBuilder();
			StringBuilder lookahead;
			for(int i = 1; i < simplePath.length(); i++){
				Character c = simplePath.charAt(i);
				if(c == '['){
					buffer.append(c);
					//We need to lookahead for the next ]
					lookahead = new StringBuilder();
					for(int j = i + 1; j < simplePath.length(); j++){
						i = j;
						Character c2 = simplePath.charAt(j);
						if(c2 == ']'){							
							String index = lookahead.toString().trim();
							try{
								int number = Integer.parseInt(index);								
								buffer.append(Integer.toString(number + 1));
							} catch(NumberFormatException e){
								//Not a number, which is ok	
								buffer.append(index);
							}
							buffer.append(c2);
							lookahead = null;
							break;
						} else {
							lookahead.append(c2);
						}
					}
					if(lookahead != null && lookahead.length() > 0){
						//This only happens if we reach the end of the string with an unclosed [
						buffer.append(lookahead.toString());						
					}
				} else {
					buffer.append(c);
				}
			}
			return buffer.toString();
		}
	}
	
//	public static void main(String [] args) throws Exception{
//		System.out.println(Interpret("/this/is/[1]/pure/xpath/text()"));
//		System.out.println(Interpret("$/this/is/simple/path"));
//		System.out.println(Interpret("$/this/is/simple[0]/path"));
//		System.out.println(Interpret("$/this/is/simple[path]"));
//		System.out.println(Interpret("$/this/is/simple/[path with error, but should still work"));
//	}
}
