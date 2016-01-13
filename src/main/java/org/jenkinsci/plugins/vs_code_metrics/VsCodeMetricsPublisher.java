package org.jenkinsci.plugins.vs_code_metrics;

import java.io.IOException;
import java.io.PrintStream;

import org.jenkinsci.plugins.vs_code_metrics.util.CodeMetricsUtil;
import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
import org.jenkinsci.plugins.vs_code_metrics.util.StringUtil;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import jenkins.tasks.SimpleBuildStep;

/**
 * @author Yasuyuki Saito
 */
public class VsCodeMetricsPublisher extends Recorder implements SimpleBuildStep {

    private final String reportFiles;
    private final VsCodeMetricsThresholds thresholds;
    private final boolean failBuild;

    @DataBoundConstructor
    public VsCodeMetricsPublisher(String reportFiles, int minMaintainabilityIndex, int maxMaintainabilityIndex, boolean failBuild) {
        this.reportFiles = reportFiles;
        this.failBuild   = failBuild;

        if (minMaintainabilityIndex >= maxMaintainabilityIndex)
            this.thresholds = new VsCodeMetricsThresholds(Constants.MIN_MAINTAINABILITY_INDEX, Constants.MAX_MAINTAINABILITY_INDEX);
        else
            this.thresholds = new VsCodeMetricsThresholds(minMaintainabilityIndex, maxMaintainabilityIndex);
    }

    public String getReportFiles() {
        return reportFiles;
    }

    public int getMinMaintainabilityIndex() {
        return thresholds.getMinMaintainabilityIndex();
    }

    public int getMaxMaintainabilityIndex() {
        return thresholds.getMaxMaintainabilityIndex();
    }


    public boolean isFailBuild() {
        return failBuild;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        if (StringUtil.isNullOrSpace(reportFiles)) return false;

        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());
        String includes = env.expand(reportFiles);

        performInternal(build, build.getWorkspace(), listener, includes);
        
        return true;
    }
    
    @Override
    public void perform(Run<?, ?> run, FilePath filePath, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        performInternal(run, filePath, listener, reportFiles);
    }

    private void performInternal(Run<?, ?> run, FilePath fp, TaskListener listener, String includes) throws InterruptedException, IOException {
        final PrintStream logger = listener.getLogger();

        logger.println("Code Metrics Report path: " + includes);
        FilePath[] reports = CodeMetricsUtil.locateReports(fp, includes);

        if (reports.length == 0) {
            if (run.getResult().isWorseThan(Result.UNSTABLE)) {
                return;
            }

            logger.println("Code Metrics Report Not Found.");
            run.setResult((failBuild) ? Result.FAILURE : Result.UNSTABLE);
            return;
        }

        FilePath metricsFolder = new FilePath(CodeMetricsUtil.getReportDir(run));
        if (!CodeMetricsUtil.saveReports(metricsFolder, reports)) {
            logger.println("Code Metrics Report Convert Error.");
            run.setResult((failBuild) ? Result.FAILURE : Result.UNSTABLE);
            return;
        }

        VsCodeMetricsBuildAction action = new VsCodeMetricsBuildAction(run, thresholds);
        run.getActions().add(action);
    }

    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new VsCodeMetricsProjectAction(project);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
         return DESCRIPTOR;
    }

    /**
     * Descriptor should be singleton.
     */
    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(VsCodeMetricsPublisher.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.VsCodeMetricsPublisher_DisplayName();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
