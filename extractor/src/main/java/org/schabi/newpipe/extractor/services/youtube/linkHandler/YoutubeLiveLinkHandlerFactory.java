package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL;

/**
 * Link handler of the kiosk listing the livestreams YouTube is currently promoting.
 *
 * <p>
 * There is no dedicated feed for these: they are the {@code livetab} of YouTube's own Live
 * channel, which is what the desktop site links to as well.
 * </p>
 */
public class YoutubeLiveLinkHandlerFactory extends ListLinkHandlerFactory {

    public static final String KIOSK_ID = "live";

    private static final String LIVE_CHANNEL_PATH = "/channel/UC4R8DWoMoI7CAwX8_LjQHig/livetab";
    private static final String LIVE_CHANNEL_TAB_PARAMS = "ss=CKEK";

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter) {
        return "https://www.youtube.com" + LIVE_CHANNEL_PATH + "?" + LIVE_CHANNEL_TAB_PARAMS;
    }

    @Override
    public String getId(final String url) {
        return KIOSK_ID;
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        final URL urlObj;
        try {
            urlObj = Utils.stringToURL(url);
        } catch (final MalformedURLException e) {
            return false;
        }

        return Utils.isHTTP(urlObj) && (isYoutubeURL(urlObj) || isInvidiousURL(urlObj))
                && LIVE_CHANNEL_PATH.equals(urlObj.getPath())
                && LIVE_CHANNEL_TAB_PARAMS.equals(urlObj.getQuery());
    }
}
