package com.kayhut.fuse.model.execution.plan.composite;

import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.CompositePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;

import static com.kayhut.fuse.model.Utils.*;

/**
 * Created by User on 22/02/2017.
 */
public class Plan extends CompositePlanOp implements IPlan {
    //region Constructors
    public Plan() {}

    public Plan(PlanOp... ops) {
        super(ops);
    }

    public Plan(Iterable<PlanOp> ops) {
        super(ops);
    }
    //endregion

    public static boolean contains(Plan plan, PlanOp op) {
        return plan.getOps().stream().anyMatch(p->p.equals(op));
    }

    public static boolean equals(Plan plan, Plan newPlan) {
        return IterablePlanOpDescriptor.getSimple().describe(newPlan.getOps())
                .compareTo(IterablePlanOpDescriptor.getSimple().describe(plan.getOps())) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return equals((Plan)o,this);
    }

    @Override
    public int hashCode() {
        return IterablePlanOpDescriptor.getSimple().describe(this.getOps()).hashCode();
    }

    public static Plan clone(Plan plan) {
        return new Plan(plan.getOps());
    }
}