package org.schabi.newpipe.extractor.sponsorblock;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.utils.RandomStringFromAlphabetGenerator;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

public final class SponsorBlockExtractorHelper {
    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random NUMBER_GENERATOR = new SecureRandom();

    private SponsorBlockExtractorHelper() {
    }

    private static String normalizeApiUrl(final @Nullable String apiUrl) {
        if (apiUrl == null || apiUrl.isEmpty()) {
            return "";
        }
        return apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
    }

    private static String getOrGenerateUserId(final @Nullable String providedUserId) {
        if (providedUserId != null && providedUserId.length() >= 32) {
            return providedUserId;
        }
        return RandomStringFromAlphabetGenerator.generate(ALPHABET, 32, NUMBER_GENERATOR);
    }

    public static SponsorBlockSegment[] getSegments(final StreamInfo streamInfo,
                                                    final SponsorBlockApiSettings apiSettings)
            throws UnsupportedEncodingException {
        final String apiUrl = normalizeApiUrl(apiSettings.apiUrl);
        if (!streamInfo.getUrl().startsWith("https://www.youtube.com")
                || apiUrl.isEmpty()) {
            return new SponsorBlockSegment[0];
        }

        final String videoId = streamInfo.getId();

        final ArrayList<String> categoryParamList = new ArrayList<>();

        if (apiSettings.includeSponsorCategory) {
            categoryParamList.add(SponsorBlockCategory.SPONSOR.getApiName());
        }
        if (apiSettings.includeIntroCategory) {
            categoryParamList.add(SponsorBlockCategory.INTRO.getApiName());
        }
        if (apiSettings.includeOutroCategory) {
            categoryParamList.add(SponsorBlockCategory.OUTRO.getApiName());
        }
        if (apiSettings.includeInteractionCategory) {
            categoryParamList.add(SponsorBlockCategory.INTERACTION.getApiName());
        }
        if (apiSettings.includeHighlightCategory) {
            categoryParamList.add(SponsorBlockCategory.HIGHLIGHT.getApiName());
        }
        if (apiSettings.includeSelfPromoCategory) {
            categoryParamList.add(SponsorBlockCategory.SELF_PROMO.getApiName());
        }
        if (apiSettings.includeMusicCategory) {
            categoryParamList.add(SponsorBlockCategory.NON_MUSIC.getApiName());
        }
        if (apiSettings.includePreviewCategory) {
            categoryParamList.add(SponsorBlockCategory.PREVIEW.getApiName());
        }

        if (apiSettings.includeFillerCategory) {
            categoryParamList.add(SponsorBlockCategory.FILLER.getApiName());
        }
        if (apiSettings.includeHookCategory) {
            categoryParamList.add(SponsorBlockCategory.HOOK.getApiName());
        }
        if (apiSettings.includeExclusiveAccessCategory) {
            categoryParamList.add(SponsorBlockCategory.EXCLUSIVE_ACCESS.getApiName());
        }
        if (apiSettings.includeChapterCategory) {
            categoryParamList.add(SponsorBlockCategory.CHAPTER.getApiName());
        }

        if (categoryParamList.isEmpty()) {
            return new SponsorBlockSegment[0];
        }

        final String categoryParams = Utils.encodeUrlUtf8(
                "[\"" + String.join("\",\"", categoryParamList) + "\"]");

        final String actionParams = Utils.encodeUrlUtf8(
                "[\"skip\",\"mute\",\"poi\",\"full\",\"chapter\"]");

        final String videoIdHash;
        try {
            videoIdHash = Utils.toSha256(videoId);
        } catch (final NoSuchAlgorithmException e) {
            return new SponsorBlockSegment[0];
        }

        final String url = apiUrl + "skipSegments/" + videoIdHash.substring(0, 4)
                + "?categories=" + categoryParams
                + "&actionTypes=" + actionParams
                + "&userAgent=Mozilla/5.0";

        JsonArray responseArray = null;

        try {
            final String responseBody = NewPipe.getDownloader().get(url).responseBody();

            responseArray = JsonParser.array().from(responseBody);
        } catch (ReCaptchaException | IOException | JsonParserException e) {
            // ignored
        }

        if (responseArray == null) {
            return new SponsorBlockSegment[0];
        }

        final ArrayList<SponsorBlockSegment> result = new ArrayList<>();

        for (final Object obj1 : responseArray) {
            final JsonObject jObj1 = (JsonObject) obj1;

            final String responseVideoId = jObj1.getString("videoID");
            if (!responseVideoId.equals(videoId)) {
                continue;
            }

            final JsonArray segmentArray = (JsonArray) jObj1.get("segments");
            if (segmentArray == null) {
                continue;
            }

            for (final Object obj2 : segmentArray) {
                final JsonObject jObj2 = (JsonObject) obj2;

                final JsonArray segmentInfo = (JsonArray) jObj2.get("segment");
                if (segmentInfo == null) {
                    continue;
                }

                final String uuid = jObj2.getString("UUID");
                final double startTime = segmentInfo.getDouble(0) * 1000;
                final double endTime = segmentInfo.getDouble(1) * 1000;
                final String category = jObj2.getString("category");
                final String action = jObj2.getString("actionType");
                final int votes = jObj2.getInt("votes", 0);
                final boolean locked = jObj2.getInt("locked", 0) > 0
                        || jObj2.getBoolean("locked", false);
                final double videoDuration = jObj2.getDouble("videoDuration", 0.0);
                final String description = jObj2.getString("description", "");

                final SponsorBlockSegment sponsorBlockSegment =
                        new SponsorBlockSegment(uuid, startTime, endTime,
                                SponsorBlockCategory.fromApiName(category),
                                SponsorBlockAction.fromApiName(action))
                                .setMetadata(votes, locked, videoDuration, description);
                result.add(sponsorBlockSegment);
            }
        }

        return result.toArray(new SponsorBlockSegment[0]);
    }

