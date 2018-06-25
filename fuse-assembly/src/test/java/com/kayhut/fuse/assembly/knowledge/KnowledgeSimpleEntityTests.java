package com.kayhut.fuse.assembly.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowlegde.KnowledgeDataInfraManager;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.GlobalElasticEmbeddedNode;
import javaslang.collection.Stream;
import org.elasticsearch.client.transport.TransportClient;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.kayhut.fuse.assembly.knowledge.KnowledgeAutomationFunctions.CreateKnowledgeEntity;
import static com.kayhut.fuse.assembly.knowledge.KnowledgeAutomationFunctions.FetchCreatedEntity;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.*;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


public class KnowledgeSimpleEntityTests {
    static long start;
    static Jooby app;
    static ElasticEmbeddedNode elasticEmbeddedNode;
    static KnowledgeDataInfraManager manager;
    static FuseClient fuseClient;
    ObjectMapper _mapper = new ObjectMapper();
    static TransportClient client = null;

    @BeforeClass
    public static void setup() throws Exception {
        // Start embedded ES
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();
        // Load fuse engine config file
        String confFilePath = Paths.get("src", "test", "resources", "application.test.engine3.m1.dfs.knowledge.public.conf").toString();
        // Start elastic data manager
        manager = new KnowledgeDataInfraManager(confFilePath);
        // Connect to elastic
        client = elasticEmbeddedNode.getClient();
        // Create indexes by templates
        manager.init(client);
        start = System.currentTimeMillis();
        // Start fuse app (based on Jooby app web server)
        app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("src", "test", "resources", "application.test.engine3.m1.dfs.knowledge.public.conf").toString()),
                        "activeProfile");
        app.start("server.join=false");
        //create fuse client class for web api access
        fuseClient = new FuseClient("http://localhost:8888/fuse");
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("tearDownClass");
    }


    @Test
    public void testInsertOneSimpleEntityWithBuilder() throws IOException, InterruptedException {
        KnowledgeWriterContext ctx = KnowledgeWriterContext.init(client,manager.getSchema());
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        Assert.assertEquals(1,KnowledgeWriterContext.commit(ctx,INDEX, e1));

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Get ontology from fuse catalog
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName(NAME.name).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", $ont.eType$("Entity"), 2, 0)
                )).build();
        // Read Entity (with V1 query)
        QueryResultBase pageData = FetchCreatedEntity(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance().withEntity(e1.toEntity()).build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false);
    }

//    @Test
    public void testInsertOneSimpleEntity() throws IOException, InterruptedException {
        // Clearance to Reference
        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");
        // Link to Reference
        ArrayNode refsNode = _mapper.createArrayNode();
        //refsNode.add("ref" + String.format(manager.getSchema().getIdFormat("reference"), 1));
        // LogicalId
        String logicalId = "e" + String.format(manager.getSchema().getIdFormat("entity"), 1);

        // Create entity ObjectNode and insert knowledge entity directly to ES
        CreateKnowledgeEntity(_mapper, manager, client, "entity", logicalId, "context1", "person", "Kobi", "Shaul",
                "2018-05-27 14:32:56.533", "2018-05-26 10:02:30.133", 1, authNode, refsNode);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Get ontology from fuse catalog
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName(NAME.name).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", $ont.eType$("Entity"), 2, 0)
                )).build();
        // Read Entity (with V1 query)
        QueryResultBase pageData = FetchCreatedEntity(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        final Entity build = Entity.Builder.instance()
                .withEID("e00000000.context1")
                .withETag(Stream.of("A").toJavaSet())
                .withEType($ont.eType$("Entity"))
                .withProperties(Arrays.asList(
                        new Property("lastUpdateUser", "raw", "Kobi"),
                        new Property("category", "raw", "person"),
                        new Property("logicalId", "raw", logicalId),
                        new Property("context", "raw", "context1"),
                        new Property("creationUser", "raw", "Shaul"),
                        new Property("lastUpdateTime", "raw", "2018-05-27 14:32:56.533"),
                        new Property("creationTime", "raw", "2018-05-26 10:02:30.133"),
                        new Property("refs", "raw", null),
                        new Property("authorization", "raw", Arrays.asList("source1.procedure1", "source2.procedure2"))
                )).build();

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(build).build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, false);

