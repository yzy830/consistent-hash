package com.gerald.consistenthash;

import java.util.List;

import com.gerald.consistenthash.hashalgrithoms.HashAlgrithom;

public abstract class ConsistentHash {
    protected final List<Node> nodes;
    
    protected final HashAlgrithom hash;
    
    public ConsistentHash(List<Node> nodes, HashAlgrithom hash) {
        if((nodes == null) || (nodes.size() == 0)) {
            throw new IllegalArgumentException();
        }
        
        this.nodes = nodes;
        this.hash = hash;
    }
    
    public List<Node> getNodes() {
        return nodes;
    }

    public HashAlgrithom getHash() {
        return hash;
    }

    public Node hash(String key) {
        if(nodes.size() == 1) {
            return nodes.get(0);
        } else {
            return doHash(key);
        }
    }
    
    protected abstract Node doHash(String key);
}
