package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.FileUtils.resolveTestResource;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class YoutubeParsingHelperTest
{

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/";

    @BeforeAll
    public static void setUp() throws IOException {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "youtubeParsingHelper"));
    }

    @Test
    public void testAreHardcodedClientVersionAndKeyValid() throws IOException, ExtractionException {
        assertTrue(YoutubeParsingHelper.areHardcodedClientVersionAndKeyValid(),
                "Hardcoded client version and key are not valid anymore");
    }

    @Test
    public void testAreHardcodedYoutubeMusicKeysValid() throws IOException, ExtractionException {
        assertTrue(YoutubeParsingHelper.isHardcodedYoutubeMusicKeyValid(),
                "Hardcoded YouTube Music keys are not valid anymore");
    }

    @Test
    public void testParseDurationString() throws ParsingException {
        assertEquals(1162567, YoutubeParsingHelper.parseDurationString("12:34:56:07"));
        assertEquals(4445767, YoutubeParsingHelper.parseDurationString("1,234:56:07"));
        assertEquals(754, YoutubeParsingHelper.parseDurationString("12:34 "));
    }

    @Test
    public void testConvertFromGoogleCacheUrl() {
        assertEquals("https://mohfw.gov.in/",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://webcache.googleusercontent.com/search?q=cache:https://mohfw.gov.in/"));
        assertEquals("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html",
                YoutubeParsingHelper.extractCachedUrlIfNeeded("https://www.infektionsschutz.de/coronavirus-sars-cov-2.html"));
    }

    @Test
    void testGetAttributedDescription() throws Exception {
        final InputStream fileIn = Files.newInputStream(resolveTestResource("attributed_description.json").toPath());
        final JsonObject attributedDesc = JsonParser.object().from(fileIn).getObject("attributedDescription");
        final String description = YoutubeParsingHelper.getAttributedDescription(attributedDesc);
        assertEquals("\uD83C\uDFA7Listen and download aespa's debut single \"Black Mamba\": <a href=\"https://smarturl.it/aespa_BlackMamba\">https://smarturl.it/aespa_BlackMamba</a><br>\uD83D\uDC0DThe Debut Stage <a href=\"https://www.youtube.com/watch?v=Ky5RT5oGg0w&amp;t=0\">aespa 에스파 'Black ...</a><br><br>\uD83C\uDF9F️ aespa Showcase SYNK in LA! Tickets now on sale: <a href=\"https://www.ticketmaster.com/event/0A005CCD9E871F6E\">https://www.ticketmaster.com/event/0A...</a><br><br>Subscribe to aespa Official YouTube Channel!<br><a href=\"https://www.youtube.com/aespa?sub_confirmation=1\">https://www.youtube.com/aespa?sub_con...</a><br><br>aespa official<br><a href=\"https://www.youtube.com/c/aespa\">aespa</a><br><a href=\"https://www.instagram.com/aespa_official\">https://www.instagram.com/aespa_official</a><br><a href=\"https://www.tiktok.com/@aespa_official\">https://www.tiktok.com/@aespa_official</a><br><a href=\"https://twitter.com/aespa_Official\">https://twitter.com/aespa_Official</a><br><a href=\"https://www.facebook.com/aespa.official\">https://www.facebook.com/aespa.official</a><br><a href=\"https://weibo.com/aespa\">https://weibo.com/aespa</a><br><br>#aespa #æspa #BlackMamba #블랙맘바 #에스파<br>aespa 에스파 'Black Mamba' MV ℗ SM Entertainment", description);
    }
}
