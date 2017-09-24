package com.kayhut.fuse.unipop.schemaProviders.indexPartitions;

import javaslang.collection.Stream;

import java.util.Optional;

public interface IndexPartitions {
    Optional<String> partitionField();
    Iterable<Partition> partitions();

    interface Partition {
        Iterable<String> indices();

        interface Range<T extends Comparable<T>> extends Partition {
            T from();

            T to();

            boolean isWithin(T value);

            class Impl<T extends Comparable<T>> implements Range<T> {
                //region Constructors
                public Impl(T from, T to, String... indices) {
                    this(from, to, Stream.of(indices));
                }

                public Impl(T from, T to, Iterable<String> indices) {
                    this.from = from;
                    this.to = to;
                    this.indices = Stream.ofAll(indices).toJavaSet();
                }
                //endregion

                //region Range Implementation
                @Override
                public Iterable<String> indices() {
                    return this.indices;
                }

                @Override
                public T from() {
                    return this.from;
                }

                @Override
                public T to() {
                    return this.to;
                }

                @Override
                public boolean isWithin(T value) {
                    return value.compareTo(this.from) >= 0 && value.compareTo(this.to) < 0;
                }
                //endregion

                //region Fields
                private Iterable<String> indices;
                private T from;
                private T to;
                //endregion
            }
        }
    }

    class Impl implements IndexPartitions {
        //region Constructors
        public Impl(Partition...partitions) {
            this(Stream.of(partitions));
        }

        public Impl(Iterable<Partition> partitions) {
            this.partitions = Stream.ofAll(partitions).toJavaList();
            this.partitionField = Optional.empty();
        }

        public Impl(String partitionField, Partition...partitions) {
            this(partitionField, Stream.of(partitions));
        }

        public Impl(String partitionField, Iterable<Partition> partitions) {
            this.partitionField = Optional.of(partitionField);
            this.partitions = Stream.ofAll(partitions).toJavaList();
        }
        //endregion

        //region IndexPartitions Implementation
        @Override
        public Optional<String> partitionField() {
            return this.partitionField;
        }

        @Override
        public Iterable<Partition> partitions() {
            return this.partitions;
        }
        //endregion

        //region Fields
        private Optional<String> partitionField;
        private Iterable<Partition> partitions;
        //endregion
    }
}
