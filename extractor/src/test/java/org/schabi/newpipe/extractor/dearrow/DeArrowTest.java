package org.schabi.newpipe.extractor.dearrow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.Arrays;

public class DeArrowTest implements InitNewPipeTest {

    @Test
    void testModelCleaningAndUrls() {
        final DeArrowTitle title = new DeArrowTitle(
                ">Test >Title >Clean", false, 10.0, false, "uuid-1", "user-1");
        Assertions.assertEquals("Test Title Clean", title.getCleanTitle());

        final DeArrowApiSettings settings = new DeArrowApiSettings();
        final DeArrowThumbnail thumb = new DeArrowThumbnail(
                14.5, false, 5.0, true, "uuid-2", "user-2");
        final String url = thumb.getThumbnailUrl("dQw4w9WgXcQ", settings);
        Assertions.assertEquals(
                "https://dearrow-thumb.ajay.app/api/v1/getThumbnail?videoID=dQw4w9WgXcQ&time=14.5",
                url);
    }

    @Test
    void testQualityFiltering() {
        final DeArrowTitle badTitle = new DeArrowTitle(
                "Bad Title", false, -5.0, false, "uuid-1", "user-1");
        final DeArrowTitle goodTitle = new DeArrowTitle(
                "Good Title", false, 2.0, false, "uuid-2", "user-2");

        final DeArrowThumbnail badThumb = new DeArrowThumbnail(
                1.0, false, -2.0, false, "uuid-t1", "user-1");
        final DeArrowThumbnail lockedThumb = new DeArrowThumbnail(
                5.0, false, -1.0, true, "uuid-t2", "user-2");

        final DeArrowInfo info = new DeArrowInfo(
                Arrays.asList(badTitle, goodTitle),
                Arrays.asList(badThumb, lockedThumb),
                0.25, 200.0);

        Assertions.assertNotNull(info.getBestTitle());
        Assertions.assertEquals("Good Title", info.getBestTitle().title);

        Assertions.assertNotNull(info.getBestThumbnail());
        Assertions.assertEquals(5.0, info.getBestThumbnail().timestamp, 0.001);
    }

    @Test
    void testHelperNullAndInvalidInputs() {
        final DeArrowApiSettings settings = new DeArrowApiSettings();
        Assertions.assertNull(DeArrowExtractorHelper.getInfo(
                (StreamInfo) null, settings));
        Assertions.assertNull(DeArrowExtractorHelper.getInfo(
                "dQw4w9WgXcQ", null));
        Assertions.assertNull(DeArrowExtractorHelper.getInfo(
                "", settings));
    }

    @Test
    void testLiveHashPrefixFetching() {
        final DeArrowApiSettings settings = new DeArrowApiSettings();
        settings.useHashPrefix = true;
        final DeArrowInfo info = DeArrowExtractorHelper.getInfo("dQw4w9WgXcQ", settings);
        Assertions.assertNotNull(info, "Live DeArrow response should not be null for Rick Roll");
        Assertions.assertNotNull(info.titles);
        Assertions.assertNotNull(info.thumbnails);
    }

    @Test
    void testLiveDirectFetching() {
        final DeArrowApiSettings settings = new DeArrowApiSettings();
        settings.useHashPrefix = false;
        final DeArrowInfo info = DeArrowExtractorHelper.getInfo("dQw4w9WgXcQ", settings);
        Assertions.assertNotNull(info, "Direct DeArrow response should not be null");
        Assertions.assertNotNull(info.titles);
        Assertions.assertNotNull(info.thumbnails);
    }
    @Test
    void testLiveDirectFetchingCYXm3SVmlOM() {
        final DeArrowApiSettings settings = new DeArrowApiSettings();
        settings.useHashPrefix = false;
        final DeArrowInfo info = DeArrowExtractorHelper.getInfo("CYXm3SVmlOM", settings);
        Assertions.assertNotNull(info, "Direct DeArrow response should not be null for CYXm3SVmlOM");
        Assertions.assertNotNull(info.titles);
        Assertions.assertNotNull(info.thumbnails);
    }
}
