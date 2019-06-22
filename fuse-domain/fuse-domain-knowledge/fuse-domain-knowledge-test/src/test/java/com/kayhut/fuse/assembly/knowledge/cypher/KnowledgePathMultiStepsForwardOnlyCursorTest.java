package com.kayhut.fuse.assembly.knowledge.cypher;

import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.cursor.CreateForwardOnlyPathTraversalCursorRequest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;

import static com.kayhut.fuse.assembly.knowledge.Setup.fuseClient;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.nextPage;
import static com.kayhut.fuse.client.FuseClient.countGraphElements;

public class KnowledgePathMultiStepsForwardOnlyCursorTest {

    //number of elements on les miserables graph

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(true);
        //load data
        loadData();
    }

    private static void loadData() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("./data/logical/multi_steps.json");
        QueryResourceInfo info = fuseClient.loadData(KNOWLEDGE, resource);
        Assert.assertNotNull(info);
    }


    @Test
    public void testFetchEntityWithRelation4StepsLogicalResultPathSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                        " (e1:Entity)-[rv1:hasEvalue]->(ev1:Evalue {stringValue: 'Myriel'}), " +
                        " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                        " (e3:Entity)-[r3:relatedEntity]->(e4:Entity), " +
                        " (e4:Entity)-[r4:relatedEntity]->(e5:Entity) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(22, totalGraphSize);
    }
    @Test
    public void testFetchEntityWithRelation5StepsLogicalResultPathSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                        " (e1:Entity)-[rv1:hasEvalue]->(ev1:Evalue {stringValue: 'Myriel'}), " +
                        " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                        " (e3:Entity)-[r3:relatedEntity]->(e4:Entity), " +
                        " (e4:Entity)-[r4:relatedEntity]->(e5:Entity), " +
                        " (e5:Entity)-[r5:relatedEntity]->(e6:Entity) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(13, totalGraphSize);
    }
    @Test
    public void testFetchEntityWithRelation6StepsLogicalResultPathSpecificName() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query =
                "Match (e1:Entity)-[r1:relatedEntity]->(e2:Entity), " +
                        " (e1:Entity)-[rv1:hasEvalue]->(ev1:Evalue {stringValue: 'Myriel'}), " +
                        " (e2:Entity)-[r2:relatedEntity]->(e3:Entity), " +
                        " (e3:Entity)-[r3:relatedEntity]->(e4:Entity), " +
                        " (e4:Entity)-[r4:relatedEntity]->(e5:Entity), " +
                        " (e5:Entity)-[r5:relatedEntity]->(e6:Entity), " +
                        " (e6:Entity)-[r6:relatedEntity]->(e7:Entity) " +
                        " Return *";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(),
                new CreateForwardOnlyPathTraversalCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        long totalGraphSize = 0;
        while (countGraphElements(pageData) > totalGraphSize  ) {
            // Check Entity Response
            totalGraphSize = countGraphElements(pageData);
            pageData = nextPage(fuseClient, cursorResourceInfo, 1000);
        }
        //compare Entity created (*2 for both sides + relation entity itself) + relation (*2 in + out)
        Assert.assertEquals(0, totalGraphSize);
    }



}