package com.kayhut.fuse.model.results;

import javaslang.collection.Stream;
import org.junit.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Roman on 15/05/2017.
 */
public class QueryResultAssert {
    //region Public Methods
    public static void assertEquals(QueryResult expected, QueryResult actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        assertIfBothNull(expected.getAssignments(), actual.getAssignments());
        assertIfBothNotNull(expected.getAssignments(), actual.getAssignments());

        Assert.assertTrue(expected.getAssignments().size() == actual.getAssignments().size());

        List<Assignment> expectedAssignments = Stream.ofAll(expected.getAssignments())
                .sortBy(Assignment::toString).toJavaList();
        List<Assignment> actualAssignments = Stream.ofAll(actual.getAssignments())
                .sortBy(Assignment::toString).toJavaList();

        for(int i = 0 ; i < expectedAssignments.size() ; i++) {
            assertEquals(expectedAssignments.get(i), actualAssignments.get(i));
        }
    }

    public static void assertEquals(Assignment expected, Assignment actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        assertIfBothNull(expected.getEntities(), actual.getEntities());
        assertIfBothNotNull(expected.getEntities(), actual.getEntities());

        List<Entity> expectedEntities = Stream.ofAll(expected.getEntities())
                .sortBy(Entity::geteID).toJavaList();
        List<Entity> actualEntities = Stream.ofAll(actual.getEntities())
                .sortBy(Entity::geteID).toJavaList();

        for(int i = 0 ; i < expectedEntities.size() ; i++) {
            assertEquals(expectedEntities.get(i), actualEntities.get(i));
        }

        assertIfBothNull(expected.getRelationships(), actual.getRelationships());
        assertIfBothNotNull(expected.getRelationships(), actual.getRelationships());

        List<Relationship> expectedRelationships = Stream.ofAll(expected.getRelationships())
                .sortBy(Relationship::getrID).toJavaList();
        List<Relationship> actualRelationships = Stream.ofAll(actual.getRelationships())
                .sortBy(Relationship::getrID).toJavaList();

        for(int i = 0 ; i < expectedRelationships.size() ; i++) {
            assertEquals(expectedRelationships.get(i), actualRelationships.get(i));
        }
    }

    public static void assertEquals(Entity expected, Entity actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertEquals(expected.geteID(), actual.geteID());
        Assert.assertEquals(Stream.ofAll(expected.geteTag()).sorted().toJavaList().toString(),
                Stream.ofAll(actual.geteTag()).sorted().toJavaList().toString());
        Assert.assertTrue(expected.geteType() == actual.geteType());

        assertIfBothNull(expected.getProperties(), actual.getProperties());
        assertIfBothNotNull(expected.getProperties(), actual.getProperties());

        List<Property> expectedProperties = Stream.ofAll(expected.getProperties())
                .sortBy(Property::getpType).toJavaList();
        List<Property> actualProperties = Stream.ofAll(actual.getProperties())
                .sortBy(Property::getpType).toJavaList();

        for(int i = 0 ; i < expectedProperties.size() ; i++) {
            assertEquals(expectedProperties.get(i), actualProperties.get(i));
        }

        assertIfBothNull(expected.getAttachedProperties(), actual.getAttachedProperties());
        assertIfBothNotNull(expected.getAttachedProperties(), actual.getAttachedProperties());

        List<AttachedProperty> expectedAttachedProperties = Stream.ofAll(expected.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();
        List<AttachedProperty> actualAttachedProperties = Stream.ofAll(actual.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();

        for(int i = 0 ; i < expectedAttachedProperties.size() ; i++) {
            assertEquals(expectedAttachedProperties.get(i), actualAttachedProperties.get(i));
        }
    }

    public static void assertEquals(Property expected, Property actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertTrue(expected.getpType() == actual.getpType());
        Assert.assertEquals(expected.getValue(), actual.getValue());
        Assert.assertEquals(expected.getAgg(), actual.getAgg());
    }

    public static void assertEquals(AttachedProperty expected, AttachedProperty actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertEquals(expected.getpName(), actual.getpName());
        Assert.assertEquals(expected.getTag(), actual.getTag());
        Assert.assertEquals(expected.getValue(), actual.getValue());
    }

    public static void assertEquals(Relationship expected, Relationship actual) {
        assertIfBothNull(expected, actual);
        assertIfBothNotNull(expected, actual);

        Assert.assertTrue(expected.getrType() == actual.getrType());
        Assert.assertEquals(expected.getrID(), actual.getrID());
        Assert.assertEquals(expected.geteID1(), actual.geteID1());
        Assert.assertEquals(expected.geteID2(), actual.geteID2());
        Assert.assertEquals(expected.geteTag1(), actual.geteTag1());
        Assert.assertEquals(expected.geteTag2(), actual.geteTag2());

        assertIfBothNull(expected.getProperties(), actual.getProperties());
        assertIfBothNotNull(expected.getProperties(), actual.getProperties());

        List<Property> expectedProperties = Stream.ofAll(expected.getProperties())
                .sortBy(Property::getpType).toJavaList();
        List<Property> actualProperties = Stream.ofAll(actual.getProperties())
                .sortBy(Property::getpType).toJavaList();

        for(int i = 0 ; i < expectedProperties.size() ; i++) {
            assertEquals(expectedProperties.get(i), actualProperties.get(i));
        }

        assertIfBothNull(expected.getAttachedProperties(), actual.getAttachedProperties());
        assertIfBothNotNull(expected.getAttachedProperties(), actual.getAttachedProperties());

        List<AttachedProperty> expectedAttachedProperties = Stream.ofAll(expected.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();
        List<AttachedProperty> actualAttachedProperties = Stream.ofAll(actual.getAttachedProperties())
                .sortBy(AttachedProperty::getpName).toJavaList();

        for(int i = 0 ; i < expectedAttachedProperties.size() ; i++) {
            assertEquals(expectedAttachedProperties.get(i), actualAttachedProperties.get(i));
        }
    }
    //endregion

    //region Private Methods
    private static void assertIfBothNull(Object expected, Object actual) {
        if (expected == null) {
            Assert.assertTrue(actual == null);
        }
    }

    private static void assertIfBothNotNull(Object expected, Object actual) {
        Assert.assertTrue(expected != null && actual != null);
    }
    //endregion
}
