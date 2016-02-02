
package com.laytonsmith.core;

import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A parse tree wraps a generic tree node, but provides functions that are commonly used to discover
 * things about a particular section of code.
 * 
 */
public class ParseTree implements Cloneable{
	

	private enum CacheTypes{
		IS_SYNC, IS_ASYNC, FUNCTIONS
	}
	
	/**
	 * Since for the most part, this is a wrapper class, we want instantiations
	 * to be cheap, both time and memory. However we also want to be able to cache certain information,
	 * since many of our operations are fairly expensive,
	 * so we also want to maintain a cache. But we ALSO don't want
	 * to have a memory leak by simply having tons of cached references. So, we
	 * store a private cache of weak references to "this" instance.
	 */		
	private static Map<ParseTree, Map<CacheTypes, Object>> cache 
		= new WeakHashMap<ParseTree, Map<CacheTypes, Object>>();
	
	private static boolean isCached(ParseTree tree, CacheTypes type){
		if(!cache.containsKey(tree)){
			return false;
		} else {
			return cache.get(tree).containsKey(type);
		}
	}
	
	/**
	 * Returns the value from the cache. This will throw an Error if no cached
	 * value exists, so you must ALWAYS call isCached first.
	 * @param tree
	 * @param type
	 * @return 
	 */
	private static Object getCache(ParseTree tree, CacheTypes type){
		if(!isCached(tree, type)){
			throw new Error("It is an error to call getCache on an object that does not already have a cached value");
		}
		return cache.get(tree).get(type);
	}
	
	private static void setCache(ParseTree tree, CacheTypes type, Object value){
		if(!cache.containsKey(tree)){
			cache.put(tree, new EnumMap<CacheTypes, Object>(CacheTypes.class));
		}
		cache.get(tree).put(type, value);
	}
	
	private static void clearCache(ParseTree tree){
		cache.remove(tree);
	}
	
	
	private Construct data = null;
	private boolean isOptimized = false;
	private final FileOptions fileOptions;
	private List<ParseTree> children = null;
	private boolean hasBeenMadeStatic = false;
	
	/**
	 * Creates a new empty tree node
	 */
	public ParseTree(FileOptions options){
		children = new ArrayList<ParseTree>();
		this.fileOptions = options;
	}
	
	/**
	 * Creates a new tree node, with this construct as the data
	 * @param construct 
	 */
	public ParseTree(Construct construct, FileOptions options){
		this(options);
		setData(construct);
	}
	
	public FileOptions getFileOptions(){
		return fileOptions;
	}
	
	public void setData(Construct data) {
		this.data = data;
	}
	
	public void setOptimized(boolean optimized){
		isOptimized = optimized;
	}
	
	public boolean isOptimized(){
		return isOptimized;
	}
	
	public boolean hasBeenMadeStatic() {
		return hasBeenMadeStatic;
	}
	
	public void hasBeenMadeStatic(boolean state){
		hasBeenMadeStatic = state;
	}
	
	/**
	 * Returns a flat list of all node data. This can be used when an entire tree
	 * needs to be scoured for information, regardless of visitation order.
	 * @return 
	 */
	public List<Construct> getAllData(){
		List<Construct> list = new ArrayList<Construct>();
		list.add(getData());
		for(ParseTree node : getChildren()){
			list.addAll(node.getAllData());
		}
		return list;
	}
	
	/**
	 * Returns a list of direct children
	 * @return 
	 */
	public List<ParseTree> getChildren(){
		return children;
	}
	
	/**
	 * Gets the child at the index specified.
	 * @param index
	 * @throws IndexOutOfBoundsException if the index overflows
	 * @return 
	 */
	public ParseTree getChildAt(int index){
		return children.get(index);
	}
	
	/**
	 * Returns the data in this node
	 * @return 
	 */
	public Construct getData(){
		return data;
	}
	
	/**
	 * Returns true if this node has children
	 * @return 
	 */
	public boolean hasChildren(){		
		return !children.isEmpty();
	}
	
	public void setChildren(List<ParseTree> children){
		this.children = children;
	}
	
	/**
	 * Adds a child
	 * @param node 
	 */
	public void addChild(ParseTree node){
		children.add(node);
	}
	
	/**
	 * Adds a child at the specified index
	 * @param index
	 * @param node 
	 */
	public void addChildAt(int index, ParseTree node){
		children.add(index, node);
	}
	
	/**
	 * Returns the number of children this node has
	 * @return 
	 */
	public int numberOfChildren(){
		return children.size();
	}
	
	/**
	 * Removes a child at the specified index
	 * @param index 
	 */
	public void removeChildAt(int index){
		children.remove(index);
	}
	
