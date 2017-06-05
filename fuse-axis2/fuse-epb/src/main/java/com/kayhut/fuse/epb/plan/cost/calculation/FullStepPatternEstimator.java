package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.DetailedCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.properties.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by moti on 29/05/2017.
 *
 */
public class FullStepPatternEstimator implements PatternCostEstimator {
    private CostEstimationConfig config;

    public FullStepPatternEstimator(CostEstimationConfig config) {
        this.config = config;
    }

    @Override
    public StepEstimator.StepEstimatorResult estimate(StatisticsProvider statisticsProvider, Map<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> patternParts, Optional<PlanWithCost<Plan, PlanDetailedCost>> previousCost) {
        return calculateFullStep(config, statisticsProvider, previousCost.get(), Step.buildFullStep(patternParts));
    }

    /**
     * ********************************************************
     * Calculate estimates for a full step.
     * Algorithm description:
     * Step = E1 ------- Rel + filter(+ E2 pushdown props) ------> E2 + filter( without pushdown props)
     * <p>
     * N1 = Prior estimate for E1 count
     * <p>
     * calculations:
     * R1 (relation estimate based on E1 count and global selectivity) = N1 * GS
     * R2 (relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
     * R (rel estimate) = min(R1, R2)
     * <p>
     * Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
     * alpha - GS ratio, > 1
     * N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
     * N2-2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
     * <p>
     * N2 = min(N2-1, N2-2)
     * <p>
     * lambda = (R/R1)*(N2/N2-1)
     * N1' = lambda*N1 (back propagate count estimate)
     * ********************************************************
     * @param config
     * @param statisticsProvider
     * @param previousCost
     * @param step
     * @return
     */
    public static StepEstimator.StepEstimatorResult calculateFullStep(CostEstimationConfig config, StatisticsProvider statisticsProvider, PlanWithCost<Plan, PlanDetailedCost> previousCost,Step step) {
        EntityOp start = step.start()._1;
        EntityFilterOp startFilter = step.start()._2;
        EntityOp end = step.end()._1;
        EntityFilterOp endFilter = step.end()._2;
        RelationOp rel = step.rel()._1;
        RelationFilterOp relationFilter = step.rel()._2;

        PlanDetailedCost cost = previousCost.getCost();
        Cost entityOneCost = cost.getOpCost(start).get();

        //edge estimate =>
        Direction direction = Direction.of(rel.getAsgEBase().geteBase().getDir());
        //(relation estimate based on E1 count and global selectivity) = N1 * GS
        long selectivity = statisticsProvider.getGlobalSelectivity(rel.getAsgEBase().geteBase(),
                relationFilter.getAsgEBase().geteBase(),
                start.getAsgEBase().geteBase(), direction);
        double N1 = cost.getPlanOpCost(start).get().peek();
        double R1 = N1 * selectivity;
        //(relation filter estimate) = statistical_estimate(Rel + filter + E1 pushdown)
        double R2 = statisticsProvider.getEdgeFilterStatistics(relationFilter.getRel().geteBase(), relationFilter.getAsgEBase().geteBase()).getTotal();
        //(rel estimate) = min(R1, R2)
        double R = Math.min(R1, R2);

        EPropGroup clone = endFilter.getAsgEBase().geteBase().clone();
        List<RelProp> pushdownProps = new LinkedList<>();
        List<RelProp> collect = relationFilter.getAsgEBase().geteBase().getProps().stream().filter(f -> (f instanceof PushdownRelProp) &&
                (!f.getpType().equals(Integer.toString(OntologyFinalizer.ID_FIELD_P_TYPE)) &&
                        (!f.getpType().equals(Integer.toString(OntologyFinalizer.TYPE_FIELD_P_TYPE)))))
                .collect(Collectors.toList());
        collect.forEach(p -> {
            pushdownProps.add(p);
            clone.getProps().add(EProp.of(p.getpType(), p.geteNum(), p.getCon()));
        });

        // Z (E2 node pushdown props only estimate) = statistical_estimate(E1 pushdown filter only)
        double Z = statisticsProvider.getRedundantNodeStatistics(end.getAsgEBase().geteBase(), RelPropGroup.of(pushdownProps)).getTotal();

        // N2-1 (relation based estimate for E2) = min(R*alpha/GS, Z)
        double N2_1 = Math.min(R * config.getAlpha(), Z);
        //N2_2 (E2 complete estimate) = statistical_estimate(E1 + filter (with pushdown))
        double N2_2 = statisticsProvider.getNodeFilterStatistics(end.getAsgEBase().geteBase(), clone).getTotal();

        //* N2 = min(N2-1, N2-2)
        double N2 = Math.min(N2_1, N2_2);

        //calculate back propagation weight
        double lambdaEdge = R / R2;
        double lambdaNode = N2 / N2_2;
        // lambda = (R/R1)*(N2/N2-1)
        double lambda = lambdaEdge * lambdaNode;

        //cost if zero since the real cost is residing on the adjacent filter (rel filter)
        Cost relCost = new DetailedCost(R * config.getDelta(), lambdaNode, lambdaEdge, R, N2);

        PlanOpWithCost<Cost> entityOneOpCost = new PlanOpWithCost<>(entityOneCost, N1, start, startFilter);
        entityOneOpCost.push(N1*lambda);
        PlanOpWithCost<Cost> relOpCost = new PlanOpWithCost<>(relCost, R, rel, relationFilter);
        PlanOpWithCost<Cost> entityTwoOpCost = new PlanOpWithCost<>(new DetailedCost(N2, lambdaNode, lambdaEdge, R, N2), N2, end, endFilter);

        return StepEstimator.StepEstimatorResult.of(lambda, entityOneOpCost, relOpCost, entityTwoOpCost);
    }

}