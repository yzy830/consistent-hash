package com.gerald.consistenthash.hashalgrithoms;

import com.gerald.consistenthash.util.MurmurHash3;

public class MurmurHashAlgrithom implements HashAlgrithom {

    public int hash(String key) {
        return MurmurHash3.murmurhash3_x86_32(key, 0, key.length(), 0xc58f1a7b);
    }
}
