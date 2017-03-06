package com.kayhut.fuse.services;

import com.kayhut.fuse.model.transport.CursorFetchRequest;
import com.kayhut.fuse.model.transport.QueryRequest;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.scanner.Scanner;


@SuppressWarnings({"unchecked", "rawtypes"})
public class FuseApp extends Jooby {

    {
        use(new Scanner());

        /** JSON: */
        use(new Jackson());

        /** CORS: */
        use("*", (req, rsp) -> {
            rsp.header("Access-Control-Allow-Origin", "*");
            rsp.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PATCH");
            rsp.header("Access-Control-Max-Age", "3600");
            rsp.header("Access-Control-Allow-Headers", "x-requested-with", "origin", "content-type",
                    "accept");
            if (req.method().equalsIgnoreCase("options")) {
                rsp.end();
            }
        });

        /** Fuse API: */
        use("/fuse/health")
                /** check health */
                .get(() -> "Alive And Well...");

        /** Fuse API: */
        use("/fuse/catalog/ontology/:id")
                /** check health */
                .get(req -> Results.with(catalogCtrl().ontology(req.param("id").value()), Status.FOUND));

        use("/fuse/query/graph")
                /** submit a graph query */
                .post(req -> Results.with(queryCtrl().query(req.body(QueryRequest.class)), Status.CREATED));

        use("/fuse/search")
                /** submit a plan */
                .post(req -> Results.with(searchCtrl().search(req.body(QueryRequest.class)), Status.CREATED));

        use("/fuse/query/:id/plan")
                /** submit a plan */
                .post(req -> Results.with(queryCtrl().explain(req.param("getId").value()), Status.CREATED));

        use("/fuse/query/:queryId/cursor/:cursorId/fetch")
                /** submit a plan */
                .post(req -> Results.with(cursorCtrl().fetch(req.param("queryId").value(), req.param("cursorId").intValue(), req.body(CursorFetchRequest.class).getFetchSize()), Status.CREATED));

        use("/fuse/query/:queryId/cursor/:cursorId/result/:resultId")
                /** result by ID. */
                .get(req -> Results.with(resultsCtrl().get(req.param("queryId").value(), req.param("cursorId").intValue(), req.param("resultId").intValue()), Status.FOUND));
    }

    private QueryController queryCtrl() {
        return require(QueryController.class);
    }

    private CursorController cursorCtrl() {
        return require(CursorController.class);
    }

    private CatalogController catalogCtrl() {
        return require(CatalogController.class);
    }

    private SearchController searchCtrl() {
        return require(SearchController.class);
    }

    private ResultsController resultsCtrl() {
        return require(ResultsController.class);
    }

    public static void main(final String[] args) {
        run(FuseApp::new, args);
    }

}
