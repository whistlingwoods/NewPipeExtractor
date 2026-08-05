package org.schabi.newpipe.extractor.dearrow;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DeArrowExtractorHelper {

    private DeArrowExtractorHelper() {
    }

    public static DeArrowInfo getInfo(
            @Nullable final StreamInfo streamInfo,
            @Nullable final DeArrowApiSettings apiSettings) {
        if (streamInfo == null || apiSettings == null) {
            return null;
        }
        return getInfo(streamInfo.getId(), apiSettings);
    }

    public static DeArrowInfo getInfo(
            @Nullable final String videoId,
            @Nullable final DeArrowApiSettings apiSettings) {
        final String apiUrl = getNormalizedApiUrl(apiSettings);
        if (apiUrl == null || videoId == null || videoId.isEmpty()) {
            return null;
        }

        final String queryParams = buildQueryParams(apiSettings,
                apiSettings.useHashPrefix ? null : videoId);
        final String url;
        if (apiSettings.useHashPrefix) {
            String hashPrefix = null;
            try {
                final String sha256 = Utils.toSha256(videoId);
                if (sha256 != null && sha256.length() >= 4) {
                    hashPrefix = sha256.substring(0, 4);
                }
            } catch (final NoSuchAlgorithmException e) {
                // Ignore and fall back to direct request if SHA256 is unavailable
            }
            if (hashPrefix != null) {
                url = apiUrl + "api/branding/" + hashPrefix + "?" + queryParams;
            } else {
                url = apiUrl + "api/branding?" + buildQueryParams(apiSettings, videoId);
            }
        } else {
            url = apiUrl + "api/branding?" + queryParams;
        }

        try {
            final String responseBody = NewPipe.getDownloader()
                    .get(url, Collections.emptyMap()).responseBody();
            if (responseBody == null || responseBody.isEmpty()) {
                return null;
            }
            final JsonObject response = JsonParser.object().from(responseBody);
            if (apiSettings.useHashPrefix && !url.contains("videoID=")) {
                final JsonObject videoData = response.getObject(videoId);
                return videoData != null ? parseInfo(videoData) : null;
            }
            return parseInfo(response);
        } catch (final ReCaptchaException | IOException | JsonParserException e) {
            return null;
        }
    }

    private static String buildQueryParams(
            final DeArrowApiSettings settings,
            @Nullable final String videoId) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (videoId != null && !videoId.isEmpty()) {
            sb.append("videoID=").append(Utils.encodeUrlUtf8(videoId));
            first = false;
        }
        if (settings.service != null && !settings.service.isEmpty()) {
            if (!first) {
                sb.append("&");
            }
            sb.append("service=").append(Utils.encodeUrlUtf8(settings.service));
            first = false;
        }
        if (settings.returnUserId) {
            if (!first) {
                sb.append("&");
            }
            sb.append("returnUserID=true");
            first = false;
        }
        if (settings.fetchAll) {
            if (!first) {
                sb.append("&");
            }
            sb.append("fetchAll=true");
        }
        return sb.toString();
    }

    public static DeArrowInfo parseInfo(final JsonObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        final JsonArray titlesArray = jsonObject.getArray("titles");
        final List<DeArrowTitle> titles = new ArrayList<>();
        if (titlesArray != null) {
            for (int i = 0; i < titlesArray.size(); i++) {
                final JsonObject obj = titlesArray.getObject(i);
                if (obj != null) {
                    titles.add(new DeArrowTitle(
                            obj.getString("title"),
                            obj.getBoolean("original", false),
                            obj.getDouble("votes", 0.0),
                            obj.getBoolean("locked", false),
                            obj.getString("UUID"),
                            obj.getString("userID")
                    ));
                }
            }
        }

        final JsonArray thumbnailsArray = jsonObject.getArray("thumbnails");
        final List<DeArrowThumbnail> thumbnails = new ArrayList<>();
        if (thumbnailsArray != null) {
            for (int i = 0; i < thumbnailsArray.size(); i++) {
                final JsonObject obj = thumbnailsArray.getObject(i);
                if (obj != null) {
                    thumbnails.add(new DeArrowThumbnail(
                            obj.getDouble("timestamp", 0.0),
                            obj.getBoolean("original", false),
                            obj.getDouble("votes", 0.0),
                            obj.getBoolean("locked", false),
                            obj.getString("UUID"),
                            obj.getString("userID")
                    ));
                }
            }
        }

        final double randomTime = jsonObject.getDouble("randomTime", 0.0);
        final double videoDuration = jsonObject.getDouble("videoDuration", 0.0);

        return new DeArrowInfo(titles, thumbnails, randomTime, videoDuration);
    }

    /**
     * Submits a title or thumbnail replacement to DeArrow via POST /api/branding.
     *
     * @param videoId   the YouTube video ID string
     * @param title     optional custom title replacement
     * @param timestamp optional thumbnail timestamp
     * @param original  true if submitting original branding as best choice
     * @param settings  configuration containing local user UUID and user agent
     * @throws IOException on network communication failure
     * @throws ReCaptchaException on reCAPTCHA challenge interception
     */
    public static void submitBranding(
            @Nullable final String videoId,
            @Nullable final String title,
            final double timestamp,
            final boolean original,
            @Nullable final DeArrowApiSettings settings)
            throws IOException, ReCaptchaException {
        final String apiUrl = getNormalizedApiUrl(settings);
        if (apiUrl == null || videoId == null || videoId.isEmpty()) {
            return;
        }

        final String userId = getOrGenerateUserId(settings.localUserId);
        final String userAgent = settings.userAgent != null
                ? settings.userAgent : "NewPipeExtractor/1.0";
        final String service = settings.service != null
                ? settings.service : "YouTube";

        final JsonObject obj = new JsonObject();
        obj.put("videoID", videoId);
        obj.put("userID", userId);
        obj.put("userAgent", userAgent);
        obj.put("service", service);

        if (title != null && !title.isEmpty()) {
            final JsonObject titleObj = new JsonObject();
            titleObj.put("title", title);
            obj.put("title", titleObj);
        } else {
            final JsonObject thumbObj = new JsonObject();
            thumbObj.put("timestamp", timestamp);
            thumbObj.put("original", original);
            obj.put("thumbnail", thumbObj);
        }

        final byte[] body = JsonWriter.string(obj).getBytes(StandardCharsets.UTF_8);
        NewPipe.getDownloader().postWithContentType(
                apiUrl + "api/branding", null, body, "application/json");
    }

    private static String getNormalizedApiUrl(
            @Nullable final DeArrowApiSettings apiSettings) {
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

    private static String getOrGenerateUserId(@Nullable final String providedUserId) {
        if (providedUserId != null && !providedUserId.trim().isEmpty()) {
            return providedUserId;
        }
        return UUID.randomUUID().toString();
    }
}
