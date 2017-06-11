package com.gerald.consistenthash.hashalgrithoms;

public class DefaultHashAlgrithom implements HashAlgrithom {

    @Override
    public int hash(String key) {
        return key.hashCode();
    }

}
