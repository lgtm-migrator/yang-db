package com.kayhut.test.data;

import com.jayway.jsonpath.JsonPath;
import com.kayhut.fuse.executor.ontology.GraphLayoutProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphLayoutProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import net.minidev.json.JSONArray;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.model.Utils.readJsonFile;

/**
 * Created by liorp on 6/4/2017.
 */
public class DragonsOntologyGraphLayoutProviderFactory implements GraphLayoutProviderFactory {

    public static final String STRING = "string";

    //region Constructors
    public DragonsOntologyGraphLayoutProviderFactory() throws IOException {
        this("DragonsGraphLayoutProviderFactory.json");
    }

    @Inject
    public DragonsOntologyGraphLayoutProviderFactory(String configFile) throws IOException {
        this.graphLayoutProviders = new HashMap<>();
        String conf = readJsonFile("schema/" + configFile);
        this.graphLayoutProviders.put("Dragons", (edgeType, property) -> {
            try {
                JSONArray array = JsonPath.read(conf, "$['entities'][?(@.type == '" + edgeType + "')]['redundant']");
                Optional<Object> first = array.stream().flatMap(v -> ((JSONArray) v).stream()).filter(p -> ((Map) p).get("name").equals(property.getName())).findFirst();
                if(!first.isPresent()) return Optional.empty();

                Map redundantProperty = (Map) first.get();
                return Optional.of(new GraphRedundantPropertySchema.Impl(
                        redundantProperty.get("name").toString(),
                        redundantProperty.get("redundant_name").toString(),
                        "string"));
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }
    //endregion

    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphLayoutProvider get(Ontology ontology) {
        return this.graphLayoutProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, GraphLayoutProvider> graphLayoutProviders;
    //endregion
}
