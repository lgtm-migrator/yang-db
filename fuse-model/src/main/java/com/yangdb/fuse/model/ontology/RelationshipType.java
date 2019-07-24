package com.yangdb.fuse.model.ontology;

/*-
 * #%L
 * RelationshipType.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelationshipType {
    public RelationshipType() {
        properties = new ArrayList<>();
        ePairs = new ArrayList<>();
    }

    public RelationshipType(String name, String rType, boolean directional) {
        this();
        this.rType = rType;
        this.name = name;
        this.directional = directional;
    }

    //region Getters & Setters
    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectional() {
        return directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
    }

    @JsonProperty("DBrName")
    public String getDBrName() {
        return DBrName;
    }

    @JsonProperty("DBrName")
    public void setDBrName(String DBrName) {
        this.DBrName = DBrName;
    }

    public List<EPair> getePairs() {
        return ePairs;
    }

    public void setePairs(List<EPair> ePairs) {
        this.ePairs = ePairs;
    }

    public RelationshipType addPair(EPair pair) {
        this.getePairs().add(pair);
        return this;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public RelationshipType addProperty(String type) {
        this.properties.add(type);
        return this;
    }
    public RelationshipType withProperty(String ... properties) {
        this.properties.addAll(Arrays.asList(properties));
        return this;
    }

    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelationshipType that = (RelationshipType) o;

        if (rType != that.rType) return false;
        if (directional != that.directional) return false;
        if (!name.equals(that.name)) return false;
        if (DBrName != null ? !DBrName.equals(that.DBrName) : that.DBrName != null) return false;
        if (ePairs != null ? !ePairs.equals(that.ePairs) : that.ePairs != null) return false;
        return properties != null ? properties.equals(that.properties) : that.properties == null;
    }

    @Override
    public int hashCode() {
        int result = rType.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (directional ? 1 : 0);
        result = 31 * result + (DBrName != null ? DBrName.hashCode() : 0);
        result = 31 * result + (ePairs != null ? ePairs.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "RelationshipType [ePairs = "+ePairs+", rType = "+rType+", directional = "+directional+", name = "+name+", properties = "+properties+"]";
    }

    //region Fields
    private String rType;
    private String name;
    private boolean directional;
    private String DBrName;
    private List<EPair> ePairs;
    private List<String> properties;

    //endregion

    //region Builder
    public static final class Builder {
        private String rType;
        private String name;
        private boolean directional;
        private String DBrName;
        private List<EPair> ePairs;
        private List<String> properties;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withRType(String rType) {
            this.rType = rType;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDirectional(boolean directional) {
            this.directional = directional;
            return this;
        }

        public Builder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        public Builder withEPairs(List<EPair> ePairs) {
            this.ePairs = ePairs;
            return this;
        }

        public Builder withProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        public RelationshipType build() {
            RelationshipType relationshipType = new RelationshipType();
            relationshipType.setName(name);
            relationshipType.setDirectional(directional);
            relationshipType.setDBrName(DBrName);
            relationshipType.setProperties(properties);
            relationshipType.ePairs = this.ePairs;
            relationshipType.rType = this.rType;
            return relationshipType;
        }
    }
    //endregion

}