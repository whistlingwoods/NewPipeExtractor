package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class PlaceholderChannelTabExtractor extends ChannelTabExtractor {
    public PlaceholderChannelTabExtractor(StreamingService service, String tabId, ListLinkHandler linkHandler) {
        super(service, tabId, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {}

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() { return InfoItemsPage.emptyPage(); }

    @Override
    public String getNextPageUrl() { return null; }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) { return null; }
}
