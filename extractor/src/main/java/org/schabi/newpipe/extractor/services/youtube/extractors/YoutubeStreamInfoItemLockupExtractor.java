package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link StreamInfoItemExtractor} for the {@code lockupViewModel} renderer.
 *
 * <p>
 * YouTube moved the related videos sidebar from {@code compactVideoRenderer} to this shape, which
 * nests everything one or two levels deeper and no longer labels its fields, so the metadata rows
 * have to be read positionally.
 * </p>
 */
public class YoutubeStreamInfoItemLockupExtractor implements StreamInfoItemExtractor {

    private final JsonObject lockupViewModel;
    private final TimeAgoParser timeAgoParser;

    public YoutubeStreamInfoItemLockupExtractor(final JsonObject lockupViewModel,
                                                @Nullable final TimeAgoParser timeAgoParser) {
        this.lockupViewModel = lockupViewModel;
        this.timeAgoParser = timeAgoParser;
    }

    @Nonnull
    private JsonObject getMetadataViewModel() {
        return lockupViewModel.getObject("metadata")
                .getObject("lockupMetadataViewModel");
    }

    /**
     * The rows below the title: the first holds the channel name, the second the view count and
     * the age of the video, or the viewer count while the stream is running.
     */
    @Nonnull
    private JsonArray getMetadataRows() {
        return getMetadataViewModel()
                .getObject("metadata")
                .getObject("contentMetadataViewModel")
                .getArray("metadataRows");
    }

    @Nullable
    private String getMetadataPart(final int row, final int part) {
        final JsonArray parts = getMetadataRows()
                .getObject(row)
                .getArray("metadataParts");
        if (part >= parts.size()) {
            return null;
        }
        final String text = parts.getObject(part)
                .getObject("text")
                .getString("content");
        return isNullOrEmpty(text) ? null : text.trim();
    }

    @Nonnull
    private JsonObject getChannelTapCommand() {
        return getMetadataViewModel()
                .getObject("image")
                .getObject("decoratedAvatarViewModel")
                .getObject("avatar")
                .getObject("avatarViewModel")
                .getObject("rendererContext")
                .getObject("commandContext")
                .getObject("onTap")
                .getObject("innertubeCommand");
    }

    @Override
    public String getName() throws ParsingException {
        final String title = getMetadataViewModel().getObject("title").getString("content");
        if (isNullOrEmpty(title)) {
            throw new ParsingException("Could not get name");
        }
        return title;
    }

    @Override
    public String getUrl() throws ParsingException {
        final String videoId = lockupViewModel.getString("contentId");
        if (isNullOrEmpty(videoId)) {
            throw new ParsingException("Could not get video id");
        }
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        final JsonArray sources = lockupViewModel.getObject("contentImage")
                .getObject("thumbnailViewModel")
                .getObject("image")
                .getArray("sources");
        if (sources.isEmpty()) {
            return "";
        }
        // Sources are ordered from smallest to largest
        final String url = sources.getObject(sources.size() - 1).getString("url");
        return isNullOrEmpty(url) ? "" : url;
    }

    @Override
    public StreamType getStreamType() {
        return isLive() ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    /**
     * @return the {@code 2:39:40} style badge drawn over the thumbnail, or {@code null} on
     *         livestreams, which are badged {@code LIVE} instead
     */
    @Nullable
    private String getDurationBadgeText() {
        final JsonArray overlays = lockupViewModel.getObject("contentImage")
                .getObject("thumbnailViewModel")
                .getArray("overlays");

        for (final Object overlay : overlays) {
            if (!(overlay instanceof JsonObject)) {
                continue;
            }
            final JsonArray badges = ((JsonObject) overlay)
                    .getObject("thumbnailBottomOverlayViewModel")
                    .getArray("badges");
            for (final Object badge : badges) {
                if (!(badge instanceof JsonObject)) {
                    continue;
                }
                final String text = ((JsonObject) badge)
                        .getObject("thumbnailBadgeViewModel")
                        .getString("text");
                if (!isNullOrEmpty(text)) {
                    return text;
                }
            }
        }
        return null;
    }

    private boolean isLive() {
        final String badge = getDurationBadgeText();
        if (badge == null) {
            return false;
        }
        // The badge of a running stream is a word, not a timestamp
        return !badge.contains(":");
    }

    @Override
    public long getDuration() throws ParsingException {
        if (isLive()) {
            return -1;
        }
        final String badge = getDurationBadgeText();
        if (badge == null) {
            return -1;
        }
        try {
            return YoutubeParsingHelper.parseDurationString(badge);
        } catch (final Exception e) {
            return -1;
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        final String text = getMetadataPart(1, 0);
        if (text == null) {
            return -1;
        }
        try {
            return Utils.mixedNumberWordToLong(text);
        } catch (final Exception e) {
            // Localized counts such as "343万回視聴" are not always parseable; the app treats a
            // negative count as unknown and simply omits it
            return -1;
        }
    }

    @Override
    public String getUploaderName() {
        final String name = getMetadataPart(0, 0);
        return name == null ? "" : name;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final JsonObject command = getChannelTapCommand();

        final String channelId = command.getObject("browseEndpoint").getString("browseId");
        if (!isNullOrEmpty(channelId)) {
            return "https://www.youtube.com/channel/" + channelId;
        }

        final String path = command.getObject("commandMetadata")
                .getObject("webCommandMetadata")
                .getString("url");
        if (!isNullOrEmpty(path)) {
            return "https://www.youtube.com" + path;
        }

        return null;
    }

    @Override
    public String getUploaderAvatarUrl() {
        final JsonArray sources = getMetadataViewModel()
                .getObject("image")
                .getObject("decoratedAvatarViewModel")
                .getObject("avatar")
                .getObject("avatarViewModel")
                .getObject("image")
                .getArray("sources");
        if (sources.isEmpty()) {
            return null;
        }
        final String url = sources.getObject(sources.size() - 1).getString("url");
        return isNullOrEmpty(url) ? null : url;
    }

    @Override
    public boolean isUploaderVerified() {
        // Not exposed by this renderer
        return false;
    }

    @Override
    public String getTextualUploadDate() {
        if (isLive()) {
            return null;
        }
        // The second row is "<view count> • <age>"; the age is only present on regular videos
        return getMetadataPart(1, 1);
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();
        if (timeAgoParser == null || isNullOrEmpty(textualUploadDate)) {
            return null;
        }
        try {
            return timeAgoParser.parse(textualUploadDate);
        } catch (final ParsingException e) {
            return null;
        }
    }
}
