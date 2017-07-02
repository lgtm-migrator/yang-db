package com.kayhut.fuse.unipop.controller.search.translation;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Collections;

/**
 * Created by Roman on 18/05/2017.
 */
public class CompositeQueryTranslator implements PredicateQueryTranslator {
    //region Constructors
    public CompositeQueryTranslator(PredicateQueryTranslator...translators) {
        this.translators = Stream.of(translators).toJavaList();
    }

    public CompositeQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        this.translators = Stream.ofAll(translators).toJavaList();
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        for(PredicateQueryTranslator translator : this.translators) {
            queryBuilder = translator.translate(queryBuilder, key, predicate);
        }

        return queryBuilder;
    }
    //endregion

    //region Fields
    protected Iterable<PredicateQueryTranslator> translators;
    //endregion
}
