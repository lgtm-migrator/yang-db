package com.kayhut.fuse.unipop.schemaProviders;

/**
 * Created by moti on 5/9/2017.
 */
public interface GraphRedundantPropertySchema extends GraphElementPropertySchema{
    String getPropertyRedundantName();

    class Impl extends GraphElementPropertySchema.Impl implements GraphRedundantPropertySchema {
        //region Constructors
        public Impl(String name, String redundantName, String type) {
            super(name, type);
            this.propertyRedundantName = redundantName;
        }

        public Impl(String name, String redundantName, String type, Iterable<IndexingSchema> indexingSchemes) {
            super(name, type, indexingSchemes);
            this.propertyRedundantName = redundantName;
        }
        //endregion

        //region GraphElementPropertySchema Implementation
        @Override
        public String getPropertyRedundantName() {
            return this.propertyRedundantName;
        }
        //endregion

        //region Fields
        private String propertyRedundantName;
        //endregion
    }
}
