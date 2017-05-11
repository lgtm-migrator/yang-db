package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.CompositePlanOpBase;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.log.Trace;
import com.kayhut.fuse.model.log.TraceComposite;
import javaslang.Tuple2;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by Roman on 24/04/2017.
 */
public class ChainedPlanValidator implements PlanValidator<Plan, AsgQuery> ,Trace<String>{
    private TraceComposite<String> trace = TraceComposite.build(this.getClass().getSimpleName());

    @Override
    public void log(String event, Level level) {
        trace.log(event,level);
    }

    @Override
    public List<Tuple2<String, String>> getLogs(Level level) {
        return trace.getLogs(level);
    }

    @Override
    public String who() {
        return trace.who();
    }

    public interface PlanOpValidator extends Trace<String> {
        void reset();
        boolean isPlanOpValid(AsgQuery query, CompositePlanOpBase compositePlanOp, int opIndex);
    }

    //region Constructors
    public ChainedPlanValidator(PlanOpValidator planOpValidator) {
        this.planOpValidator = planOpValidator;
        trace.with(planOpValidator);
    }
    //endregion

    //region PlanValidator Implementation
    @Override
    public boolean isPlanValid(Plan plan, AsgQuery query) {
        this.planOpValidator.reset();

        int opIndex = 0;
        for (PlanOpBase planOp : plan.getOps()) {
            if (!planOpValidator.isPlanOpValid(query, plan, opIndex++)) {
                return false;
            }
        }

        return true;
    }
    //endregion

    //region Fields
    private PlanOpValidator planOpValidator;
    //endregion
}

