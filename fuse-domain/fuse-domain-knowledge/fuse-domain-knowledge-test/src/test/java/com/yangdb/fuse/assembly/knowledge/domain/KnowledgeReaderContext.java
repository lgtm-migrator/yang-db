package com.yangdb.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.*;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;
import javaslang.Tuple2;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.Filter.filter;
import static com.yangdb.fuse.model.OntologyTestUtils.NAME;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

public class KnowledgeReaderContext {

    public static final String KNOWLEDGE = "Knowledge";

    static public class KnowledgeQueryBuilder {
        private Query.Builder knowledge;
        private List<EBase> elements;
        private AtomicInteger counter = new AtomicInteger(0);
        public Stack<Quant1> entityStack = new Stack<>();

        private int nextEnum() {
            return counter.incrementAndGet();
        }

        private int currentEnum() {
            return counter.get();
        }

        private EBase current() {
            return elements.get(currentEnum());
        }

        private KnowledgeQueryBuilder() {
            knowledge = Query.Builder.instance().withName(NAME.name).withOnt(KNOWLEDGE);
            elements = new ArrayList<>();
        }

        private Quant1 quant() {
            //case we are in quant scope
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue
            return quant1;
        }

        public static KnowledgeQueryBuilder start() {
            KnowledgeQueryBuilder builder = new KnowledgeQueryBuilder();
            builder.elements.add(new Start(builder.currentEnum(), builder.nextEnum()));
            return builder;
        }

        public KnowledgeQueryBuilder withGlobalEntityValues(String eTag, Filter... filters) {
            return withGlobalEntity(eTag, Collections.EMPTY_LIST,
                    Arrays.asList(filter().with(QuantType.all, "fieldId", Constraint.of(ConstraintOp.inSet,Arrays.asList("title", "nicknames")))));
        }

        public KnowledgeQueryBuilder withGlobalEntity(String eTag) {
            return withGlobalEntity(eTag,Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        }

        public KnowledgeQueryBuilder withGlobalEntity(String eTag, List<Filter> entityFilter, List<Filter> entityValueFilter) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntity", L, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), LogicalEntity.type, LogicalEntity.type, nextEnum(), 0));
            this.elements.add(new Rel(currentEnum(), "hasEntity", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag+"#"+currentEnum(), EntityBuilder.type, nextEnum(), 0));
            //add global quant
            quant();
            //add global rel + values
            elements.add(filter().with(QuantType.all, "context", Constraint.of(ConstraintOp.eq, "global"))
                    .build(currentEnum()));
            //adds to quant
            entityStack.peek().getNext().add(currentEnum());
            nextEnum();//continue
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEvalue", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag+"#"+currentEnum(), ValueBuilder.type, nextEnum(), 0));

            quant();

            entityValueFilter.forEach(filter -> {
                //adds to quant
                entityStack.peek().getNext().add(currentEnum());
                //adds to element
                elements.add(filter.build(currentEnum()));
                //next enum
                nextEnum();//continue
            });
            return this;
        }

        public KnowledgeQueryBuilder withEntity(String eTag, Filter... filters) {
            this.elements.add(new ETyped(currentEnum(), eTag, EntityBuilder.type, nextEnum(), 0));
            quant();
            Arrays.stream(filters).forEach(filter -> {
                entityStack.peek().getNext().add(currentEnum());
                elements.add(filter.build(currentEnum()));
                nextEnum();//continue
            });
            return this;
        }


        public KnowledgeQueryBuilder relatedTo(String eTag, String sideB, Filter... filters) {
            return relatedTo(entityStack.peek(),eTag,sideB,filters);
        }

        public KnowledgeQueryBuilder relatedTo(Quant1 quantEntity, String eTag, String sideB, Filter... filters) {
            quantEntity.getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasRelation", R, EntityBuilder.type, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, RelationBuilder.type, nextEnum(), 0));
            Quant1 quant1 = new Quant1(currentEnum(), QuantType.all, new ArrayList<>(), 0);
            this.elements.add(quant1);
            entityStack.push(quant1);
            nextEnum();//continue

            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasRelation", L, EntityBuilder.type, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), sideB, EntityBuilder.type, nextEnum(), 0));

            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withFile(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEfile", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, FileBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withValue(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEvalue", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, ValueBuilder.type, 0, 0));
            nextEnum();//continue
            Arrays.stream(filters).forEach(filter -> {
                entityStack.peek().getNext().add(currentEnum());
                elements.add(filter.build(currentEnum()));
                nextEnum();//continue
            });
            return this;
        }

