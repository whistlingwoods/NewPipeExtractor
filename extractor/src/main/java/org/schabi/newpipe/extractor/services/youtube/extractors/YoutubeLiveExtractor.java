package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeLiveLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Kiosk listing the livestreams YouTube is currently promoting.
 *
 * <p>
 * YouTube has no feed for these; the desktop site surfaces them as the {@code livetab} of its own
 * Live channel, so that is what is browsed here.
 * </p>
 */
public class YoutubeLiveExtractor extends KioskExtractor<StreamInfoItem> {

    private static final String LIVE_CHANNEL_ID = "UC4R8DWoMoI7CAwX8_LjQHig";
    private static final String LIVE_TAB_PARAMS = "EgdsaXZldGFikgEDCKEK";

    private JsonObject initialData;

    public YoutubeLiveExtractor(final StreamingService service,
                                final ListLinkHandler linkHandler,
                                final String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(getExtractorLocalization(),
                        getExtractorContentCountry())
                        .value("browseId", LIVE_CHANNEL_ID)
                        .value("params", LIVE_TAB_PARAMS)
                        .done())
                .getBytes(UTF_8);

        initialData = getJsonPostResponse("browse", body, getExtractorLocalization());
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return YoutubeLiveLinkHandlerFactory.KIOSK_ID;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ParsingException {
        return collectItems(getLiveTabContents());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getId())) {
            throw new IllegalArgumentException("Page doesn't have a continuation token");
        }

        final byte[] body = JsonWriter.string(
                prepareDesktopJsonBuilder(getExtractorLocalization(),
                        getExtractorContentCountry())
                        .value("continuation", page.getId())
                        .done())
                .getBytes(UTF_8);

        final JsonObject response = getJsonPostResponse("browse", body,
                getExtractorLocalization());

        return collectItems(response.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems"));
    }

    @Nonnull
    private JsonArray getLiveTabContents() {
        return initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .getObject(0)
                .getObject("tabRenderer")
                .getObject("content")
                .getObject("richGridRenderer")
                .getArray("contents");
    }

    @Nonnull
    private InfoItemsPage<StreamInfoItem> collectItems(@Nonnull final JsonArray contents)
            throws ParsingException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        contents.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(content -> content.has("richItemRenderer"))
                .map(content -> content.getObject("richItemRenderer")
                        .getObject("content")
                        .getObject("videoRenderer"))
                .filter(videoRenderer -> !isNullOrEmpty(videoRenderer))
                .forEachOrdered(videoRenderer -> collector.commit(
                        new YoutubeStreamInfoItemExtractor(videoRenderer, timeAgoParser)));

        return new InfoItemsPage<>(collector, getNextPage(contents));
    }

    @Nullable
    private Page getNextPage(@Nonnull final JsonArray contents) throws ParsingException {
        final String token = contents.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(content -> content.has("continuationItemRenderer"))
                .map(content -> content.getObject("continuationItemRenderer")
                        .getObject("continuationEndpoint")
                        .getObject("continuationCommand")
                        .getString("token"))
                .filter(t -> !isNullOrEmpty(t))
                .findFirst()
                .orElse(null);

        return token == null ? null : new Page(getUrl(), token);
    }
}
