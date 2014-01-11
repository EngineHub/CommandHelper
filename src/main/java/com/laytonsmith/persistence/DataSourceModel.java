package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.core.GenericTreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a data source model. The underlying model is just a map
 * of values or maps, but this class abstracts accessing and storing the data.
 * If a value in a model can be more efficiently lazily loaded, this class
 * perhaps should not be used, but for non event driven models, it should
 * suffice for many cases. If a data source inherently has a better way to store
 * the data, then it may choose to not use this class. Note: Not all data
 * sources can store a key in both namespace.value and namespace.value.other, in
 * that case, to make namespace.value's actual value, it should be stored as
 * namespace.value.~
 *
 * @author lsmith
 */
public final class DataSourceModel {

	private GenericTreeNode<Pair<String, String>> tree = new GenericTreeNode<Pair<String, String>>();

	public DataSourceModel(Map<String, Object> model) {
		//We have to do a depth first traversal here to get all the keys
		if (model != null) {
			build(model, tree);
		}
	}

	/**
	 * This constructor assumes that the key is fully specified in dot notation.
	 *
	 * @param list
	 */
	public DataSourceModel(List<Pair<String, String>> list) {
		for (Pair<String, String> pair : list) {
			String[] key = pair.getKey().split("\\.");
			set(key, pair.getValue());
		}
	}

	private void build(Object node, GenericTreeNode<Pair<String, String>> treeNode) {
		if (node instanceof Map) {
			//We need to iterate through all the keys, creating children as we go
			for (String key : ((Map<String, Object>) node).keySet()) {
				if (key.equals("_")) {
					//Special case, this is a reserved key
					build(((Map<String, Object>) node).get(key), treeNode);
				} else {
					GenericTreeNode<Pair<String, String>> newNode =
							new GenericTreeNode<Pair<String, String>>(new Pair<String, String>(key, null));
					treeNode.addChild(newNode);
					build(((Map<String, Object>) node).get(key), newNode);
				}
			}
		} else {
			//This is the node we want to put the data in
			treeNode.data.setValue(node==null?null:node.toString());
		}
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (GenericTreeNode<Pair<String, String>> child : tree.getChildren()) {
			decompose(map, child);
		}
		return map;
	}

	public List<Pair<String[], String>> toList() {
		List<Pair<String[], String>> list = new ArrayList<Pair<String[], String>>();
		//TODO
		return list;
	}

	private void decompose(Map<String, Object> node, GenericTreeNode<Pair<String, String>> treeNode) {
		if (treeNode.hasChildren()) {
			//If it's not a leaf node, we need to add a new child to the map. 
			//However, if the data isn't null, we need to add the data now as a _ key
			Map<String, Object> map = new HashMap<String, Object>();
			if (treeNode.getData().getValue() != null) {
				map.put("_", treeNode.getData().getValue());
			}
			node.put(treeNode.getData().getKey(), map);
			for (GenericTreeNode<Pair<String, String>> child : treeNode.getChildren()) {
				decompose(map, child);
			}
		} else {
			//It's a leaf node, so we just put the data in the map and call it a day
			node.put(treeNode.getData().getKey(), treeNode.getData().getValue());
		}
	}

	public String get(String[] key) {
		return getValue(new ArrayList<String>(Arrays.asList(key)), tree);
	}

	public void set(String[] key, String value) {
		setValue(new ArrayList<String>(Arrays.asList(key)), tree, value);
	}

	public void clearKey(String[] key) {
		set(key, null);
	}

	private String getValue(List<String> keys, GenericTreeNode<Pair<String, String>> treeNode) {
		String value = null;
		if (!keys.isEmpty()) {
			String key = keys.get(0);
			keys.remove(0);
			for (GenericTreeNode<Pair<String, String>> child : treeNode.getChildren()) {
				if (child.getData().getKey().equals(key)) {
					return getValue(keys, child);
				}
			}
		} else {
			value = treeNode.getData().getValue();
		}
		return value;
	}

	private void setValue(List<String> keys, GenericTreeNode<Pair<String, String>> treeNode, String value) {
		if (keys.isEmpty()) {
			treeNode.getData().setValue(value);
		} else {
			GenericTreeNode<Pair<String, String>> found = null;
			String key = keys.get(0);
			keys.remove(0);
			for (GenericTreeNode<Pair<String, String>> child : treeNode.getChildren()) {
				if (child.getData().getKey().equals(key)) {
					found = child;
					break;
				}
			}
			if (found == null) {
				found = new GenericTreeNode<Pair<String, String>>(new Pair<String, String>(key, null));
				treeNode.addChild(found);
			}
			if (value == null) {
				if (keys.isEmpty()) {
					//We need to remove this node.
					//TODO: This fails to remove the parent
					for (int i = 0; i < treeNode.getNumberOfChildren(); i++) {
						GenericTreeNode<Pair<String, String>> node = treeNode.getChildAt(i);
						if (node.getData().getKey().equals(key)) {
							treeNode.removeChildAt(i);
							break;
						}
					}
					return;
				}
			}
			setValue(keys, found, value);
		}
	}

	public Set<String[]> keySet() {
		Set<String[]> keys = new HashSet<String[]>();
		for (GenericTreeNode child : tree.getChildren()) {
			traverse(child, new ArrayList<String>(), keys);
		}
		return keys;
	}

	private void traverse(GenericTreeNode<Pair<String, String>> treeNode, List<String> ongoingKey, Set<String[]> keys) {
		if (treeNode.hasChildren()) {
			ongoingKey.add(treeNode.getData().getKey());
			if (treeNode.getData().getValue() != null) {
				//Data and children
				keys.add(ongoingKey.toArray(new String[ongoingKey.size()]));
			}
			for (GenericTreeNode<Pair<String, String>> child : treeNode.getChildren()) {
				//recurse down now
				traverse(child, ongoingKey, keys);
			}
			ongoingKey.remove(ongoingKey.size() - 1);
		} else {
			//This is it, we're done here, so we can put the key in the list now, then pop off the last element in the ongoingKey
			ongoingKey.add(treeNode.getData().getKey());
			keys.add(ongoingKey.toArray(new String[ongoingKey.size()]));
			ongoingKey.remove(ongoingKey.size() - 1);
		}
	}
}