	/**
	 * Removes all children from this node
	 */
	public void removeChildren(){
		children.clear();
	}
	
	/**
	 * A value is considered "const" if it doesn't have any children. An array
	 * is considered a const, though the array function is not. That is to say,
	 * the value is determined for sure, despite the rest of the code, this will
	 * always return the same value. Most constructs are const.
	 * @return 
	 */
	public boolean isConst(){
		return !data.isDynamic();
	}
	
	/**
	 * Returns the opposite of isConst().
	 * @return 
	 */
	public boolean isDynamic(){
		return data.isDynamic();
	}
	
	//TODO: None of this will work until we deeply consider procs, which can't happen yet.
//	/**
//	 * If ANY data node REQUIRES this to be async, this will return true. If
//	 * NONE of the data nodes REQUIRE this to be async, or if NONE of them care,
//	 * it returns false.
//	 * @return 
//	 */
//	public boolean isAsync(){
//		if(isCached(this, CacheTypes.IS_ASYNC)){
//			return (Boolean)getCache(this, CacheTypes.IS_ASYNC);
//		} else {
//			boolean ret = false;
//			for(Function ff : getFunctions()){
//				Boolean runAsync = ff.runAsync();
//				if(runAsync != null && runAsync == true){
//					//We're done here. It's definitely async only,
//					//so we can stop looking.
//					ret = true;				
//				}
//			}
//			setCache(this, CacheTypes.IS_ASYNC, ret);
//			return ret;
//		}		
//	}
//	
//	/**
//	 * If ANY data node REQUIRES this to be sync, this will return true. If
//	 * NONE of the data nodes REQUIRE this to be sync, or if NONE of them care,
//	 * it returns false.
//	 * @return 
//	 */
//	public boolean isSync(){
//		if(isCached(this, CacheTypes.IS_SYNC)){
//			return (Boolean)getCache(this, CacheTypes.IS_SYNC);
//		} else {
//			boolean ret = false;
//			for(Function ff : getFunctions()){
//				Boolean runAsync = ff.runAsync();
//				if(runAsync != null && runAsync == false){
//					//We're done here. It's definitely sync only,
//					//so we can stop looking.
//					ret = true;
//				}
//			}
//			setCache(this, CacheTypes.IS_SYNC, ret);
//			return ret;
//		}
//	}
	
	/**
	 * Returns a list of all functions contained in this parse tree.
	 * @return 
	 */
	public List<Function> getFunctions(){
		if(isCached(this, CacheTypes.FUNCTIONS)){
			return new ArrayList<Function>((List<Function>)getCache(this, CacheTypes.FUNCTIONS));
		} else {
			List<Function> functions = new ArrayList<Function>();
			List<Construct> allChildren = getAllData();
			loop: for(Construct c : allChildren){
				if(c instanceof CFunction){
					try {
						FunctionBase f = FunctionList.getFunction(c);
						if(f instanceof Function){
							Function ff = (Function)f;
							functions.add(ff);
						}
					} catch (ConfigCompileException ex) {
						throw new Error(ex);
					}
					
				}
			}			
			setCache(this, CacheTypes.FUNCTIONS, functions);
			return new ArrayList<Function>(functions);
		}
	}

	@Override
	public ParseTree clone() throws CloneNotSupportedException {
		ParseTree clone = (ParseTree)super.clone();
		clone.data = data.clone();
		clone.children = new ArrayList<ParseTree>(this.children);
		return clone;
	}
	
	@Override
	public String toString(){
		return data.toString();
	}
	
	public String toStringVerbose(){
		StringBuilder stringRepresentation = new StringBuilder();
		if(data instanceof CFunction){
			stringRepresentation.append(data.toString());
			stringRepresentation.append("(");
			boolean first = true;
			for(ParseTree child : children){
				if(!first){
					stringRepresentation.append(", ");
				}
				first = false;
				stringRepresentation.append(child.toStringVerbose());
			}
			stringRepresentation.append(")");
		} else if (data instanceof CString) {
            // Convert: \ -> \\ and ' -> \'
            stringRepresentation.append("'").append(data.val().replaceAll("\t", "\\t").replaceAll("\n", "\\n").replace("\\", "\\\\").replace("'", "\\'")).append("'");
		} else if(data instanceof IVariable){
			stringRepresentation.append(((IVariable)data).getVariableName());
        } else {
            stringRepresentation.append(data.val());
        }
        return stringRepresentation.toString();
	}
	
	public Target getTarget(){
		if(data == null){
			return Target.UNKNOWN;
		} else {
			return data.getTarget();
		}
	}
	
	
}
