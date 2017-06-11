package com.gerald.consistenthash;

import java.util.List;
import java.util.TreeMap;

import com.gerald.consistenthash.hashalgrithoms.HashAlgrithom;

public class BaseConsistentHash extends ConsistentHash {
    private TreeMap<Integer, Node> hashNodes = new TreeMap<>();

    public BaseConsistentHash(List<Node> nodes, HashAlgrithom hash) {
        super(nodes, hash);
        
        for(Node node : nodes) {
            hashNodes.put(hash.hash(node.getId()), node);
        }
    }

    @Override
    protected Node doHash(String key) {
        return hashNodes.ceilingEntry(hash.hash(key)).getValue();
    }
}
