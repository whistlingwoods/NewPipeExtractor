package org.schabi.newpipe.extractor.returnyoutubedislike;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReturnYouTubeDislikeExtractorHelper {

    private ReturnYouTubeDislikeExtractorHelper() {
    }

    @SuppressWarnings("CheckStyle")
    public static ReturnYouTubeDislikeInfo getInfo(
            final StreamInfo streamInfo,
            final ReturnYouTubeDislikeApiSettings apiSettings) {
        if (streamInfo == null || streamInfo.getServiceId() != ServiceList.YouTube.getServiceId()) {
            return null;
        }
        return getInfo(streamInfo.getId(), streamInfo.getLikeCount(), apiSettings);
    }

    public static ReturnYouTubeDislikeInfo getInfo(
            final String videoId,
            final long likeCount,
            final ReturnYouTubeDislikeApiSettings apiSettings) {
        final String apiUrl = getNormalizedApiUrl(apiSettings);
        if (apiUrl == null || videoId == null || videoId.isEmpty()) {
            return null;
        }

        final StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("Votes?videoId=").append(videoId);
        if (likeCount > 0) {
            urlBuilder.append("&likeCount=").append(likeCount);
        }

        JsonObject response = null;
        try {
            final String responseBody = org.schabi.newpipe.extractor.NewPipe.getDownloader()
                    .get(urlBuilder.toString()).responseBody();

            response = JsonParser.object().from(responseBody);
        } catch (final ReCaptchaException | IOException | JsonParserException e) {
            // ignored
        }

        return parseInfo(response);
    }

    public static List<ReturnYouTubeDislikeInfo> getInfo(
            final List<String> videoIds,
            final ReturnYouTubeDislikeApiSettings apiSettings) {
        if (videoIds == null || videoIds.isEmpty()
                || getNormalizedApiUrl(apiSettings) == null) {
            return Collections.emptyList();
        }

        final List<ReturnYouTubeDislikeInfo> results = new ArrayList<>();
        for (final String videoId : videoIds) {
            if (videoId != null && !videoId.isEmpty()) {
                final ReturnYouTubeDislikeInfo info = getInfo(videoId, 0L, apiSettings);
                if (info != null) {
                    results.add(info);
                }
            }
        }
        return results;
    }

    /**
     * Sends a list of video IDs to the RYD server via POST /votes.
     * This registers the videos with the backend for cache warmup and tracking without
     * returning immediate statistics (as the server returns an empty response body).
     *
     * @param videoIds    the list of YouTube video ID strings
     * @param apiSettings the API settings configuration containing the API URL
     */
    public static void sendVideoIdsForCacheWarmup(
            final List<String> videoIds,
            final ReturnYouTubeDislikeApiSettings apiSettings) {
        final String apiUrl = getNormalizedApiUrl(apiSettings);
        if (apiUrl == null || videoIds == null || videoIds.isEmpty()) {
            return;
        }

        final JsonArray array = new JsonArray();
        for (final String videoId : videoIds) {
            if (videoId != null && !videoId.isEmpty()) {
                array.add(videoId);
            }
        }
        final byte[] body = JsonWriter.string(array).getBytes(StandardCharsets.UTF_8);

        final String url = apiUrl + "Votes";
        try {
            org.schabi.newpipe.extractor.NewPipe.getDownloader().postWithContentType(
                    url, null, body, "application/json");
        } catch (final ReCaptchaException | IOException e) {
            // ignored
        }
    }

    private static String getNormalizedApiUrl(
            final ReturnYouTubeDislikeApiSettings apiSettings) {
        if (apiSettings == null || apiSettings.apiUrl == null
                || apiSettings.apiUrl.isEmpty()) {
            return null;
        }
        String url = apiSettings.apiUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    private static ReturnYouTubeDislikeInfo parseInfo(final JsonObject response) {
        if (response == null) {
            return null;
        }
        final long likes = response.getLong("likes", 0L);
        final long dislikes = response.getLong("dislikes", 0L);
        final double rating = response.getDouble("rating", 0);
        final long viewCount = response.getLong("viewCount", 0L);
        final boolean deleted = response.getBoolean("deleted", false);
        final String id = response.getString("id", "");
        final String dateCreated = response.getString("dateCreated", "");
        final long rawLikes = response.getLong("rawLikes", 0L);
        final long rawDislikes = response.getLong("rawDislikes", 0L);

        return new ReturnYouTubeDislikeInfo(likes, dislikes, rating, viewCount, deleted)
                .setExtraInfo(id, dateCreated, rawLikes, rawDislikes);
    }
}
