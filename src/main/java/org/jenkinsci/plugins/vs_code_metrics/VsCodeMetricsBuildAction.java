package org.jenkinsci.plugins.vs_code_metrics;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.jenkinsci.plugins.vs_code_metrics.bean.*;
import org.jenkinsci.plugins.vs_code_metrics.util.CodeMetricsUtil;
import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
import org.kohsuke.stapler.StaplerProxy;

import hudson.model.Action;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Run;

/**
 * @author Yasuyuki Saito
 */
public class VsCodeMetricsBuildAction implements Action, StaplerProxy, HealthReportingAction {

    private final Run<?,?> run;
    private final VsCodeMetricsThresholds thresholds;
    private int maintainabilityIndex;
    private int cyclomaticComplexity;
    private boolean metricsValue;
    private transient WeakReference<CodeMetrics> resultRef = null;

    public VsCodeMetricsBuildAction(Run<?, ?> run, VsCodeMetricsThresholds thresholds) {
        this.run      = run;
        this.thresholds = thresholds;
        setMetricsValue();
    }

    public String getIconFileName() {
        return Constants.ACTION_ICON;
    }

    public String getDisplayName() {
        return Messages.VsCodeMetricsBuildAction_DisplayName();
    }

    public String getUrlName() {
        return Constants.ACTION_URL;
    }

    public int getMaintainabilityIndex() {
        return maintainabilityIndex;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public boolean isMetricsValue() {
        return metricsValue;
    }

    public Object getTarget() {
        return getReport();
    }

    public Run<?,?> getBuild() {
        return run;
    }

    private CodeMetricsReport getReport() {
        CodeMetrics result = getCodeMetrics();
        return new CodeMetricsReport(run, result);
    }

    public HealthReport getBuildHealth() {
        int maintainabilityIndex = 0;
        if (this.metricsValue)
            maintainabilityIndex = this.maintainabilityIndex;
        else {
            CodeMetrics result = getCodeMetrics();
            if (result == null) return null;
            maintainabilityIndex = result.getMaintainabilityIndex();
        }
        int score = getHealthScore(maintainabilityIndex, thresholds.getMinMaintainabilityIndex(), thresholds.getMaxMaintainabilityIndex());
        return new HealthReport(score, Messages._HealthReport_Description(maintainabilityIndex));
    }

    private int getHealthScore(int value, int minValue, int maxValue) {
        if (value >= maxValue) return 100;
        if (value <  minValue) return 0;
        return 50;
    }

    public synchronized CodeMetrics getCodeMetrics() {
        CodeMetrics result = null;
        if (resultRef != null) {
            result = resultRef.get();
            if (result != null) return result;
        }

        try {
            result = CodeMetricsUtil.getCodeMetrics(run);
            resultRef = new WeakReference<CodeMetrics>(result);
            return result;
        } catch (InterruptedException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private void setMetricsValue() {
        CodeMetrics result = getCodeMetrics();
        if (result == null) return;
        this.maintainabilityIndex = result.getMaintainabilityIndex();
        this.cyclomaticComplexity = result.getCyclomaticComplexity();
        this.metricsValue = true;
    }
}
