package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class ChannelTabExtractorFactory {
    /**
     * @return a list containing all available channel tabs extractors ids in a service.
     */
    @Nonnull
    public abstract List<String> availableTabIdList();

    /**
     * @return the id of the default tab from the available list (see {@link #availableTabIdList()}).
     */
    public abstract String defaultTabId();

    /**
     * @param tabId              a tab id from the available list (see {@link #availableTabIdList()}).
     * @param channelLinkHandler a channel url link handler.
     * @return an instance of a {@link ChannelTabExtractor} by its tab id.
     */
    public ChannelTabExtractor getTabExtractor(final String tabId, ListLinkHandler channelLinkHandler) throws ExtractionException {
        if (!availableTabIdList().contains(tabId)) {
            throw new ExtractionException("Tab not recognized = " + tabId);
        }

        final ChannelTabExtractor tabExtractor = instantiateTabExtractor(tabId, channelLinkHandler);

        if (tabExtractor == null) {
            throw new ExtractionException("Tab extractor cannot be null (id = " + tabId + ")");
        }

        return tabExtractor;
    }

    /**
     * Actual method responsible for creating channel tab extractor instances.
     */
    protected abstract ChannelTabExtractor instantiateTabExtractor(final String tabId, ListLinkHandler channelLinkHandler)
            throws ExtractionException;
}
