package org.jenkinsci.plugins.vs_code_metrics;

import hudson.model.ModelObject;
import hudson.model.Run;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.jenkinsci.plugins.vs_code_metrics.bean.AbstractBean;
import org.jenkinsci.plugins.vs_code_metrics.bean.CodeMetrics;
import org.jenkinsci.plugins.vs_code_metrics.util.CodeMetricsUtil;
import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class AbstractReport implements Serializable, ModelObject {

    private static final long serialVersionUID = 1L;

    private Run<?,?> run;
    private String name;
    private AbstractBean<?> result;
    private AbstractBean<?> previousResult;
    private String[] buildTokens = new String[0];
    private boolean depthOfInheritance = true;
    private boolean childUrlLink = true;

    /**
     *
     * @param run
     * @param name
     * @param result
     */
    protected AbstractReport(Run<?,?> run, String name, AbstractBean<?> result) {
        this.run  = run;
        this.name   = name;
        this.result = result;
    }

    public Run<?, ?> getBuild() {
        return run;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return name;
    }

    public String[] getBuildTokens() {
        return buildTokens;
    }

    public boolean isDepthOfInheritance() {
        return depthOfInheritance;
    }

    public void setDepthOfInheritance(boolean depthOfInheritance) {
        this.depthOfInheritance = depthOfInheritance;
    }

    public boolean isChildUrlLink() {
        return childUrlLink;
    }

    public void setChildUrlLink(boolean childUrlLink) {
        this.childUrlLink = childUrlLink;
    }

    public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp) {
        return getReport(token);
    }

    public void doGraph(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        AbstractGraph graph = new MaintainabilityIndexGraph(run, buildTokens, run.getTimestamp(), Constants.TREND_GRAPH_WIDTH, Constants.TREND_GRAPH_HEIGHT);
        graph.doPng(req, rsp);
    }

    public void doMaiGraph(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        AbstractGraph graph = new MaintainabilityIndexGraph(run, buildTokens, run.getTimestamp(), Constants.REPORT_GRAPH_WIDTH, Constants.REPORT_GRAPH_HEIGHT);
        graph.doPng(req, rsp);
    }

    public void doCycGraph(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        AbstractGraph graph = new CyclomaticComplexityGraph(run, buildTokens, run.getTimestamp(), Constants.REPORT_GRAPH_WIDTH, Constants.REPORT_GRAPH_HEIGHT);
        graph.doPng(req, rsp);
    }

    public AbstractBean<?> getResult() {
        return result;
    }

    public AbstractBean<?> getPreviousResult() {
        if (previousResult != null) return previousResult;
        Run<?, ?> lastBuild = run.getPreviousBuild();
        while (lastBuild != null) {
            if (!lastBuild.isBuilding() && (lastBuild.getAction(VsCodeMetricsBuildAction.class) != null)) {
                VsCodeMetricsBuildAction action = lastBuild.getAction(VsCodeMetricsBuildAction.class);
                CodeMetrics metrics = action.getCodeMetrics();
                if (metrics != null) {
                    previousResult = CodeMetricsUtil.searchBean(metrics, buildTokens);
                    return previousResult;
                }
            }
            lastBuild = lastBuild.getPreviousBuild();
        }
        return null;
    }

    public Object getPreviousResult(String token) {
        AbstractBean<?> bean = getPreviousResult();
        if (bean == null) return null;
        if (bean.getChildren().containsKey(token))
            return bean.getChildren().get(token);
        else
            return null;
    }

    public boolean hasChildren() {
        return ((result != null) && (result.getChildren().size() > 0));
    }

    public Map<String, ?> getChildren() {
        return result.getChildren();
    }

    public void setBuildTokens(String token, String[] tokens) {
        int size = (tokens == null) ? 1 : tokens.length + 1;
        String[] arr = new String[size];
        if (tokens != null)
            System.arraycopy(tokens, 0, arr, 0, tokens.length);
        arr[size - 1] = token;
        buildTokens = arr;
    }

    public abstract Object getReport(String token);

}
