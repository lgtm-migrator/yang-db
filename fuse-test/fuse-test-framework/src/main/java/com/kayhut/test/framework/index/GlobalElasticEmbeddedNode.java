package com.kayhut.test.framework.index;

import com.kayhut.test.framework.index.ElasticEmbeddedNode;

import java.util.Collections;
import java.util.Map;

/**
 * Created by roman.margolis on 01/01/2018.
 */
public class GlobalElasticEmbeddedNode {

    public static ElasticEmbeddedNode getInstance() throws Exception {
        return getInstance("fuse.test_elastic");
    }

    public static ElasticEmbeddedNode getInstance(String nodeName) throws Exception {
        if (instance == null) {
            instance = new ElasticEmbeddedNode("target/es", 9200, 9300, nodeName);
        }

        return instance;
    }

    private static ElasticEmbeddedNode instance;
}
