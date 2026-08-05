package org.schabi.newpipe.extractor.dearrow;

import java.io.Serializable;

public class DeArrowThumbnail implements Serializable {

    public final double timestamp;
    public final boolean original;
    public final double votes;
    public final boolean locked;
    public final String uuid;
    public final String userId;

    public DeArrowThumbnail(final double timestamp,
                            final boolean original,
                            final double votes,
                            final boolean locked,
                            final String uuid,
                            final String userId) {
        this.timestamp = timestamp;
        this.original = original;
        this.votes = votes;
        this.locked = locked;
        this.uuid = uuid;
        this.userId = userId;
    }

    /**
     * Constructs the direct rendering URL for this timestamp thumbnail.
     *
     * @param videoId     the YouTube video ID string
     * @param apiSettings the API settings containing the thumbnail service URL
     * @return the HTTP URL string to fetch the generated thumbnail image, or null if original
     */
    public String getThumbnailUrl(final String videoId,
                                  final DeArrowApiSettings apiSettings) {
        if (original || videoId == null || videoId.isEmpty() || apiSettings == null) {
            return null;
        }
        String baseUrl = apiSettings.thumbnailUrl;
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = DeArrowApiSettings.DEFAULT_THUMBNAIL_URL;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        return baseUrl + "api/v1/getThumbnail?videoID=" + videoId + "&time=" + timestamp;
    }
}
