package com.kayhut.fuse.dispatcher.asg;

import com.kayhut.fuse.model.query.EBase;

import java.util.List;

/**
 * Created by liorp on 6/1/2017.
 */
public interface NextFactory {
    //region Public Methods
    List<Integer> supplyNext(EBase eBase);
}
