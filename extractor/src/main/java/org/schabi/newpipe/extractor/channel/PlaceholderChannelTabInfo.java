package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.Collections;
import java.util.List;

public class PlaceholderChannelTabInfo extends ChannelTabInfo {
    public PlaceholderChannelTabInfo(int serviceId, String id, String url, String originalUrl, String name, ListLinkHandler listLinkHandler) {
        super(serviceId, id, url, originalUrl, name, listLinkHandler);
    }

    public static PlaceholderChannelTabInfo getPlaceHolder(PlaceholderChannelTabExtractor extractor, boolean isDefaultTab) throws ExtractionException {
        final int serviceId = extractor.getServiceId();
        final String id = extractor.getId();
        final String url = extractor.getUrl();
        final String originalUrl = extractor.getOriginalUrl();
        final String name = extractor.getName();

        final PlaceholderChannelTabInfo placeholderChannelTabInfo = new PlaceholderChannelTabInfo(serviceId, id,
                url, originalUrl, name, extractor.getLinkHandler());
        placeholderChannelTabInfo.setDefaultTab(isDefaultTab);

        return placeholderChannelTabInfo;
    }

    @Override
    public List<InfoItem> getRelatedItems() {
        return Collections.emptyList();
    }

    @Override
    public String getNextPageUrl() {
        return null;
    }
}
