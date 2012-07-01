/*
 Copyright 2010 Vivin Suresh Paliath
 Distributed under the BSD License
*/

package com.laytonsmith.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericTree<T> {

    private GenericTreeNode<T> root;

    public GenericTree() {
        super();
    }

    private GenericTreeNode<T> auxiliaryFind(GenericTreeNode<T> currentNode, GenericTreeNode<T> nodeToFind) {
        GenericTreeNode<T> returnNode = null;
        int i = 0;

        if (currentNode.equals(nodeToFind)) {
            returnNode = currentNode;
        }

        else if(currentNode.hasChildren()) {
            i = 0;
            while(returnNode == null && i < currentNode.getNumberOfChildren()) {
                returnNode = auxiliaryFind(currentNode.getChildAt(i), nodeToFind);
                i++;
            }
        }

        return returnNode;
    }

    private int auxiliaryGetNumberOfNodes(GenericTreeNode<T> node) {
        int numberOfNodes = node.getNumberOfChildren();

        for(GenericTreeNode<T> child : node.getChildren()) {
            numberOfNodes += auxiliaryGetNumberOfNodes(child);
        }

        return numberOfNodes;
    }

    public List<GenericTreeNode<T>> build(GenericTreeNode<T> node, GenericTreeTraversalOrderEnum traversalOrder) {
        List<GenericTreeNode<T>> traversalResult = new ArrayList<GenericTreeNode<T>>();

        if(traversalOrder == GenericTreeTraversalOrderEnum.PRE_ORDER) {
            buildPreOrder(node, traversalResult);
        }

        else if(traversalOrder == GenericTreeTraversalOrderEnum.POST_ORDER) {
            buildPostOrder(node, traversalResult);
        }

        return traversalResult;
    }

    public List<GenericTreeNode<T>> build(GenericTreeTraversalOrderEnum traversalOrder) {
        List<GenericTreeNode<T>> returnList = null;

        if(root != null) {
            returnList = build(root, traversalOrder);
        }

        return returnList;
    }

    private void buildPostOrder(GenericTreeNode<T> node, List<GenericTreeNode<T>> traversalResult) {
        for(GenericTreeNode<T> child : node.getChildren()) {
            buildPostOrder(child, traversalResult);
        }

        traversalResult.add(node);
    }

    private void buildPostOrderWithDepth(GenericTreeNode<T> node, Map<GenericTreeNode<T>, Integer> traversalResult, int depth) {
        for(GenericTreeNode<T> child : node.getChildren()) {
            buildPostOrderWithDepth(child, traversalResult, depth + 1);
        }

        traversalResult.put(node, depth);
    }

    private synchronized void buildPreOrder(GenericTreeNode<T> node, List<GenericTreeNode<T>> traversalResult) {
        traversalResult.add(node);
        
        for(int i = 0; i < node.getNumberOfChildren(); i++){
        //for(GenericTreeNode<T> child : node.getChildren()) {
            buildPreOrder(node.getChildAt(i), traversalResult);
        }
    }

    private void buildPreOrderWithDepth(GenericTreeNode<T> node, Map<GenericTreeNode<T>, Integer> traversalResult, int depth) {
        traversalResult.put(node, depth);

        for(GenericTreeNode<T> child : node.getChildren()) {
            buildPreOrderWithDepth(child, traversalResult, depth + 1);
        }
    }

    public Map<GenericTreeNode<T>, Integer> buildWithDepth(GenericTreeNode<T> node, GenericTreeTraversalOrderEnum traversalOrder) {
        Map<GenericTreeNode<T>, Integer> traversalResult = new LinkedHashMap<GenericTreeNode<T>, Integer>();

        if(traversalOrder == GenericTreeTraversalOrderEnum.PRE_ORDER) {
            buildPreOrderWithDepth(node, traversalResult, 0);
        }

        else if(traversalOrder == GenericTreeTraversalOrderEnum.POST_ORDER) {
            buildPostOrderWithDepth(node, traversalResult, 0);
        }

        return traversalResult;
    }

    public Map<GenericTreeNode<T>, Integer> buildWithDepth(GenericTreeTraversalOrderEnum traversalOrder) {
        Map<GenericTreeNode<T>, Integer> returnMap = null;

        if(root != null) {
            returnMap = buildWithDepth(root, traversalOrder);
        }

        return returnMap;
    }

    public boolean exists(GenericTreeNode<T> nodeToFind) {
        return (find(nodeToFind) != null);
    }

    public GenericTreeNode<T> find(GenericTreeNode<T> nodeToFind) {
        GenericTreeNode<T> returnNode = null;

        if(root != null) {
            returnNode = auxiliaryFind(root, nodeToFind);
        }

        return returnNode;
    }

    public int getNumberOfNodes() {
        int numberOfNodes = 0;

        if(root != null) {
            numberOfNodes = auxiliaryGetNumberOfNodes(root) + 1; //1 for the root!
        }

        return numberOfNodes;
    }

    public GenericTreeNode<T> getRoot() {
        return this.root;
    }

    public boolean isEmpty() {
        return (root == null);
    }

    public void setRoot(GenericTreeNode<T> root) {
        this.root = root;
    }

    public String toString() {
        /*
        We're going to assume a pre-order traversal by default
         */

        String stringRepresentation = "";

        if(root != null) {
            stringRepresentation = build(GenericTreeTraversalOrderEnum.PRE_ORDER).toString();

        }

        return stringRepresentation;
    }

    public String toStringWithDepth() {
        /*
        We're going to assume a pre-order traversal by default
         */

        String stringRepresentation = "";

        if(root != null) {
            stringRepresentation = buildWithDepth(GenericTreeTraversalOrderEnum.PRE_ORDER).toString();
        }

        return stringRepresentation;
    }
}
