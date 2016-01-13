package org.jenkinsci.plugins.vs_code_metrics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Calendar;

import org.jenkinsci.plugins.vs_code_metrics.bean.AbstractBean;
import org.jenkinsci.plugins.vs_code_metrics.bean.CodeMetrics;
import org.jenkinsci.plugins.vs_code_metrics.util.CodeMetricsUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

import hudson.model.Run;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

public abstract class AbstractGraph extends Graph {

    private final Run<?, ?> run;
    private final String[] buildTokens;
    protected String valueKey = null;
    protected Integer upperBound = null;

    public AbstractGraph(Run<?, ?> run, String[] buildTokens, Calendar timestamp, int defaultW, int defaultH) {
        super(timestamp, defaultW, defaultH);
        this.run = run;
        this.buildTokens = buildTokens;
    }

    @Override
    protected JFreeChart createGraph() {
        CategoryDataset dataset = createDataset();
        JFreeChart chart = ChartFactory.createLineChart(null, null, valueKey, dataset, PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        if (upperBound != null)
            rangeAxis.setUpperBound(upperBound);
        rangeAxis.setLowerBound(0);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(2.0f));
        ColorPalette.apply(renderer);

        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }

    private CategoryDataset createDataset() {
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

        Run<?, ?> lastBuild = run;
        while (lastBuild != null) {
            if (!lastBuild.isBuilding() && (lastBuild.getAction(VsCodeMetricsBuildAction.class) != null)) {
                VsCodeMetricsBuildAction action = lastBuild.getAction(VsCodeMetricsBuildAction.class);

                if ((buildTokens == null || buildTokens.length == 0) && action.isMetricsValue()) {
                    NumberOnlyBuildLabel buildLabel;
                    buildLabel = new NumberOnlyBuildLabel((Run<?, ?>)lastBuild);
                    builder.add(getValue(action), valueKey, buildLabel);
                } else {
                    CodeMetrics metrics = action.getCodeMetrics();
                    if (metrics != null) {
                        AbstractBean<?> bean = CodeMetricsUtil.searchBean(metrics, buildTokens);

                        if (bean != null) {
                            NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel((Run<?, ?>)lastBuild);
                            builder.add(getValue(bean), valueKey, buildLabel);
                        }
                    }
                }
            }
            lastBuild = lastBuild.getPreviousBuild();
        }

        return builder.build();
    }

    protected abstract int getValue(AbstractBean<?> bean);

    protected abstract int getValue(VsCodeMetricsBuildAction action);
}
