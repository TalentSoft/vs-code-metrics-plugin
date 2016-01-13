package org.jenkinsci.plugins.vs_code_metrics;

import hudson.model.Run;

import java.util.Calendar;

import org.jenkinsci.plugins.vs_code_metrics.bean.AbstractBean;

public final class CyclomaticComplexityGraph extends AbstractGraph {

    public CyclomaticComplexityGraph(Run<?, ?> run, String[] buildTokens, Calendar timestamp, int defaultW, int defaultH) {
        super(run, buildTokens, timestamp, defaultW, defaultH);
        valueKey = Messages.ChartLabel_CyclomaticComplexity();
    }

    @Override
    protected int getValue(AbstractBean<?> bean) {
        return bean.getCyclomaticComplexity();
    }

    @Override
    protected int getValue(VsCodeMetricsBuildAction action) {
        return action.getCyclomaticComplexity();
    }

}
