package com.kayhut.fuse.test;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.test.util.FuseClient;

import java.util.Arrays;

import static com.kayhut.fuse.model.OntologyTestUtils.FIRE;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.OntologyTestUtils.ORIGINATED_IN;
import static java.util.Collections.singletonList;

public class DragonKingdomQuery4Test extends TestCase {

    public void run(FuseClient fuseClient) throws Exception {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));
        Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_100", "D0", singletonList(NAME.type), 2, 0),
                new Rel(2, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.DRAGON.name), singletonList(NAME.type), 4, 0),
                new Rel(4, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "C", ont.eType$(OntologyTestUtils.KINGDOM.name), singletonList(NAME.type), 6, 0),
                new Rel(6, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 7, 0),
                new ETyped(7, "D", ont.eType$(OntologyTestUtils.DRAGON.name), singletonList(NAME.type), 8, 0),
                new Rel(8, ont.rType$(FIRE.getName()), Rel.Direction.L, null, 9, 0),
                new EConcrete(9, "E", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_200", "D0", singletonList(NAME.type), 0, 0)
        )).build();
        /*Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_100", "D0", singletonList(NAME.type), 2, 0),
                new Rel(2, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();*/


        testAndAssertQuery(query, fuseClient);
    }


}