//        while (true) {
//            Thread.sleep(1000);
//        }
    }


    // TODO: This is a BUG!!!
    // Reference is returned with logicalId property which makes no sense since Reference doesn't have a logicalId property.
    @Test(expected = AssertionError.class)
    public void testInsertEntityWithReference() throws IOException, InterruptedException {
        // Clearance to Reference
        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");
        // Link to Reference
        ArrayNode refsNode = _mapper.createArrayNode();
        refsNode.add("ref" + String.format(manager.getSchema().getIdFormat("reference"), 1));
        // LogicalId
        String logicalId = "e" + String.format(manager.getSchema().getIdFormat("entity"), 1);

        // Create entity ObjectNode and insert knowledge entity directly to ES
        CreateKnowledgeEntity(_mapper, manager, client, "entity", logicalId, "context1", "person", "Kobi", "Shaul",
                "2018-05-27 14:32:56.533", "2018-05-26 10:02:30.133", 1, authNode, refsNode);

        // Create Reference
        KnowledgeAutomationFunctions.CreateKnowledgeReference(manager, client, 1);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Get ontology from fuse catalog
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName(NAME.name).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                        new Rel(2, $ont.rType$("hasEntityReference"), R, null, 3, 0),
                        new ETyped(3, "B", $ont.eType$("Reference"), 0, 0)
                )).build();
        // Read Entity (with V1 query)
        QueryResultBase pageData = FetchCreatedEntity(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());

        AssignmentsQueryResult expectedResult = AssignmentsQueryResult.Builder.instance()
                .withAssignment(Assignment.Builder.instance()
                        .withEntity(Entity.Builder.instance()
                                .withEID("e00000000.context1")
                                .withETag(Stream.of("A").toJavaSet())
                                .withEType($ont.eType$("Entity"))
                                .withProperties(Arrays.asList(
                                        new Property("lastUpdateUser", "raw", "Kobi"),
                                        new Property("category", "raw", "person"),
                                        new Property("logicalId", "raw", logicalId),
                                        new Property("context", "raw", "context1"),
                                        new Property("creationUser", "raw", "Shaul"),
                                        new Property("lastUpdateTime", "raw", "2018-05-27 14:32:56.533"),
                                        new Property("creationTime", "raw", "2018-05-26 10:02:30.133"),
                                        new Property("refs", "raw", Collections.singletonList(refsNode.get(0).asText())),
                                        new Property("authorization", "raw", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                )).build())
                        .withEntity(Entity.Builder.instance()
                                .withEID(refsNode.get(0).asText())
                                .withETag(Stream.of("B").toJavaSet())
                                .withEType($ont.eType$("Reference"))
                                .withProperties(Arrays.asList(
                                        new Property("title", "raw", "Title of - ref00000001"),
                                        new Property("url", "raw", "https://stackoverflow.com/questions"),
                                        new Property("value", "raw", "But I must explain to you how all this works"),
                                        new Property("system", "raw", "system1"),
                                        new Property("lastUpdateUser", "raw", "2018-05-27 14:32:56.533"),
                                        new Property("lastUpdateTime", "raw", "2018-05-27 14:32:56.533"),
                                        new Property("creationUser", "raw", "2018-05-27 14:32:56.533"),
                                        new Property("creationTime", "raw", "2018-05-27 14:32:56.533"),
                                        new Property("authorization", "raw", Arrays.asList("source1.procedure1", "source2.procedure2"))
                                )).build())
                        .withRelationship(Relationship.Builder.instance()
                            .withEID1("e00000000.context1")
                            .withEID2(refsNode.get(0).asText())
                            .withETag1("A")
                            .withETag2("B")
                            .withRType($ont.rType$("hasEntityReference")).build())
                        .build()).build();

        // Check if expected and actual are equal
        QueryResultAssert.assertEquals(expectedResult, (AssignmentsQueryResult) pageData, true);

//        while (true) {
//            Thread.sleep(1000);
//        }
    }

//    @Test
    public void testInsertEntityWithFile() throws IOException, InterruptedException {
        // Clearance to Reference
        ArrayNode authNode = _mapper.createArrayNode();
        authNode.add("source1.procedure1");
        authNode.add("source2.procedure2");
        // Link to Reference
        ArrayNode filesNode = _mapper.createArrayNode();
        final String fileId = "file" + String.format(manager.getSchema().getIdFormat("file"), 0);
        // LogicalId
        String logicalId = "e" + String.format(manager.getSchema().getIdFormat("entity"), 0);

        // Create entity ObjectNode and insert knowledge entity directly to ES
        final String entityId = CreateKnowledgeEntity(_mapper, manager, client, "entity", logicalId, "context1", "person", "Kobi", "Shaul",
                "2018-05-27 14:32:56.533", "2018-05-26 10:02:30.133", 1, authNode, _mapper.createArrayNode());

        // Create Reference
        KnowledgeAutomationFunctions.CreateKnowledgeFile(client, fileId,logicalId,entityId,3);

        filesNode.add(fileId);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Get ontology from fuse catalog
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName(NAME.name).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                        new Rel(2, $ont.rType$("hasEntityFile"), R, null, 3, 0),
                        new ETyped(3, "B", $ont.eType$("File"), 0, 0)
                )).build();
        // Read Entity (with V1 query)
        QueryResultBase pageData = FetchCreatedEntity(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(1,assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEntityFile",assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2,assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity",assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("e00000000.context1",assignments.get(0).getEntities().get(0).geteID());
        Assert.assertEquals("File",assignments.get(0).getEntities().get(1).geteType());
        Assert.assertEquals("file00000000",assignments.get(0).getEntities().get(1).geteID());

    }

    @Test
    public void testInsertEntityWithFileWithBuilder() throws IOException, InterruptedException {
        KnowledgeWriterContext ctx = KnowledgeWriterContext.init(client,manager.getSchema());
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        Assert.assertEquals(1,commit(ctx, e1));

        // Create Reference
        KnowledgeAutomationFunctions.CreateKnowledgeFile(client, fileId,logicalId,entityId,3);

        filesNode.add(fileId);

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        // Get ontology from fuse catalog
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));
        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName(NAME.name).withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                        new Rel(2, $ont.rType$("hasEntityFile"), R, null, 3, 0),
                        new ETyped(3, "B", $ont.eType$("File"), 0, 0)
                )).build();
        // Read Entity (with V1 query)
        QueryResultBase pageData = FetchCreatedEntity(fuseClient, fuseResourceInfo, query);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        final List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

        Assert.assertEquals(1, assignments.size());
        Assert.assertEquals(1,assignments.get(0).getRelationships().size());
        Assert.assertEquals("hasEntityFile",assignments.get(0).getRelationships().get(0).getrType());

        Assert.assertEquals(2,assignments.get(0).getEntities().size());
        Assert.assertEquals("Entity",assignments.get(0).getEntities().get(0).geteType());
        Assert.assertEquals("e00000000.context1",assignments.get(0).getEntities().get(0).geteID());
        Assert.assertEquals("File",assignments.get(0).getEntities().get(1).geteType());
        Assert.assertEquals("file00000000",assignments.get(0).getEntities().get(1).geteID());

    }

}