    public static Response submitSponsorBlockSegment(
            final StreamInfo streamInfo,
            final SponsorBlockSegment segment,
            final String apiUrl)
            throws IOException, ReCaptchaException {
        return submitSponsorBlockSegment(streamInfo, segment, apiUrl, null);
    }

    public static Response submitSponsorBlockSegment(
            final StreamInfo streamInfo,
            final SponsorBlockSegment segment,
            final String apiUrl,
            final @Nullable String userId)
            throws IOException, ReCaptchaException {
        if (segment.category == SponsorBlockCategory.PENDING) {
            return null;
        }

        final String normalizedApiUrl = normalizeApiUrl(apiUrl);
        if (normalizedApiUrl.isEmpty()
                || !streamInfo.getUrl().startsWith("https://www.youtube.com")) {
            return null;
        }

        final String videoId = streamInfo.getId();
        final String localUserId = getOrGenerateUserId(userId);

        final String actionType;
        if (segment.action != null) {
            actionType = segment.action.getApiName();
        } else if (segment.category == SponsorBlockCategory.HIGHLIGHT) {
            actionType = "poi";
        } else {
            actionType = "skip";
        }

        final double startInSeconds = segment.startTime / 1000.0;
        final double endInSeconds = segment.category == SponsorBlockCategory.HIGHLIGHT
                ? startInSeconds
                : segment.endTime / 1000.0;

        final double durationInSeconds = streamInfo.getDuration();
        final String durationParam = durationInSeconds > 0
                ? "&videoDuration=" + durationInSeconds
                : "";
        final String descriptionParam = segment.description != null
                && !segment.description.isEmpty()
                ? "&description=" + Utils.encodeUrlUtf8(segment.description)
                : "";

        final String url = normalizedApiUrl + "skipSegments?"
                + "videoID=" + videoId
                + "&startTime=" + startInSeconds
                + "&endTime=" + endInSeconds
                + "&category=" + segment.category.getApiName()
                + "&userID=" + localUserId
                + "&userAgent=Mozilla/5.0"
                + "&actionType=" + actionType
                + durationParam
                + descriptionParam;
        return NewPipe.getDownloader().post(url, null, new byte[0]);
    }

    public static Response submitSponsorBlockSegmentVote(final String uuid,
                                                         final String apiUrl,
                                                         final int vote)
            throws IOException, ReCaptchaException {
        return submitSponsorBlockSegmentVote(uuid, apiUrl, vote, null);
    }

    public static Response submitSponsorBlockSegmentVote(final String uuid,
                                                         final String apiUrl,
                                                         final int vote,
                                                         final @Nullable String userId)
            throws IOException, ReCaptchaException {
        final String normalizedApiUrl = normalizeApiUrl(apiUrl);
        if (normalizedApiUrl.isEmpty()) {
            return null;
        }

        final String localUserId = getOrGenerateUserId(userId);
        final String url = normalizedApiUrl + "voteOnSponsorTime?"
                + "UUID=" + uuid
                + "&userID=" + localUserId
                + "&type=" + vote;

        return NewPipe.getDownloader().post(url, null, new byte[0]);
    }

    public static Response reportViewedSegment(final String uuid, final String apiUrl)
            throws IOException, ReCaptchaException {
        final String normalizedApiUrl = normalizeApiUrl(apiUrl);
        if (normalizedApiUrl.isEmpty() || uuid == null || uuid.isEmpty()) {
            return null;
        }
        final String url = normalizedApiUrl + "viewedVideoSponsorTime?UUID=" + uuid;
        return NewPipe.getDownloader().post(url, null, new byte[0]);
    }

    public static Response submitSponsorBlockCategoryVote(final String uuid,
                                                          final SponsorBlockCategory category,
                                                          final String apiUrl)
            throws IOException, ReCaptchaException {
        return submitSponsorBlockCategoryVote(uuid, category, apiUrl, null);
    }

    public static Response submitSponsorBlockCategoryVote(final String uuid,
                                                          final SponsorBlockCategory category,
                                                          final String apiUrl,
                                                          final @Nullable String userId)
            throws IOException, ReCaptchaException {
        final String normalizedApiUrl = normalizeApiUrl(apiUrl);
        if (normalizedApiUrl.isEmpty() || category == SponsorBlockCategory.PENDING) {
            return null;
        }

        final String localUserId = getOrGenerateUserId(userId);
        final String url = normalizedApiUrl + "voteOnSponsorTime?"
                + "UUID=" + uuid
                + "&userID=" + localUserId
                + "&category=" + category.getApiName();

        return NewPipe.getDownloader().post(url, null, new byte[0]);
    }
}
