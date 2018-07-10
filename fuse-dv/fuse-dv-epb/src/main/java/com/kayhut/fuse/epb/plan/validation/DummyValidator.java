package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.model.validation.ValidationResult;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public ValidationResult isPlanValid(P plan, Q query) {
        return ValidationResult.OK;
    }
}