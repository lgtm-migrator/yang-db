package com.yangdb.fuse.assembly.knowledge.asg;

/*-
 * #%L
 * fuse-asg
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

import com.yangdb.fuse.asg.validation.*;

import java.util.Collections;

public class AsgKnowledgeValidatorStrategyRegistrar implements AsgValidatorStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgValidatorStrategy> register() {
        return Collections.singletonList(new CompositeValidatorStrategy(
                new AsgConstraintExpressionValidatorStrategy(),
                new AsgCycleValidatorStrategy(),
                new AsgCompositeQueryValidatorStrategy(),
                new AsgEntityDuplicateEnumValidatorStrategy(),
//                new AsgEntityDuplicateETagValidatorStrategy(),
                new AsgKnowledgeEntityPropertiesValidatorStrategy("Entity","Evalue"),
                new AsgOntologyEntityValidatorStrategy(),
                new AsgOntologyRelValidatorStrategy(),
                new AsgRelPropertiesValidatorStrategy(),
                new AsgStartEntityValidatorStrategy(),
                new AsgWhereByConstraintValidatorStrategy(),
                new AsgStepsValidatorStrategy()
        ));
    }
    //endregion
}
