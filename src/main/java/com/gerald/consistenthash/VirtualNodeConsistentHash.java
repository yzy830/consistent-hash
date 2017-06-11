package com.gerald.consistenthash;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.gerald.consistenthash.hashalgrithoms.HashAlgrithom;

public class VirtualNodeConsistentHash extends ConsistentHash {
    private final int replicas;
    
    private final TreeMap<Integer, Node> hashNodes = new TreeMap<>();
    
    public VirtualNodeConsistentHash(List<Node> nodes, HashAlgrithom hash, int replicas) {
        super(nodes, hash);
        this.replicas = replicas;
        
        for(Node node : nodes) {
            for(int i = 0; i< replicas; ++i) {
                String virtualNodeId = node.getId() + "#" +i;
                hashNodes.put(hash.hash(virtualNodeId), node);
            }
        }
    }

    public int getReplicas() {
        return replicas;
    }

    @Override
    protected Node doHash(String key) {
        Entry<Integer, Node> entry = hashNodes.ceilingEntry(hash.hash(key));
        if(entry != null) {
            return entry.getValue();
        } else {
            return hashNodes.firstEntry().getValue();
        }
    }
}
