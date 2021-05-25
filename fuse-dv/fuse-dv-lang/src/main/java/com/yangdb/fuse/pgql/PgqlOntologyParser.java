package com.yangdb.fuse.pgql;

/*-
 * #%L
 * fuse-dv-lang
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.EPair;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.ddl.propertygraph.CreatePropertyGraph;
import oracle.pgql.lang.ddl.propertygraph.EdgeTable;
import oracle.pgql.lang.ddl.propertygraph.ElementTable;
import oracle.pgql.lang.ddl.propertygraph.VertexTable;
import oracle.pgql.lang.ir.PgqlStatement;
import oracle.pgql.lang.ir.QueryExpression;
import oracle.pgql.lang.ir.SchemaQualifiedName;
import oracle.pgql.lang.ir.StatementType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.ontology.Ontology.OntologyPrimitiveType.*;
import static java.lang.String.format;

/**
 * translates the PGQL DDL statement into the logical (high level) ontology schema
 */
public class PgqlOntologyParser implements OntologyTransformerIfc<String, Ontology> {

    public static final String DEFAULT_ID_PK = "id";

    @Override
    public Ontology transform(String ontologyName, String query) {
        Ontology.OntologyBuilder builder = Ontology.OntologyBuilder.anOntology().withOnt(ontologyName);
        try (Pgql pgql = new Pgql()) {
            //parse DDL graph query
            PgqlStatement pgqlStatement = pgql.parse(query).getPgqlStatement();
            StatementType type = pgqlStatement.getStatementType();
            if (!type.equals(StatementType.CREATE_PROPERTY_GRAPH)) {
                throw new FuseError.FuseErrorException("Pgql Ontology DDL query was not found ",
                        new FuseError("Pgql Ontology Parser Error", "DDL query was not found " + query));
            }

            CreatePropertyGraph createGraphDDL = (CreatePropertyGraph) pgqlStatement;
            SchemaQualifiedName graphName = createGraphDDL.getGraphName();
            List<VertexTable> vertexTables = createGraphDDL.getVertexTables();
            List<EdgeTable> edgeTables = createGraphDDL.getEdgeTables();
            //transform tables into vertices
            vertexTables.forEach(t -> transform(builder, t));
            //transform tables into vertices
            edgeTables.forEach(t -> transform(builder, t));
            //consolidate edges within same label relating to different pairs
            consolidateEdges(builder);
            return builder.build();
        } catch (PgqlException e) {
            throw new FuseError.FuseErrorException("No valid Pgql Ontology DDL query ",
                    new FuseError("Pgql Ontology Parser Error", "No valid Pgql Ontology command " + query));
        }
    }

    /**
     * consolidate different relations that refer to the same db table but relate to different pair of vertices
     * @param builder
     */
    private void consolidateEdges(Ontology.OntologyBuilder builder) {
        //collect the similar shallow relationships
        Map<Integer, Set<RelationshipType>> map = builder.getRelationships().stream().collect(
                Collectors.groupingBy(RelationshipType::hashCodeWithoutPairs, Collectors.toSet()));
        //gather all epairs of all similar relationships into the first one
        builder.withRelationshipTypes(map.values().stream()
                .map(this::combine).collect(Collectors.toList()));
    }

    /**
     * collect all epairs into the first relationship
     * @param relations
     * @return
     */
    private RelationshipType combine( Set<RelationshipType> relations) {
        return relations.stream().limit(1).findAny().get().withEPairs(
                relations.stream().flatMap(r->r.getePairs().stream()).collect(Collectors.toList())
        );
    }

