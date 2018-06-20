package com.kayhut.fuse.assembly.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.assembly.knowlegde.KnowledgeDataInfraManager;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.collection.Stream;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import static com.kayhut.fuse.assembly.knowlegde.KnowledgeDataInfraManager.ENTITY;
import static com.kayhut.fuse.assembly.knowlegde.KnowledgeDataInfraManager.PGE;
import java.security.MessageDigest;


public class KnowledgeAutomationFunctions {

    static public void CreateKnowledgeEntity(ObjectMapper mapper, KnowledgeDataInfraManager manager, TransportClient client, String type,
                                             String logicalId, String context, String category, String lastUpdateUser,
                                             String creationUser, String lastUpdateTime, String creationTime,
                                             Integer authorizationCount, ArrayNode authorizationNode, ArrayNode refsNode)
            throws IOException
    {
        ArrayList<String> entities = new ArrayList<>();
        //create knowledge entity
        ObjectNode on = mapper.createObjectNode();
        on.put("type", type);
        on.put("logicalId", logicalId);
        on.put("context", context);
        on.put("category", category);
        on.put("lastUpdateUser", lastUpdateUser);
        on.put("creationUser", creationUser);
        on.put("lastUpdateTime", lastUpdateTime);
        on.put("creationTime", creationTime);
        on.put("authorizationCount", authorizationCount);
        on.put("authorization", authorizationNode); // Authorization = Clearance
        on.put("refs", refsNode);

        entities.add(mapper.writeValueAsString(on));
        BulkRequestBuilder bulk = client.prepareBulk();
        // Insert knowledge entity directly to elastic
        insertEntities("e1", manager.getSchema(), client, bulk, entities);
    }

    static public void CreateKnowledgeReference(KnowledgeDataInfraManager manager, TransportClient client, int refNum) {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        BulkRequestBuilder bulk = client.prepareBulk();
        RawSchema schema = manager.getSchema();
        for (int refId = 0; refId < refNum; refId++) {
            String referenceId = "ref" + String.format(schema.getIdFormat("reference"), refId + 1);
            String index = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(referenceId)
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setSource(new MapBuilder<String, Object>()
                            .put("type", "reference")
                            .put("title", "Title of - " + referenceId)
                            .put("url", "https://stackoverflow.com/questions")
                            .put("value", "But I must explain to you how all this works")
                            .put("system", "system1")
                            .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                            .put("authorizationCount", 1)
                            .put("lastUpdateUser", "2018-05-27 14:32:56.533")
                            .put("lastUpdateTime", "2018-05-27 14:32:56.533")
                            .put("creationUser", "2018-05-27 14:32:56.533")
                            .put("creationTime", "2018-05-27 14:32:56.533").get()));
        }
        int count = bulk.execute().actionGet().getItems().length;
        System.out.println("There are " + count + " references");
    }

    static List<String> domains = Arrays.asList("com", "co.uk", "gov", "org", "net", "me", "ac");
    static Random random = new Random();
    static List<String> contexts = Arrays.asList("context1", "context2", "context3", "global");
    static List<String> contents = Arrays.asList(
            "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was",
            "born and I will give you a complete account of the system, and expound the actual teachings of the");
    static  List<String> users = Arrays.asList("Tonette Kwon", "Georgiana Vanasse", "Tena Barriere", "Sharilyn Dennis");
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    static public void CreateKnowledgeReferences(KnowledgeDataInfraManager manager, TransportClient client, int refNum) {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        BulkRequestBuilder bulk = client.prepareBulk();
        RawSchema schema = manager.getSchema();
        for (int refId = 0; refId < refNum; refId++) {
            String referenceId = "ref" + String.format(schema.getIdFormat("reference"), refId);
            String index = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                    .filter(partition -> partition.isWithin(referenceId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

            bulk.add(client.prepareIndex().setIndex(index).setType("pge").setId(referenceId)
                .setOpType(IndexRequest.OpType.INDEX)
                .setSource(new MapBuilder<String, Object>()
                        .put("type", "reference")
                        .put("title", "Title of - " + referenceId)
                        .put("url", "http://" + UUID.randomUUID().toString() + "." + domains.get(random.nextInt(domains.size())))
                        .put("value", contents.get(random.nextInt(contents.size())))
                        .put("system", "system" + random.nextInt(10))
                        .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
                        .put("authorizationCount", 1)
                        .put("lastUpdateUser", users.get(random.nextInt(users.size())))
                        .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
                        .put("creationUser", users.get(random.nextInt(users.size())))
                        .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));
        }
        int count = bulk.execute().actionGet().getItems().length;
        System.out.println("There are " + count + " references");
    }


    static public QueryResultBase FetchCreatedEntity(FuseClient fuseClient, FuseResourceInfo fuseResourceInfo, Query query)
            throws IOException, InterruptedException
    {
        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        // Create object of cursorRequest
        CreateGraphCursorRequest cursorRequest = new CreateGraphCursorRequest();
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), cursorRequest);
        // Press on page to get the relevant page
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);
        // Waiting until it gets the response
        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }
        // return the relevant data
        return fuseClient.getPageData(pageResourceInfo.getDataUrl());
    }


    static public void insertEntities(String index, RawSchema schema, Client client, BulkRequestBuilder bulk, List<String> entities ) {

        for(int i = 0; i < entities.size(); i++) {
            String mylogicalId = "e" + String.format(schema.getIdFormat(ENTITY), i);
            IndexRequestBuilder request = client.prepareIndex()
                    .setIndex(index)
                    .setType(PGE)
                    .setId(mylogicalId + "." + "context" + (i+1))
                    .setOpType(IndexRequest.OpType.INDEX)
                    .setRouting(mylogicalId)
                    .setSource(entities.get(i), XContentType.JSON);
            bulk.add(request).get();
        }
    }



}