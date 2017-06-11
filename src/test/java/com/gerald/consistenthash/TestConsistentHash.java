package com.gerald.consistenthash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.gerald.consistenthash.hashalgrithoms.DefaultHashAlgrithom;
import com.gerald.consistenthash.hashalgrithoms.MurmurHashAlgrithom;

public class TestConsistentHash {
    private final List<String> uuids = new ArrayList<>();
    
    private final int num = 5000000; 
    
    public TestConsistentHash() {
        for(int i = 0; i < num; ++i) {
            uuids.add(String.valueOf(i));
        }
    }
    
    public static class HashResult {
        public Map<Node, Integer> distribution;
        
        public int max;
        
        public int min;
        
        public double mean;
        
        public double var;
        
        public String toString() {
            StringBuilder builder = new StringBuilder();
            
            builder.append("====================================\n");
            
            for(Map.Entry<Node, Integer> entry : distribution.entrySet()) {
                builder.append("node = " + entry.getKey().getId() + ", count = " + entry.getValue() + "\n");
            }
            builder.append("max = " + max).append("\n")
                   .append("min = " + min).append("\n")
                   .append("mean = " + mean).append("\n")
                   .append("var = " + var).append("\n");
            
            builder.append("====================================\n");
            
            return builder.toString();
        }
    }
    
    private HashResult calc(ConsistentHash hash) {        
        Map<Node, Integer> nodeInfo = new HashMap<>();
        for(Node node : hash.getNodes()) {
            nodeInfo.put(node, 0);
        }
        
        for(String uuid : uuids) {
            Node node = hash.hash(uuid);
            nodeInfo.put(node, nodeInfo.get(node) + 1);
        }
        
        int min = Integer.MAX_VALUE;
        int max = 0;
        double mean = 0;
        double var = 0;
        
        for(Integer num : nodeInfo.values()) {
            if(num < min) {
                min = num;
            }
            
            if(num > max) {
                max = num;
            }
            
            mean += num;
        }
        
        mean /= hash.getNodes().size();
        
        for(Integer num : nodeInfo.values()) {
            var += (num - mean) * (num - mean);
        }
        
        var = Math.sqrt(var/hash.getNodes().size());
        
        HashResult result = new HashResult();
        result.distribution = nodeInfo;
        result.max = max;
        result.min = min;
        result.mean = mean;
        result.var = var;
        
        return result;
    }
    
    public static class SimpleNode implements Node, Comparable<SimpleNode> {
        private String id;
        
        public SimpleNode(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }        
        
        @Override
        public boolean equals(Object o) {
            if(o == this) {
                return true;
            }
            
            if(!(o instanceof SimpleNode)) {
                return false;
            }
            
            SimpleNode other = (SimpleNode)o;
            
            return other.id.equals(id);
        }
        
        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public int compareTo(SimpleNode o) {
            return id.compareTo(o.id);
        }
    }
    
    private List<Node> getNodes(int num) {
        List<Node> nodes = new ArrayList<>();

        for(int i = 0; i < num; ++i) {
            nodes.add(new SimpleNode("192.168.31." + (100 + i)));
        }
//        nodes.add(new SimpleNode("jfkafeajflekjaj"));
//        nodes.add(new SimpleNode("3uqo3nfkjameafk"));
//        nodes.add(new SimpleNode("knf3iafneajfneo"));
//        nodes.add(new SimpleNode("fnajfneiajfn3kj"));
//        nodes.add(new SimpleNode("h833nr93jkanfak"));
//        nodes.add(new SimpleNode("793jrhfdsljnlsn"));
//        nodes.add(new SimpleNode("32jnfoajnefknkk"));
//        nodes.add(new SimpleNode("32rfh893io13nks"));
//        nodes.add(new SimpleNode("32ijrf83iwnfaik"));
//        nodes.add(new SimpleNode("03k3rlfkj3kfi3j"));
        
        return nodes;
    }
    
