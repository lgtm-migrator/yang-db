package com.kayhut.fuse.asg.translator.cypher.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 The Fuse Graph Database Project
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

import com.kayhut.fuse.asg.translator.cypher.strategies.expressions.EqualityExpression;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.util.InputPosition;
import scala.Option;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class NodePatternCypherTranslatorStrategy implements CypherElementTranslatorStrategy<PatternElement> {

    public NodePatternCypherTranslatorStrategy(EqualityExpression equalityExpression) {
        this.equalityExpression = equalityExpression;
    }

    @Override
    public void apply(PatternElement element, AsgQuery query, CypherStrategyContext context) {
        if (element instanceof NodePattern) {
            final Option<LogicalVariable> variable = element.variable();

            int current = context.getScope().geteNum() + 1;
            if (!context.getScope().getNext().isEmpty()) {
                current = context.getScope().getNext().get(0).geteNum();
            }

            String name = "Node_#" + current;

            if (!variable.isEmpty()) {
                //get scope and calculate next enum
                final LogicalVariable logicalVariable = variable.get();
                name = logicalVariable.name();
            }

            final Option<Expression> properties = ((NodePattern) element).properties();
            if(properties.nonEmpty()){
                final List<Literal> var = CypherUtils.literal(properties.get());
                if(var.size()>0) {
                    var.forEach(literal->equalityExpression.apply(Optional.empty(),
//                            new Equals(element.variable().get(),(Expression)literal,InputPosition.NONE()),

                            com.bpodgursky.jbool_expressions.Variable.of(CypherUtils.Wrapper.of((Expression)literal)),
                                query,
                                context)
                    );
                }
            }


            //build label and update query, mutate new current scope
            final Collection<LabelName> labels = asJavaCollectionConverter(((NodePattern) element).labels()).asJavaCollection();
            //labels
            final List<String> collect = labels.stream().map(l -> l.name()).collect(Collectors.toList());
            //create label
            AsgEBase<? extends EBase> node = new AsgEBase<>(new EUntyped(current, name, collect, Collections.emptyList(), current + 1, 0));
            //is single label - use EType label (specific typed label)
            if (labels.size() == 1) {
                node = new AsgEBase<>(new ETyped(current, name, labels.iterator().next().name(), current + 1, 0));
            }

            context.getScope().addNext(node);
            context.scope(node);
        }
    }

    private EqualityExpression equalityExpression;
}
