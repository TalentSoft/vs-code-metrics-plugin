package org.jenkinsci.plugins.vs_code_metrics;

import hudson.model.Run;

import java.util.Calendar;

import org.jenkinsci.plugins.vs_code_metrics.bean.AbstractBean;

public final class MaintainabilityIndexGraph extends AbstractGraph {

    public MaintainabilityIndexGraph(Run<?, ?> run, String[] buildTokens, Calendar timestamp, int defaultW, int defaultH) {
        super(run, buildTokens, timestamp, defaultW, defaultH);
        upperBound = 100;
        valueKey = Messages.ChartLabel_MaintainabilityIndex();
    }

    @Override
    protected int getValue(AbstractBean<?> bean) {
        return bean.getMaintainabilityIndex();
    }

    @Override
    protected int getValue(VsCodeMetricsBuildAction action) {
        return action.getMaintainabilityIndex();
    }

}
