package org.schabi.newpipe.extractor.returnyoutubedislike;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReturnYouTubeDislikeTest implements InitNewPipeTest {

    @Test
    void testInfoModelWithLargeNumbers() {
        final long largeLikes = 3_500_000_000L;
        final long largeDislikes = 1_200_000_000L;
        final long largeViews = 10_000_000_000L;

        final ReturnYouTubeDislikeInfo info = new ReturnYouTubeDislikeInfo(
                largeLikes, largeDislikes, 4.85, largeViews, false)
                .setExtraInfo("kxOuG8jMIgI", "2022-04-09T21:44:20.5103Z", 50000L, 12000L);

        Assertions.assertEquals(largeLikes, info.likes);
        Assertions.assertEquals(largeDislikes, info.dislikes);
        Assertions.assertEquals(4.85, info.rating, 0.001);
        Assertions.assertEquals(largeViews, info.viewCount);
        Assertions.assertFalse(info.deleted);
        Assertions.assertEquals("kxOuG8jMIgI", info.id);
        Assertions.assertEquals("2022-04-09T21:44:20.5103Z", info.dateCreated);
        Assertions.assertEquals(50000L, info.rawLikes);
        Assertions.assertEquals(12000L, info.rawDislikes);
    }

    @Test
    void testHelperNullAndInvalidInputs() {
        final ReturnYouTubeDislikeApiSettings settings = new ReturnYouTubeDislikeApiSettings();
        settings.apiUrl = "https://returnyoutubedislikeapi.com";

        Assertions.assertNull(ReturnYouTubeDislikeExtractorHelper.getInfo(
                (StreamInfo) null, settings));
        Assertions.assertNull(ReturnYouTubeDislikeExtractorHelper.getInfo(
                "", 0L, settings));

        final List<ReturnYouTubeDislikeInfo> results =
                ReturnYouTubeDislikeExtractorHelper.getInfo(Collections.emptyList(), settings);
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void testLiveSingleVideoFetching() {
        final ReturnYouTubeDislikeApiSettings settings = new ReturnYouTubeDislikeApiSettings();
        settings.apiUrl = "https://returnyoutubedislikeapi.com";

        final ReturnYouTubeDislikeInfo info = ReturnYouTubeDislikeExtractorHelper.getInfo(
                "kxOuG8jMIgI", 30000L, settings);
        Assertions.assertNotNull(info);
        Assertions.assertEquals("kxOuG8jMIgI", info.id);
        Assertions.assertTrue(info.likes > 0);
        Assertions.assertTrue(info.dislikes > 0);
        Assertions.assertTrue(info.viewCount > 0);
        Assertions.assertNotNull(info.dateCreated);
        Assertions.assertFalse(info.dateCreated.isEmpty());
    }

    @Test
    void testLiveBatchFetching() {
        final ReturnYouTubeDislikeApiSettings settings = new ReturnYouTubeDislikeApiSettings();
        settings.apiUrl = "https://returnyoutubedislikeapi.com";

        final List<String> videoIds = Arrays.asList("kxOuG8jMIgI", "dQw4w9WgXcQ");
        final List<ReturnYouTubeDislikeInfo> results =
                ReturnYouTubeDislikeExtractorHelper.getInfo(videoIds, settings);

        Assertions.assertNotNull(results);
        Assertions.assertEquals(2, results.size());

        for (final ReturnYouTubeDislikeInfo info : results) {
            Assertions.assertNotNull(info.id);
            Assertions.assertTrue(videoIds.contains(info.id));
            Assertions.assertTrue(info.likes > 0);
            Assertions.assertTrue(info.dislikes > 0);
            Assertions.assertTrue(info.viewCount > 0);
        }
    }

    @Test
    void testLiveCacheWarmup() {
        final ReturnYouTubeDislikeApiSettings settings = new ReturnYouTubeDislikeApiSettings();
        settings.apiUrl = "https://returnyoutubedislikeapi.com";

        final List<String> videoIds = Arrays.asList("kxOuG8jMIgI", "dQw4w9WgXcQ");
        Assertions.assertDoesNotThrow(() ->
                ReturnYouTubeDislikeExtractorHelper.sendVideoIdsForCacheWarmup(
                        videoIds, settings));
    }
}
