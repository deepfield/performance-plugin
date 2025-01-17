package hudson.plugins.performance.data;

import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.plugins.performance.PerformancePublisher;
import hudson.plugins.performance.reports.AbstractReport;
import hudson.plugins.performance.reports.UriReport;

public abstract class ReportValueSelector {

    public abstract long getValue(AbstractReport report);

    public abstract String getGraphType();

    public static ReportValueSelector get(Job<?, ?> job) {
        if (job instanceof AbstractProject) {
            // can't get a AbstractProject reference from PerformanceReportMap :/
            AbstractProject<?, ?> project = (AbstractProject<?, ?>) job;
            return get(project.getPublishersList().get(PerformancePublisher.class));
        }
        return get((PerformancePublisher) null);
    }

    public static ReportValueSelector get(PerformancePublisher publisher) {
        if (publisher == null)
            return new SelectAverage();

        String graphType = publisher.getGraphType();
        if (graphType == null)
            return new SelectAverage();
        if (graphType.equals(PerformancePublisher.MRT))
            return new SelectMedian();
        if (graphType.equals(PerformancePublisher.PRT))
            return new SelectPercentile();
        if (graphType.equals(PerformancePublisher.AMU))
            return new SelectAverageKb();
        if (graphType.equals(PerformancePublisher.MMU))
            return new SelectMaxKb();
        return new SelectAverage(); // default
    }

    // Public. The alternative is worse
    public static class SelectMaxKb extends ReportValueSelector {

        @Override
        public long getValue(AbstractReport report) {
            return (long)((UriReport)report).getMaxKb();
        }

        @Override
        public String getGraphType() {
            return PerformancePublisher.MMU;
        }
    }

    // Public. The alternative is worse
    public static class SelectAverageKb extends ReportValueSelector {

        @Override
        public long getValue(AbstractReport report) {
            return (long)((UriReport)report).getAverageKb();
        }

        @Override
        public String getGraphType() {
            return PerformancePublisher.AMU;
        }
    }

    private static class SelectAverage extends ReportValueSelector {

        @Override
        public long getValue(AbstractReport report) {
            return report.getAverage();
        }

        @Override
        public String getGraphType() {
            return PerformancePublisher.ART;
        }
    }

    private static class SelectMedian extends ReportValueSelector {

        @Override
        public long getValue(AbstractReport report) {
            return report.getMedian();
        }

        @Override
        public String getGraphType() {
            return PerformancePublisher.MRT;
        }
    }

    private static class SelectPercentile extends ReportValueSelector {

        @Override
        public long getValue(AbstractReport report) {
            return report.get90Line();
        }

        @Override
        public String getGraphType() {
            return PerformancePublisher.PRT;
        }
    }
}
