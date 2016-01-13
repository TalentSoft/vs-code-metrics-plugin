package org.jenkinsci.plugins.vs_code_metrics;

import hudson.model.Run;

import org.jenkinsci.plugins.vs_code_metrics.bean.CodeMetrics;
import org.jenkinsci.plugins.vs_code_metrics.bean.Module;

public final class CodeMetricsReport extends AbstractReport {

    /**
     *
     * @param run
     * @param result
     */
    public CodeMetricsReport(Run<?, ?> run, CodeMetrics result) {
        super(run, Messages.CodeMetricsReport_DisplayName(), result);
    }

    @Override
    public Object getReport(String token) {
        if ((getChildren() != null) && getChildren().containsKey(token))
            return new ModuleReport(getBuild(), (Module)getChildren().get(token));
        else
            return this;
    }
}
