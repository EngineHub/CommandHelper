/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Construct;
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
 * @author Layton
 */
public class ParseTree implements Cloneable{
	

	private enum CacheTypes{
		IS_SYNC, IS_ASYNC
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
	
	
	private GenericTreeNode<Construct> tree;
	private boolean isOptimized = false;
	
	/**
	 * Creates a new empty tree node
	 */
	public ParseTree(){
		this.tree = new GenericTreeNode<Construct>();
	}
	
	/**
	 * Creates a new tree node, with this construct as the data
	 * @param construct 
	 */
	public ParseTree(Construct construct){
		this();
		tree.setData(construct);
	}
	
	/**
	 * Creates a new ParseTree, using the GenericTreeNode as the backing.
	 * @param tree 
	 */
	private ParseTree(GenericTreeNode<Construct> tree){
		this.tree = tree;
	}
	
	public void setData(Construct data) {
		tree.setData(data);
	}
	
	public void setOptimized(boolean optimized){
		isOptimized = optimized;
	}
	
	public boolean isOptimized(){
		return isOptimized;
	}
	
	/**
	 * Returns a flat list of all node data. This can be used when an entire tree
	 * needs to be scoured for information, regardless of visitation order.
	 * @return 
	 */
	public List<Construct> getAllData(){
		List<Construct> list = new ArrayList<Construct>();
		list.add(tree.getData());
		for(GenericTreeNode<Construct> node : tree.getChildren()){
			list.addAll(new ParseTree(node).getAllData());
		}
		return list;
	}
	
	/**
	 * Returns a list of direct children
	 * @return 
	 */
	public List<ParseTree> getChildren(){
		List<ParseTree> children = new ArrayList<ParseTree>();
		for(GenericTreeNode<Construct> node : tree.getChildren()){
			children.add(new ParseTree(node));
		}
		return children;
	}
	
	/**
	 * Gets the child at the index specified.
	 * @param index
	 * @throws IndexOutOfBoundsException if the index overflows
	 * @return 
	 */
	public ParseTree getChildAt(int index){
		return new ParseTree(tree.getChildAt(index));
	}
	
	/**
	 * Returns the data in this node
	 * @return 
	 */
	public Construct getData(){
		return tree.getData();
	}
	
	/**
	 * Returns true if this node has children
	 * @return 
	 */
	public boolean hasChildren(){		
		return tree.hasChildren();
	}
	
	public void setChildren(List<ParseTree> children){
		List<GenericTreeNode<Construct>> gtnChildren = new ArrayList<GenericTreeNode<Construct>>();
		for(ParseTree child : children){
			gtnChildren.add(child.tree);
		}
		tree.children = gtnChildren;
	}
	
	/**
	 * Adds a child
	 * @param node 
	 */
	public void addChild(ParseTree node){
		tree.addChild(node.tree);
	}
	
	/**
	 * Adds a child at the specified index
	 * @param index
	 * @param node 
	 */
	public void addChildAt(int index, ParseTree node){
		tree.addChildAt(index, node.tree);
	}
	
	/**
	 * Returns the number of children this node has
	 * @return 
	 */
	public int numberOfChildren(){
		return tree.getNumberOfChildren();
	}
	
	/**
	 * Removes a child at the specified index
	 * @param index 
	 */
	public void removeChildAt(int index){
		tree.removeChildAt(index);
	}
	
	/**
	 * Removes all children from this node
	 */
	public void removeChildren(){
		tree.removeChildren();
	}
	
	/**
	 * A value is considered "const" if it doesn't have any children. An array
	 * is considered a const, though the array function is not. That is to say,
	 * the value is determined for sure, despite the rest of the code, this will
	 * always return the same value. Most constructs are const.
	 * @return 
	 */
	public boolean isConst(){
		return !tree.data.isDynamic();
	}
	
	/**
	 * Returns the opposite of isConst().
	 * @return 
	 */
	public boolean isDynamic(){
		return tree.data.isDynamic();
	}
	
	/**
	 * If ANY data node REQUIRES this to be async, this will return true. If
	 * NONE of the data nodes REQUIRE this to be async, or if NONE of them care,
	 * it returns false.
	 * @return 
	 */
	public boolean isAsync(){
		if(isCached(this, CacheTypes.IS_ASYNC)){
			return (Boolean)getCache(this, CacheTypes.IS_ASYNC);
		} else {
			List<Construct> allChildren = getAllData();
			boolean ret = false;
			loop: for(Construct c : allChildren){
				if(c instanceof CFunction){
					try {
						FunctionBase f = FunctionList.getFunction(c);
						if(f instanceof Function){
							Function ff = (Function)f;
							Boolean runAsync = ff.runAsync();
							if(runAsync != null && runAsync == true){
								//We're done here. It's definitely async only,
								//so we can stop looking.
								ret = true;
								break loop;
							}
						}
					} catch (ConfigCompileException ex) {
						throw new Error(ex);
					}
					
				}
			}
			setCache(this, CacheTypes.IS_ASYNC, ret);
			return ret;
		}		
	}
	
	/**
	 * If ANY data node REQUIRES this to be sync, this will return true. If
	 * NONE of the data nodes REQUIRE this to be sync, or if NONE of them care,
	 * it returns false.
	 * @return 
	 */
	public boolean isSync(){
		if(isCached(this, CacheTypes.IS_SYNC)){
			return (Boolean)getCache(this, CacheTypes.IS_SYNC);
		} else {
			List<Construct> allChildren = getAllData();
			boolean ret = false;
			loop: for(Construct c : allChildren){
				if(c instanceof CFunction){
					try {
						FunctionBase f = FunctionList.getFunction(c);
						if(f instanceof Function){
							Function ff = (Function)f;
							Boolean runAsync = ff.runAsync();
							if(runAsync != null && runAsync == false){
								//We're done here. It's definitely sync only,
								//so we can stop looking.
								ret = true;
								break loop;
							}
						}
					} catch (ConfigCompileException ex) {
						throw new Error(ex);
					}
					
				}
			}
			setCache(this, CacheTypes.IS_SYNC, ret);
			return ret;
		}
	}

	@Override
	public ParseTree clone() throws CloneNotSupportedException {
		return new ParseTree(tree.clone());
	}		
	
	
}
