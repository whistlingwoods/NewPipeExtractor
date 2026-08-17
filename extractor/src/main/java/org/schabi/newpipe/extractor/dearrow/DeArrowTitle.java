package org.schabi.newpipe.extractor.dearrow;

import java.io.Serializable;

public class DeArrowTitle implements Serializable {

    public final String title;
    public final boolean original;
    public final double votes;
    public final boolean locked;
    public final String uuid;
    public final String userId;

    public DeArrowTitle(final String title,
                        final boolean original,
                        final double votes,
                        final boolean locked,
                        final String uuid,
                        final String userId) {
        this.title = title;
        this.original = original;
        this.votes = votes;
        this.locked = locked;
        this.uuid = uuid;
        this.userId = userId;
    }

    /**
     * Removes the auto-formatting preservation indicator '&gt;' from words in the title.
     *
     * @return the cleaned title string suitable for display in player interfaces
     */
    public String getCleanTitle() {
        if (title == null) {
            return null;
        }
        return title.replaceAll("(^|\\s)>", "$1");
    }
}
