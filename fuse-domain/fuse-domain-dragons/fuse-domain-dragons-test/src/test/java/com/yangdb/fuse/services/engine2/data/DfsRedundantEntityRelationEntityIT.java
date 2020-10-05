package com.yangdb.fuse.services.engine2.data;

import com.yangdb.fuse.services.engine2.RedundantTestSuite;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.promise.Constraint;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;

import static com.yangdb.fuse.model.GlobalConstants.EdgeSchema.*;

/**
 * Created by Roman on 22/05/2017.
 */
public class DfsRedundantEntityRelationEntityIT extends EntityRelationEntityIT {
    @BeforeClass
    public static void setup() throws Exception {
        EntityRelationEntityIT.setup(RedundantTestSuite.elasticEmbeddedNode.getClient());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        EntityRelationEntityIT.cleanup(RedundantTestSuite.elasticEmbeddedNode.getClient());
    }

    @Override
    protected TraversalConstraint getExpectedEdgeTraversalConstraint(
            String relationType,
            Direction direction,
            String relProperty,
            P relPropertyPredicate,
            String entityBId,
            Iterable<String> entityBTypes) {

        List<Traversal> traversals = new ArrayList<>();
        traversals.add(__.has(T.label, relationType));
        traversals.add(__.has(GlobalConstants.HasKeys.DIRECTION, direction));

        if (relProperty != null) {
            traversals.add(__.has(relProperty, relPropertyPredicate));
        }

        if (entityBTypes != null && !Stream.ofAll(entityBTypes).isEmpty()) {
            traversals.add(__.has(DEST_TYPE, P.within(Stream.ofAll(entityBTypes).toJavaArray(String.class))));
        }

        if (entityBId != null) {
            traversals.add(__.has(DEST_ID, P.eq(entityBId)));
        }

        return Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class)));
    }
}
