package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.epb.plan.OntologyElementRawStatisticableProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchema;

/**
 * Created by moti on 4/12/2017.
 */
public class QueryItemStatisticsProvider implements StatisticsProvider<StatisticableQueryItemInfo> {
    private OntologyElementRawStatisticableProvider ontologyElementRawStatisticableProvider;
    private StatisticsProvider<RawGraphStatisticableItemInfo> rawGraphStatisticsProvider;

    @Override
    public CardinalityStatistics getCardinalityStatistics(StatisticableQueryItemInfo info) {
        return rawGraphStatisticsProvider.getCardinalityStatistics(createRawItemInfo(info));
    }

    private RawGraphStatisticableItemInfo createRawItemInfo(StatisticableQueryItemInfo info){
        if(info instanceof StatisticableOntologyElementInfo){
            StatisticableOntologyElementInfo statisticableOntologyElementInfo = (StatisticableOntologyElementInfo) info;
            return ontologyElementRawStatisticableProvider.getRawStatisticable(statisticableOntologyElementInfo.geteBase());
        }
        return null;
    }

    @Override
    public <T extends Comparable<T>> HistogramStatistics<T> getHistogramStatistics(StatisticableQueryItemInfo info) {
        return rawGraphStatisticsProvider.getHistogramStatistics(createRawItemInfo(info));
    }
}
