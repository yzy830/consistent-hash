package com.gerald.consistenthash;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.gerald.consistenthash.hashalgrithoms.DefaultHashAlgrithom;
import com.gerald.consistenthash.hashalgrithoms.HashAlgrithom;
import com.gerald.consistenthash.hashalgrithoms.MurmurHashAlgrithom;

public class TestHashPerformance {
    private final List<String> uuids = new ArrayList<>();
    
    private final int uuidNum = 5000000;
    
    private static final Random r = new Random();
    
    public TestHashPerformance() {   
        byte[] bytes = new byte[4];
        
        for(int i = 0; i < uuidNum; ++i) {
            r.nextBytes(bytes);
            uuids.add(bytes[0] + "." + bytes[1] + "." + bytes[2] + "." + bytes[3]);
        }
    }
    
    private void testPerformace(HashAlgrithom hash) {
        Date start = new Date();
        
        for(String uuid: uuids) {
            hash.hash(uuid);
        }
        
        Date end = new Date();
        
        System.out.println(hash.getClass().getName() + ", time = " + (end.getTime() - start.getTime()));
    }
    
//    @Test
//    public void testDefaultPerformance() {
//        testPerformace(new DefaultHashAlgrithom());     
//    }
//    
//    @Test
//    public void testMurmurPerformance() {
//        testPerformace(new MurmurHashAlgrithom());
//    }
    
    private void testCollision(HashAlgrithom hash) {
        Map<Integer, Integer> map = new HashMap<>();
        int collisionNum = 0;
        int max = 0;
        int min = Integer.MAX_VALUE;
        
        for(String uuid: uuids) {
            int key = hash.hash(uuid);
            if(map.containsKey(key)) {
                map.put(key, map.get(key) + 1);
                collisionNum++;
            } else {
                map.put(key, 1);
            }
        }
        
        for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if(entry.getValue() > max) {
                max = entry.getValue();
            }
            
            if(entry.getValue() < min) {
                min = entry.getValue();
            }
        }
        
        System.out.println(hash.getClass().getName() + 
                           ", count = " + collisionNum + 
                           ", max = " + max + 
                           ", min = " + min);
    }
    
    @Test
    public void testDefaultCollision() {
        testCollision(new DefaultHashAlgrithom());
    }
    
    @Test
    public void testMurmurCollision() {
        testCollision(new MurmurHashAlgrithom());
    }
}
