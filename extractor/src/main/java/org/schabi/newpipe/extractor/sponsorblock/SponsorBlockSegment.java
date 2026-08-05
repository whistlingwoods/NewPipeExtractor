package org.schabi.newpipe.extractor.sponsorblock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SponsorBlockSegment implements Serializable {
    public String uuid;
    public double startTime;
    public double endTime;
    public SponsorBlockCategory category;
    public SponsorBlockAction action;
    public int votes;
    public boolean locked;
    public double videoDuration;
    public String description;
    public List<SponsorBlockSegment> chain;

    public SponsorBlockSegment(final String uuid, final double startTime, final double endTime,
                               final SponsorBlockCategory category,
                               final SponsorBlockAction action) {
        // NOTE: start/end times are in milliseconds

        this.uuid = uuid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.action = action;
        this.votes = 0;
        this.locked = false;
        this.videoDuration = 0.0;
        this.description = "";
        this.chain = new ArrayList<>();

        // since "highlight" segments are marked with the same start time and end time,
        // increment the end time by 1 second (so it is actually visible on the seekbar)
        if (this.category == SponsorBlockCategory.HIGHLIGHT) {
            this.endTime = this.startTime + 1000;
        }
    }

    public SponsorBlockSegment setMetadata(final int newVotes, final boolean isLocked,
                                           final double duration, final String desc) {
        this.votes = newVotes;
        this.locked = isLocked;
        this.videoDuration = duration;
        this.description = desc != null ? desc : "";
        return this;
    }

    public double getChainStartTime() {
        if (chain.isEmpty()) {
            return startTime;
        } else {
            return chain.get(0).startTime;
        }
    }

    public double getChainEndTime() {
        if (chain.isEmpty()) {
            return endTime;
        } else {
            return chain.get(chain.size() - 1).endTime;
        }
    }
}