    /**
     * adds vertex table to ontology and its related properties
     *
     * @param builder
     * @param table
     */
    private void transform(Ontology.OntologyBuilder builder, VertexTable table) {
        table.getLabels().forEach(label -> {
            builder.addEntityType(EntityType.Builder.get()
                    .withEType(label.getName())
                    .withDBrName(table.getTableName().getName())
                    .withName(label.getName())
                    .withIdField(getField(table))
                    .withProperties(label.getProperties().stream()
                            .map(p->format("%s_%s", label.getName(), p.getPropertyName())).collect(Collectors.toList()))
                    .build());
            //add label related properties to ontology
            builder.addProperties(label.getProperties().stream()
                    .map(p -> new com.yangdb.fuse.model.ontology.Property(
                            format("%s_%s", label.getName(), p.getPropertyName()),
                            format("%s_%s", label.getName(), p.getPropertyName()),
                            getType(p.getValueExpression().getExpType())))
                    .collect(Collectors.toList()));
        });
    }

    /**
     * translate PK fields into list of strings  - if non exist we use default "id" field as pk
     *
     * @param table
     * @return
     */
    private String[] getField(ElementTable table) {
        if (table.getKey() == null)
            return new String[]{DEFAULT_ID_PK};
        //collect pk columns
        return table.getKey().getColumnNames().toArray(new String[0]);
    }

    /**
     * translate PGQL type to our own primitive ontology type
     *
     * @param p
     * @return
     */
    private String getType(QueryExpression.ExpressionType p) {
        switch (p) {
            case INTEGER:
                return INT.name();
            case DECIMAL:
                return FLOAT.name();
            case VARREF:
            case STRING:
                return STRING.name();
            case BOOLEAN:
                return INT.name();
            case DATE:
                return DATE.name();
            case TIME:
                return DATE.name();
            case TIMESTAMP:
                return LONG.name();
            case TIME_WITH_TIMEZONE:
                return STRING.name();
            case TIMESTAMP_WITH_TIMEZONE:
                return STRING.name();
            default:
                return STRING.name();
        }
    }

    /**
     * adds edge table to ontology and its related properties
     *
     * @param builder
     * @param table
     */
    private void transform(Ontology.OntologyBuilder builder, EdgeTable table) {
        table.getLabels().forEach(edge -> {
            builder.addRelationshipType(RelationshipType.Builder.get()
                    .withDBrName(table.getTableName().getName())
                    .withRType(table.getTableAlias())//type would be associated with the table alias
                    .withName(edge.getName())//name would be associated with the label
                    .withIdField(getField(table))
                    .withProperties(edge.getProperties().stream()
                            .map(p->format("%s_%s", edge.getName(), p.getPropertyName())).collect(Collectors.toList()))
                    .withEPair(buildEpair(builder, table))
                    .build());


            //add label related properties to ontology
            builder.addProperties(edge.getProperties().stream()
                    .map(p -> new com.yangdb.fuse.model.ontology.Property(
                            format("%s_%s", edge.getName(), p.getPropertyName()),
                            format("%s_%s", edge.getName(), p.getPropertyName()),
                            getType(p.getValueExpression().getExpType())))
                    .collect(Collectors.toList()));
        });
    }

    private EPair buildEpair(Ontology.OntologyBuilder builder, EdgeTable table) {
        return EPair.EPairBuilder.anEPair()
                //create EPair for both sides of the relation
                .withETypeAIdField(table.getEdgeSourceKey() != null ? String.join(",", table.getEdgeSourceKey().getColumnNames()) : null)
                .withETypeBIdField(table.getEdgeDestinationKey() != null ? String.join(",", table.getEdgeDestinationKey().getColumnNames()) : null)
                //set the label names for the source / dest vertices
                .with(builder.getEntityTypeByTableName(table.getSourceVertexTable().getTableName().getName())
                                .orElseThrow(() -> new FuseError.FuseErrorException(
                                        new FuseError("Ontology creation transformation error ",
                                                "No Element in Ontology named " + table.getSourceVertexTable().getTableName().getName())))
                                .geteType(),
                        builder.getEntityTypeByTableName(table.getDestinationVertexTable().getTableName().getName())
                                .orElseThrow(() -> new FuseError.FuseErrorException(
                                        new FuseError("Ontology creation transformation error ",
                                                "No Element in Ontology named " + table.getDestinationVertexTable().getTableName().getName())))
                                .geteType())
                .build();

    }

    @Override
    public String translate(Ontology source) {
        //Todo
        throw new NotImplementedException();
    }

}