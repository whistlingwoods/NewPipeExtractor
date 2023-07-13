package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

public class ChannelTabInfo extends ListInfo<InfoItem> {

    public ChannelTabInfo(int serviceId, String id, String url, String originalUrl, String name, ListLinkHandler listLinkHandler) {
        super(serviceId, id, url, originalUrl, name, listLinkHandler.getContentFilters(), listLinkHandler.getSortFilter());
    }

    public static ListExtractor.InfoItemsPage<InfoItem> getMoreItems(StreamingService service, String tabId,
                                                                     String url, String nextPageUrl) throws IOException, ExtractionException {
        return service.getChannelTabExtractor(tabId, url).getPage(nextPageUrl);
    }

    public static ChannelTabInfo getInfo(PlaceholderChannelTabInfo placeholderInfo) throws ExtractionException, IOException {
        final StreamingService service = NewPipe.getService(placeholderInfo.getServiceId());
        return getInfo(service.getChannelTabExtractor(placeholderInfo.getId(), placeholderInfo.getUrl()), placeholderInfo.isDefaultTab());
    }

    public static ChannelTabInfo getInfo(ChannelTabExtractor extractor, boolean isDefaultTab) throws ExtractionException, IOException {
        extractor.fetchPage();
        final int serviceId = extractor.getServiceId();
        final String id = extractor.getId();
        final String url = extractor.getUrl();
        final String originalUrl = extractor.getOriginalUrl();
        final String name = extractor.getName();

        final ChannelTabInfo info = new ChannelTabInfo(serviceId, id, url, originalUrl, name, extractor.getLinkHandler());

        final ListExtractor.InfoItemsPage<InfoItem> itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(itemsPage.getItems());
        info.setNextPageUrl(itemsPage.getNextPageUrl());
        info.setDefaultTab(isDefaultTab);

        return info;
    }

    private boolean isDefaultTab = false;

    public boolean isDefaultTab() {
        return isDefaultTab;
    }

    protected void setDefaultTab(boolean isDefaultTab) {
        this.isDefaultTab = isDefaultTab;
    }
}
