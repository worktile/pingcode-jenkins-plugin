package io.jenkins.plugins.worktile.model;

public class WTBuildEntity {
    public final String provider = "jenkins";

    private final String name;
    private final String identifier;
    private final String jobUrl;
    private final String resultOverview;
    private final String resultUrl;
    private final String status;
    private final String[] workItemIdentifiers;
    private final long startAt;
    private final long endAt;
    private final long duration;

    private WTBuildEntity(final Builder builder) {
        this.name = builder.name;
        this.identifier = builder.identifier;
        this.jobUrl = builder.jobUrl;
        this.resultOverview = builder.resultOverview;
        this.resultUrl = builder.resultUrl;
        this.status = builder.status;
        this.workItemIdentifiers = builder.workItemIdentifiers;
        this.startAt = builder.startAt;
        this.endAt = builder.endAt;
        this.duration = builder.duration;
    }

    public long getDuration() {
        return duration;
    }

    public long getEndAt() {
        return endAt;
    }

    public long getStartAt() {
        return startAt;
    }

    public String[] getWorkItemIdentifiers() {
        return workItemIdentifiers;
    }

    public String getStatus() {
        return status;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public String getResultOverview() {
        return resultOverview;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.format("status:%s;name=%s;identifier=%s;resultOverview=%s", this.getStatus(), this.getName(),
                this.getIdentifier(), this.getResultOverview());
    }

    public static final class Builder {
        private String name;
        private String identifier;
        private String status;
        private long startAt;
        private long endAt;
        private long duration;

        private String jobUrl;
        private String resultUrl;
        private String resultOverview;
        private String[] workItemIdentifiers;

        public WTBuildEntity build() {
            return new WTBuildEntity(this);
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withIdentifier(final String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withStatus(final String status) {
            this.status = status;
            return this;
        }

        public Builder withJobUrl(final String jobUrl) {
            this.jobUrl = jobUrl;
            return this;
        }

        public Builder withRusultUrl(final String resultUrl) {
            this.resultUrl = resultUrl;
            return this;
        }

        public Builder withWorkItemIdentifiers(final String[] workItems) {
            this.workItemIdentifiers = workItems;
            return this;
        }

        public Builder withResultOvervier(final String resultOverview) {
            this.resultOverview = resultOverview;
            return this;
        }

        public Builder withStartAt(final long startAt) {
            this.startAt = startAt;
            return this;
        }

        public Builder withEndAt(final long endAt) {
            this.endAt = endAt;
            return this;
        }

        public Builder withDuration(final long duration) {
            this.duration = duration;
            return this;
        }
    }
}
