package com.kayhut.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgCompositeQuery;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.ParameterizedQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.kayhut.fuse.model.query.properties.constraint.QueryNamedParameter;
import com.kayhut.fuse.model.resourceInfo.*;
import com.kayhut.fuse.model.results.AssignmentUtils;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import javaslang.control.Option;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.Utils.getOrCreateId;

/**
 * Created by Roman on 12/15/2017.
 */
public abstract class QueryDriverBase implements QueryDriver {
    //region Constructors
    @Inject
    public QueryDriverBase(
            CursorDriver cursorDriver,
            PageDriver pageDriver,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryTransformer<String, AsgQuery> jsonQueryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        this.cursorDriver = cursorDriver;
        this.pageDriver = pageDriver;
        this.queryTransformer = queryTransformer;
        this.jsonQueryTransformer = jsonQueryTransformer;
        this.queryValidator = queryValidator;
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region QueryDriver Implementation
    @Override
    @Deprecated()
    public Optional<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return create(request);
    }

    @Override
    public Optional<Object> run(Query query) {
        String id = UUID.randomUUID().toString();
        try {
            CreateQueryRequest queryRequest = new CreateQueryRequest(id, id, query, new CreateGraphCursorRequest(new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if(!resourceInfo.isPresent())
                return Optional.empty();

            if(resourceInfo.get().getError()!=null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
            delete(id);
        }

    }

    @Override
    public Optional<Object> run(String cypher, String ontology) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, cypher, ontology,new CreateGraphCursorRequest(new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if(!resourceInfo.isPresent())
                return Optional.empty();

            if(resourceInfo.get().getError()!=null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
            delete(id);
        }
    }


    @Override
    public Optional<Object> getNextPageData(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        try {
            if (!resourceStore.getQueryResource(queryId).isPresent())
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                "Query with id[" + queryId + "] not found in store")));

            QueryResource queryResource = resourceStore.getQueryResource(queryId).get();
            final String cursorID = cursorId.orElse(queryResource.getCurrentCursorId());
            final Optional<PageResourceInfo> info = pageDriver.create(queryId, cursorID, pageSize);
            if (!info.isPresent())
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(), "failed fetching next page for query " + queryId)));

            final PageResourceInfo pageResourceInfo = info.get();

            if (deleteCurrentPage) {
                final String priorPageId = resourceStore.getCursorResource(queryId, cursorID).get().getPriorPageId();
                pageDriver.delete(queryId, cursorID, priorPageId);
            }
            return pageDriver.getData(queryId, cursorID, pageResourceInfo.getResourceId());
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));
        }
    }

    /**
     * internal api
     *
     * @param request
     * @param metadata
     * @param query
     * @return
     */
    private Optional<QueryResourceInfo> create(CreateQueryRequest request, QueryMetadata metadata, Query query) {
        try {
            AsgQuery asgQuery = transform(query);

            ValidationResult validationResult = this.queryValidator.validate(asgQuery);

            if (!validationResult.valid()) {
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                validationResult.getValidator() + ":"
                                        + Arrays.toString(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)))));
            }

            //create inner query
            final List<QueryResource> innerQuery = createInnerQuery(request, metadata, asgQuery);
            //outer most query resource
            this.resourceStore.addQueryResource(createResource(request, query, innerQuery, asgQuery, metadata)
                    .withInnerQueryResources(innerQuery));

            final List<QueryResourceInfo> collect = innerQuery.stream().map(qr -> new QueryResourceInfo(
                    metadata.getType(),
                    urlSupplier.resourceUrl(qr.getQueryMetadata().getId()),
                    qr.getQueryMetadata().getId(),
                    urlSupplier.cursorStoreUrl(qr.getQueryMetadata().getId())))
                    .collect(Collectors.toList());

            return Optional.of(new QueryResourceInfo(
                    metadata.getType(),
                    urlSupplier.resourceUrl(metadata.getId()),
                    metadata.getId(),
                    urlSupplier.cursorStoreUrl(metadata.getId()))
                    .withInnerQueryResources(collect));
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));
        }
    }

    /**
     * add inner query to repository with related parent query name
     *
     * @param request
     * @param metadata
     * @param asgQuery
     */
    private List<QueryResource> createInnerQuery(CreateQueryRequestMetadata request, QueryMetadata metadata, AsgQuery asgQuery) {
        if (asgQuery instanceof AsgCompositeQuery) {
            return ((AsgCompositeQuery) asgQuery).getQueryChain().stream().map(asgQ -> {
                ValidationResult validate = this.queryValidator.validate(asgQuery);
                if (!validate.valid()) {
                    throw new IllegalArgumentException(validate.toString());
                }
                //inner recursive query hierarchy
                final List<QueryResource> innerQuery = createInnerQuery(request, metadata, asgQ);

                Query q = asgQ.getOrigin();
                final QueryResource resource = createResource(
                        new CreateQueryRequest(request.getId() + "->" + q.getName(),
                                request.getName() + "->" + q.getName(), q), q, innerQuery, asgQ,
                        new QueryMetadata(CreateQueryRequestMetadata.StorageType._volatile,
                                request.getId() + "->" + q.getName(),
                                request.getName() + "->" + q.getName(),
                                metadata.isSearchPlan(),
                                metadata.getCreationTime(),
                                metadata.getTtl()))
                        .withInnerQueryResources(innerQuery);
                this.resourceStore.addQueryResource(resource);
                return resource;
                //return query resource
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * internal api
     *
     * @param request
     * @param metadata
     * @param query
     * @return
     */
    protected Optional<QueryResourceInfo> create(CreateJsonQueryRequest request, QueryMetadata metadata, String query) {
        try {
            AsgQuery asgQuery = transform(query);
            asgQuery.setName(metadata.getName());
            asgQuery.setOnt(request.getOntology());

            ValidationResult validationResult = validateAsgQuery(asgQuery);
            if (!validationResult.valid()) {
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                validationResult.getValidator() + ":" + Arrays.toString(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)))));
            }

            Query build = Query.Builder.instance()
                    .withOnt(request.getOntology())
                    .withName(query).build();

            //create inner query
            List<QueryResource> innerQuery = createInnerQuery(request, metadata, asgQuery);
            //outer most query resource
            this.resourceStore.addQueryResource(createResource(
                    new CreateQueryRequest(request.getId(), request.getName(), build, request.getPlanTraceOptions(), request.getCreateCursorRequest())
                    , build
                    , innerQuery
                    , asgQuery
                    , metadata));

            return Optional.of(new QueryResourceInfo(
                    metadata.getType(),
                    urlSupplier.resourceUrl(metadata.getId()),
                    metadata.getId(),
                    urlSupplier.cursorStoreUrl(metadata.getId())));
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));
        }
    }

    protected ValidationResult validateAsgQuery(AsgQuery query) {
        return this.queryValidator.validate(query);
    }

    public ValidationResult validateQuery(Query query) {
        AsgQuery asgQuery = transform(query);
        if (!validateAsgQuery(asgQuery).valid())
            return validateAsgQuery(asgQuery);
        return validateAsgQuery(rewrite(asgQuery));
    }

    protected AsgQuery transform(Query query) {
        return this.queryTransformer.transform(query);
    }


    protected AsgQuery transform(String query) {
        return this.jsonQueryTransformer.transform(query);
    }

    @Override
    public Optional<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = this.create(request, metadata, request.getQuery());
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));

        }
    }

    @Override
    public Optional<QueryResourceInfo> create(CreateQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = this.create(request, metadata, request.getQuery());
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));

        }

    }

    protected Optional<QueryResourceInfo> getQueryResourceInfo(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
        if (!queryResourceInfo.isPresent() || queryResourceInfo.get().getError() != null) {
            if (queryResourceInfo.get().getError() != null) {
                return Optional.of(new QueryResourceInfo().error(queryResourceInfo.get().getError()));
            }
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "Failed creating cursor resource from given request: \n" + request.toString())));

        }

        if (request.getCreateCursorRequest() == null) {
            return queryResourceInfo;
        }

        Optional<CursorResourceInfo> cursorResourceInfo = this.cursorDriver.create(queryResourceInfo.get().getResourceId(), request.getCreateCursorRequest());
        if (!cursorResourceInfo.isPresent()) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "Failed creating cursor resource from given request: \n" + request.toString())));
        }

        if (request.getCreateCursorRequest().getCreatePageRequest() == null) {
            return Optional.of(new QueryResourceInfo(
                    queryResourceInfo.get().getType(),
                    queryResourceInfo.get().getResourceUrl(),
                    queryResourceInfo.get().getResourceId(),
                    cursorResourceInfo.get().getPageStoreUrl(),
                    cursorResourceInfo.get()));
        }

        Optional<PageResourceInfo> pageResourceInfo = this.pageDriver.create(
                queryResourceInfo.get().getResourceId(),
                cursorResourceInfo.get().getResourceId(),
                request.getCreateCursorRequest().getCreatePageRequest().getPageSize());

        if (!pageResourceInfo.isPresent()) {
            return Optional.of(
                    new QueryResourceInfo(
                            queryResourceInfo.get().getType(),
                            queryResourceInfo.get().getResourceUrl(),
                            queryResourceInfo.get().getResourceId(),
                            cursorResourceInfo.get().getPageStoreUrl(),
                            cursorResourceInfo.get()
                    ).error(new FuseError(Query.class.getSimpleName(), "Failed creating page resource from given request: \n" + request.toString())));
        }

        cursorResourceInfo.get().setPageResourceInfos(Collections.singletonList(pageResourceInfo.get()));

        //in parameterized query - extract inner query result data and call the outer query as callable parameterized query
        if (queryResourceInfo.get().getType() == QueryMetadata.Type.parameterized) {
            Optional<QueryResourceInfo> resourceInfo = call(new ExecuteStoredQueryRequest(
                    "call[" + request.getId()+"]",
                    request.getId(),
                    request.getCreateCursorRequest(),
                    extractInnerQueryParams(queryResourceInfo.get()),
                    Collections.emptyList()
            ));
            //return the called query call_[***] instead of the origin ***
            if (resourceInfo.isPresent()) {
                return resourceInfo;
            }
        }
        // ELSE - concrete query
        Optional<Object> pageDataResponse = pageDriver.getData(queryResourceInfo.get().getResourceId(),
                cursorResourceInfo.get().getResourceId(),
                pageResourceInfo.get().getResourceId());

        if (!pageDataResponse.isPresent()) {
            return Optional.of(
                    new QueryResourceInfo(
                            queryResourceInfo.get().getType(),
                            queryResourceInfo.get().getResourceUrl(),
                            queryResourceInfo.get().getResourceId(),
                            cursorResourceInfo.get().getPageStoreUrl(),
                            cursorResourceInfo.get()
                    ).error(new FuseError(Query.class.getSimpleName(), "Failed fetching page data from given request: \n" + request.toString())));
        }
        //populate data on page
        pageResourceInfo.get().setData(pageDataResponse.get());

        return Optional.of(
                new QueryResourceInfo(
                        queryResourceInfo.get().getType(),
                        queryResourceInfo.get().getResourceUrl(),
                        queryResourceInfo.get().getResourceId(),
                        cursorResourceInfo.get().getPageStoreUrl(),
                        cursorResourceInfo.get()
                ));
    }

    private Collection<NamedParameter> extractInnerQueryParams(QueryResourceInfo queryResourceInfo) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryResourceInfo.getResourceId());
        List<EProp> parameterizedConstraints = AsgQueryUtil.getParameterizedConstraintEProps(queryResource.get().getAsgQuery());
        return parameterizedConstraints.stream()
                .map(eProp -> extractQueryProjectedParams(queryResource.get(), eProp))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<NamedParameter> extractQueryProjectedParams(QueryResource queryResource, EProp eProp) {
        ParameterizedConstraint con = (ParameterizedConstraint) eProp.getCon();
        QueryNamedParameter namedParameter = (QueryNamedParameter) con.getExpr();
        String query = namedParameter.getQuery();
        Option<QueryResource> innerQuery = Stream.ofAll(queryResource.getInnerQueryResources())
                .find(p -> p.getQuery().getName().equals(query));

        if (!innerQuery.isEmpty()) {
            CursorResource cursorResource = innerQuery.get().getCursorResource(innerQuery.get().getCurrentCursorId()).get();
            PageResource pageResource = cursorResource.getPageResource(cursorResource.getCurrentPageId()).get();
            AssignmentsQueryResult result = (AssignmentsQueryResult) pageResource.getData();
            return Optional.of(AssignmentUtils.collectByTag(result, namedParameter.getName()));
        }

        return Optional.empty();
    }

    protected QueryMetadata getQueryMetadata(CreateQueryRequestMetadata request) {
        String queryId = getOrCreateId(request.getId());
        return new QueryMetadata(request.getStorageType(), queryId, request.getName(), request.isSearchPlan(), System.currentTimeMillis(), request.getTtl());
    }

    @Override
    public Optional<QueryResourceInfo> call(ExecuteStoredQueryRequest callRequest) {
        try {
            if (!resourceStore.getQueryResource(callRequest.getQuery().getName()).isPresent())
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                "Query with id[" + callRequest.getQuery().getName() + "] not found in store")));

            QueryResource queryResource = resourceStore.getQueryResource(callRequest.getQuery().getName()).get();
            final CreateQueryRequest storedRequest = queryResource.getRequest();

            //get cursor request - letting the calling request override the sored page request
            CreateCursorRequest cursorRequest = (callRequest.getCreateCursorRequest() != null
                    ? callRequest.getCreateCursorRequest()
                    : storedRequest.getCreateCursorRequest());

            //get page request - letting the calling request override the sored page request
            CreatePageRequest pageRequest = (callRequest.getPageCursorRequest() != null
                    ? callRequest.getPageCursorRequest()
                    : (storedRequest.getCreateCursorRequest() != null
                    ? storedRequest.getCreateCursorRequest().getCreatePageRequest()
                    : new CreatePageRequest()));

            //set pageSize attribute on PageCursorRequest using the given execution params
            callRequest.getExecutionParams().stream().filter(p -> p.getName().equals("pageSize")).findAny()
                    .ifPresent(v -> pageRequest.setPageSize((Integer) v.getValue()));

            //create the new volatile query
            Optional<QueryResourceInfo> info = create(new CreateQueryRequest(
                    callRequest.getId(),
                    callRequest.getName(),
                    new ParameterizedQuery(queryResource.getQuery(), callRequest.getParameters()),
                    callRequest.getPlanTraceOptions(),
                    cursorRequest.with(pageRequest)));
            //remove volatile query after execution returns result - should this be done right away since more pages can be requested ...
            //resourceStore.deleteQueryResource(callRequest.getId());
            return info;
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));
        }
    }

    @Override
    public Optional<StoreResourceInfo> getInfo() {
        Iterable<String> resourceUrls = Stream.ofAll(this.resourceStore.getQueryResources())
                .sortBy(queryResource -> queryResource.getQueryMetadata().getCreationTime())
                .map(queryResource -> queryResource.getQueryMetadata().getId())
                .map(this.urlSupplier::resourceUrl)
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.queryStoreUrl(), null, resourceUrls));
    }

    @Override
    public Optional<QueryResourceInfo> getInfo(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        final List<QueryResourceInfo> collect = Stream.ofAll(queryResource.get().getInnerQueryResources())
                .map(qr ->
                        new QueryResourceInfo(
                                qr.getQueryMetadata().getType(),
                                urlSupplier.resourceUrl(
                                        qr.getQueryMetadata().getId()),
                                qr.getQueryMetadata().getId(),
                                urlSupplier.cursorStoreUrl(qr.getQueryMetadata().getId())))
                .toJavaList();

        QueryResourceInfo resourceInfo =
                new QueryResourceInfo(
                        queryResource.get().getQueryMetadata().getType(),
                        urlSupplier.resourceUrl(queryId),
                        queryId,
                        urlSupplier.cursorStoreUrl(queryId))
                        .withInnerQueryResources(collect);
        return Optional.of(resourceInfo);
    }

    @Override
    public Optional<AsgQuery> getAsg(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getAsgQuery());
    }

    @Override
    public Optional<Query> getV1(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getQuery());
    }

    @Override
    public Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getExecutionPlan());
    }

    @Override
    public Optional<PlanNode<Plan>> planVerbose(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return queryResource.get().getPlanNode();
    }

    @Override
    public Optional<Boolean> delete(String queryId) {
        Optional<QueryResource> resource = resourceStore.getQueryResource(queryId);
        if (!resource.isPresent())
            return Optional.of(Boolean.FALSE);
        //try delete inner queries
        resource.get().getInnerQueryResources().forEach(inner -> delete(inner.getQueryMetadata().getId()));
        return Optional.of(resourceStore.deleteQueryResource(queryId));
    }

    @Override
    public Optional<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
        AsgQuery asgQuery = transform(query);
        if (!validateAsgQuery(asgQuery).valid())
            return Optional.of(new PlanWithCost.ErrorPlanWithCost(
                    new FuseError("PlanValidationError", validateAsgQuery(asgQuery).toString())));

        AsgQuery rewrite = rewrite(asgQuery);
        if (!validateAsgQuery(rewrite).valid())
            return Optional.of(new PlanWithCost.ErrorPlanWithCost(
                    new FuseError("PlanValidationError", validateAsgQuery(rewrite).toString())));

        try {
            return Optional.of(planWithCost(QueryMetadata.random("plan", true), rewrite));
        } catch (Exception e) {
            return Optional.of(new PlanWithCost.ErrorPlanWithCost(new FuseError("NoValidPlanFound", e)));
        }
    }

    @Override
    public Optional<GraphTraversal> traversal(Query query) {
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = plan(query).get();
        return this.cursorDriver.traversal(planWithCost,query.getOnt());
    }


    //endregion

    //region Protected Abstract Methods
    protected abstract QueryResource createResource(CreateQueryRequest request, Query query, List<QueryResource> innerQuery, AsgQuery asgQuery, QueryMetadata metadata);
    protected abstract PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query);

    protected abstract AsgQuery rewrite(AsgQuery asgQuery);
    //endregion

    //region Fields
    private final CursorDriver cursorDriver;
    private final PageDriver pageDriver;
    private QueryTransformer<String, AsgQuery> jsonQueryTransformer;
    private QueryTransformer<Query, AsgQuery> queryTransformer;
    private QueryValidator<AsgQuery> queryValidator;
    private ResourceStore resourceStore;
    private final AppUrlSupplier urlSupplier;
    //endregion
}