        public KnowledgeQueryBuilder withRef(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasEntityReference", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, RefBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public KnowledgeQueryBuilder withInsight(String eTag, Filter... filters) {
            entityStack.peek().getNext().add(currentEnum());
            this.elements.add(new Rel(currentEnum(), "hasInsight", R, null, nextEnum(), 0));
            this.elements.add(new ETyped(currentEnum(), eTag, InsightBuilder.type, 0, 0));
            nextEnum();//continue
            return this;
        }

        public Query build() {
            if (this.elements.get(this.elements.size() - 1) instanceof EEntityBase) {
                ((EEntityBase) this.elements.get(this.elements.size() - 1)).setNext(0);
            }
            return knowledge.withElements(elements).build();
        }


    }

    static public QueryResourceInfo query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, CreateQueryRequest request) throws IOException {
        return fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(),request);
    }

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query, new CreateGraphCursorRequest());
    }

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo,int pageSize, Query query)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, String query,String ontology)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query,ontology, new CreateGraphCursorRequest());
    }

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo,int pageSize, String query,String ontology)
            throws IOException, InterruptedException {
        return query(fuseClient, fuseResourceInfo, query,ontology, new CreateGraphCursorRequest(new CreatePageRequest(pageSize)));
    }

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        if(queryResourceInfo.getError()!=null) {
            return new AssignmentsQueryResult<Entity,Relation>() {
                @Override
                public int getSize() {
                    return -1;
                }

                public FuseError error() {
                    return queryResourceInfo.getError();
                }

                @Override
                public String toString() {
                    return error().getErrorDescription();
                }
            };
        }

        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), createCursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, createCursorRequest.getCreatePageRequest() != null ? createCursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }

    static public QueryResultBase query(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, String query, String ontology, CreateCursorRequest createCursorRequest)
            throws IOException, InterruptedException {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query,ontology);
        if(queryResourceInfo.getError()!=null) {
            return new AssignmentsQueryResult<Entity,Relation>() {
                @Override
                public int getSize() {
                    return -1;
                }

                public FuseError error() {
                    return queryResourceInfo.getError();
                }

                @Override
                public String toString() {
                    return error().getErrorDescription();
                }
            };
        }

        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), createCursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, createCursorRequest.getCreatePageRequest() != null ? createCursorRequest.getCreatePageRequest().getPageSize() : 1000);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }

    static public QueryResultBase nextPage(FuseClient fuseClient,CursorResourceInfo cursorResourceInfo ,int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, pageSize);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());

    }

    static public QueryResultBase nextPage(FuseClient fuseClient, CursorResourceInfo cursorResourceInfo, TypeReference typeReference, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = getPageResourceInfo(fuseClient, cursorResourceInfo, pageSize);
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl(),typeReference);

    }

    protected static PageResourceInfo getPageResourceInfo(FuseClient fuseClient, CursorResourceInfo cursorResourceInfo, int pageSize) throws IOException, InterruptedException {
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(),pageSize);
        // Waiting until it gets the response
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        return pageResourceInfo;
    }

    public static class Filter {
        private Map<QuantType, List<Tuple2<String, Constraint>>> fields;
        private AtomicInteger eNum;

        private Filter() {
            fields = new HashMap<>();
        }

        public static Filter filter() {
            return new Filter();
        }

        public Filter with(QuantType quantType, String field, Constraint constraint) {
            if (!fields.containsKey(quantType)) {
                fields.put(quantType, new ArrayList<>());
            }
            final List<Tuple2<String, Constraint>> list = fields.get(QuantType.all);
            list.add(new Tuple2<>(field, constraint));
            return this;
        }

        public Filter and(String field, Constraint constraint) {
            return with(QuantType.all, field, constraint);
        }

        public Filter or(String field, Constraint constraint) {
            return with(QuantType.some, field, constraint);
        }

        public EPropGroup build(int eNum) {
            this.eNum = new AtomicInteger(100 * eNum);
            final EPropGroup total = new EPropGroup(eNum);
            fields.forEach((quantType, tuple2s) -> {
                final EPropGroup quantGroup = EPropGroup.of(this.eNum.incrementAndGet(), quantType, new EProp[]{});
                tuple2s.forEach(field -> quantGroup.getProps().add(EProp.of(this.eNum.incrementAndGet(), field._1, field._2)));
                total.getGroups().add(quantGroup);
            });
            return total;
        }
    }

}