    /**
     * 这个案例测试了，随着虚拟节点数量的增加，各个物理节点上的数据分布趋于均衡(均方差逐渐变小)。
     * 对于500万条数据(uuid)，10个节点，复制因子为91时。均方差在5w左右。而当复制因子为1，即退回到
     * 基本的一致性hash算法时，均方差达到40w左右，节点间的分布极不均衡。
     * 
     * <p>
     * 这里使用了Murmur Hash算法。对于随机输入的字符串，例如UUID，Murmur Hash的性能(计算效率和冲突概率)与Java
     * 的默认实现相比，没有优势。但是，当输入的字符串变得有规律时(例如输入的字符串是自增连续主键或者连续的ip地址)，Murmur的计算
     * 结果可以更有效的将hashCode分散到整数空间中，而Java的hashCode实现，则会产生连续的hash值。
     * </p>
     * 
     * <p>
     * 在简单hash中，连续不冲突的hash值不会影响性能，例如hash set。但是在一致性hash中，hash值会被分配给最近的节点，
     * 导致连续hash会命中同一个节点，无法实现负载均衡
     * </p>
     */
    @Test
    public void testDistribution() {
        List<Node> nodes = getNodes(10);
        
        Map<Integer, Double> var = new TreeMap<>();
        for(int i = 1; i <= 100; i+=10) {
            ConsistentHash hash = new VirtualNodeConsistentHash(nodes, new MurmurHashAlgrithom(), i);
            
            HashResult result = calc(hash);
            
            System.out.println(result.toString());
            
            var.put(i, result.var);
        }
        
        System.out.println(var);
    }
    
    private static class MigrationResult {
        public int migrationTotal = 0;
        
        public Map<Node, Integer> nodeMigration = new TreeMap<>();
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            int nodeNum = 0;
            
            builder.append("========================================\n");
   
            for(Map.Entry<Node, Integer> node : nodeMigration.entrySet()) {
                builder.append("node = ").append(node.getKey().getId()).append(", ")
                       .append("number = ").append(node.getValue()).append("\n");
                if(node.getValue() > 0) {
                    ++nodeNum;
                }
            }
            
            builder.append("total = ").append(migrationTotal).append("\n")
                   .append("affected node number = ").append(nodeNum).append("\n");
            builder.append("========================================\n");
            
            return builder.toString();
        }
    }
    
    private MigrationResult calcMigration(ConsistentHash hash1, ConsistentHash hash2) {
        MigrationResult result = new MigrationResult();
        
        for(Node node : hash1.getNodes()) {
            result.nodeMigration.put(node, 0);
        }
        
        for(String uuid : uuids) {
            Node node1 = hash1.hash(uuid);
            Node node2 = hash2.hash(uuid);
            
            if(!node1.equals(node2)) {
                result.migrationTotal++;
                result.nodeMigration.put(node1, result.nodeMigration.get(node1) + 1);
            }
        }
        
        return result;
    }
    
    /**
     * 这个案例测试了，当节点从n增加到n+1时，数据迁移量。当复制因子为1时，影响的节点数为1；当复制因子增大时，影响的节点数最增加。
     * 因为当复制因子较大时，各节点的数据是趋于均衡的，因此迁移的数据量大概是总数据量的1/(n+1)。
     * <p>
     * 如果考虑数据迁移的空间门限是m(例如，认为当服务器的空间超过m的时候，开始考虑添加新的节点)，那么移动的数据量是nm/(n+1) < m。如果
     * 增加k台服务器，那么需要迁移的数据量是nmk/(n+k) < mk
     * </p>
     */
    @Test
    public void testMigration() {
        List<Node> nodes10 = getNodes(10);
        List<Node> nodes11 = getNodes(15);
        for(int i = 100; i <= 100; ++i) {
            ConsistentHash hash10 = new VirtualNodeConsistentHash(nodes10, new MurmurHashAlgrithom(), i);
            ConsistentHash hash11 = new VirtualNodeConsistentHash(nodes11, new MurmurHashAlgrithom(), i);
            
            MigrationResult result = calcMigration(hash10, hash11);
            
            System.out.println(result.toString());
        }
    }
}
