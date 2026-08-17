package org.schabi.newpipe.extractor.returnyoutubedislike;

import java.io.Serializable;

public class ReturnYouTubeDislikeInfo implements Serializable {
    public String id = "";
    public String dateCreated = "";
    public long likes;
    public long dislikes;
    public long rawLikes;
    public long rawDislikes;
    public double rating;
    public long viewCount;
    public boolean deleted;

    public ReturnYouTubeDislikeInfo(final long likes, final long dislikes, final double rating,
                                    final long viewCount, final boolean deleted) {
        this.likes = likes;
        this.dislikes = dislikes;
        this.rating = rating;
        this.viewCount = viewCount;
        this.deleted = deleted;
    }

    public ReturnYouTubeDislikeInfo setExtraInfo(final String videoId,
                                                 final String date,
                                                 final long rawL,
                                                 final long rawD) {
        this.id = videoId;
        this.dateCreated = date;
        this.rawLikes = rawL;
        this.rawDislikes = rawD;
        return this;
    }
}
