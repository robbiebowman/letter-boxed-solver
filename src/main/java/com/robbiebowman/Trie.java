package com.robbiebowman;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Trie {
    private class TrieNode {
        // A map of child nodes indexed by the next character in the key
        private HashMap<Character, TrieNode> children;
        // A flag to indicate that this node represents the end of a key
        private boolean endOfKey;

        public TrieNode() {
            children = new HashMap<>();
            endOfKey = false;
        }
    }

    // The root node of the trie
    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // Inserts a key into the trie
    public void insert(String key) {
        TrieNode current = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            TrieNode node = current.children.get(c);
            if (node == null) {
                node = new TrieNode();
                current.children.put(c, node);
            }
            current = node;
        }
        current.endOfKey = true;
    }

    // Returns true if the trie contains the given key, false otherwise
    public boolean contains(String key) {
        TrieNode current = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            TrieNode node = current.children.get(c);
            if (node == null) {
                return false;
            }
            current = node;
        }
        return current.endOfKey;
    }

    // Returns true if the trie contains a key that starts with the given prefix, false otherwise
    public boolean startsWith(String prefix) {
        TrieNode current = root;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            TrieNode node = current.children.get(c);
            if (node == null) {
                return false;
            }
            current = node;
        }
        return true;
    }


    // Returns true if the trie contains the given key, false otherwise
    TrieNode getNodeOrNull(String key) {
        TrieNode current = root;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            TrieNode node = current.children.get(c);
            if (node == null) {
                return null;
            }
            current = node;
        }
        return current;
    }

    public Set<String> getChildWords(String prefix) {
        var currentNode = getNodeOrNull(prefix);
        return getAllChildWords(currentNode, prefix);
    }

    private Set<String> getAllChildWords(TrieNode node, String currentWord) {
        Set<String> words = new HashSet<>();
        if (node.endOfKey) {
            words.add(currentWord);
        }
        node.children.forEach((ch, childNode) -> words.addAll(getAllChildWords(childNode, currentWord + ch)));
        return words;
    }
}