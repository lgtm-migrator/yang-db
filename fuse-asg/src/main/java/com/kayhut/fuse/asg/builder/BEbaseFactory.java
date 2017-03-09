package com.kayhut.fuse.asg.builder;

import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.aggregation.*;
import com.kayhut.fuse.model.query.combiner.HComb;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.HQuant;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;

import java.util.*;
import java.util.function.Function;

/**
 * Created by benishue on 01-Mar-17.
 */

public class BEbaseFactory {

    //region Constructor
    public BEbaseFactory() {
        this.map = new HashMap<>() ;
        this.map.put(AggM5.class, (ebase) -> ((AggM5)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggM5) ebase).getB()));
        this.map.put(AggM4.class, (ebase) -> ((AggM4)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggM4) ebase).getB()));
        this.map.put(AggM3.class, (ebase) -> ((AggM3)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggM3) ebase).getB()));
        this.map.put(AggM2.class, (ebase) -> ((AggM2)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggM2) ebase).getB()));
        this.map.put(AggM1.class, (ebase) -> ((AggM1)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggM1) ebase).getB()));
        this.map.put(AggL3.class, (ebase) -> ((AggL3)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggL3) ebase).getB()));
        this.map.put(AggL2.class, (ebase) -> ((AggL2)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggL2) ebase).getB()));
        this.map.put(AggL1.class, (ebase) -> ((AggL1)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((AggL1) ebase).getB()));
        this.map.put(HComb.class, (ebase) -> ((HComb)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((HComb) ebase).getB()));
        this.map.put(HQuant.class, (ebase) -> ((HQuant)ebase).getB());
        this.map.put(Quant1.class, (ebase) -> ((Quant1)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((Quant1) ebase).getB()));
        this.map.put(Quant2.class, (ebase) -> (Collections.emptyList()));
        this.map.put(Rel.class, (ebase) -> ((Rel)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((Rel) ebase).getB()));
        this.map.put(RelProp.class, (ebase) -> ((RelProp)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((RelProp) ebase).getB()));
        this.map.put(ETyped.class, (ebase) -> ((ETyped)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((ETyped) ebase).getB()));
        this.map.put(EUntyped.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EAgg.class, (ebase) -> (Collections.emptyList()));
        this.map.put(EConcrete.class, (ebase) -> (Collections.emptyList()));
        this.map.put(ELog.class, (ebase) -> (Collections.emptyList()));
        this.map.put(Start.class, (ebase) -> ((Start)ebase).getB() == 0 ? Collections.emptyList() : Arrays.asList(((Start) ebase).getB()));
    }
    //endregion

    //region Public Methods
    public List<Integer> supply(EBase eBase) {
        return this.map.get(eBase.getClass()).apply(eBase);
    }
    //endregion

    //region Fields
    private Map<Class, Function<EBase, List<Integer>>> map;
    //endregion
}