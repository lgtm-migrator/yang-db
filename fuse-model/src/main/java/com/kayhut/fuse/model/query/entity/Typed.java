package com.kayhut.fuse.model.query.entity;

/**
 * Created by liorp on 4/26/2017.
 */
public interface Typed {

    interface eTyped {
        void seteType(int eType);

        int geteType();
    }

    interface rTyped {
        void setrType(int rType);

        int getrType();
    }
}
