package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;

public abstract class ChannelTabExtractor extends ListExtractor<InfoItem> {
    private final String tabId;

    public ChannelTabExtractor(StreamingService service, String tabId, ListLinkHandler linkHandler) {
        super(service, linkHandler);
        this.tabId = tabId;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return tabId;
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
        return tabId;
    }
}
