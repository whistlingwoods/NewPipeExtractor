package org.schabi.newpipe.extractor.dearrow;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class DeArrowInfo implements Serializable {

    public final List<DeArrowTitle> titles;
    public final List<DeArrowThumbnail> thumbnails;
    public final double randomTime;
    public final double videoDuration;

    public DeArrowInfo(final List<DeArrowTitle> titles,
                       final List<DeArrowThumbnail> thumbnails,
                       final double randomTime,
                       final double videoDuration) {
        this.titles = titles == null ? Collections.emptyList()
                : Collections.unmodifiableList(titles);
        this.thumbnails = thumbnails == null ? Collections.emptyList()
                : Collections.unmodifiableList(thumbnails);
        this.randomTime = randomTime;
        this.videoDuration = videoDuration;
    }

    /**
     * Returns the highest quality trusted title replacement.
     * According to API guidelines, data is returned in order of quality, but elements must be
     * verified to have either locked = true or votes &gt;= 0 before being trusted as a replacement.
     *
     * @return the best trusted DeArrowTitle, or null if no trusted titles exist
     */
    public DeArrowTitle getBestTitle() {
        for (final DeArrowTitle item : titles) {
            if (item != null && (item.locked || item.votes >= 0)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Returns the highest quality trusted thumbnail replacement.
     *
     * @return the best trusted DeArrowThumbnail, or null if no trusted thumbnails exist
     */
    public DeArrowThumbnail getBestThumbnail() {
        for (final DeArrowThumbnail item : thumbnails) {
            if (item != null && (item.locked || item.votes >= 0)) {
                return item;
            }
        }
        return null;
    }
}
