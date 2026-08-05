package org.schabi.newpipe.extractor.sponsorblock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SponsorBlockTest {

    @Test
    void testCategories() {
        Assertions.assertEquals(SponsorBlockCategory.HOOK,
                SponsorBlockCategory.fromApiName("hook"));
        Assertions.assertEquals(SponsorBlockCategory.EXCLUSIVE_ACCESS,
                SponsorBlockCategory.fromApiName("exclusive_access"));
        Assertions.assertEquals(SponsorBlockCategory.CHAPTER,
                SponsorBlockCategory.fromApiName("chapter"));
        Assertions.assertEquals("hook", SponsorBlockCategory.HOOK.getApiName());
    }

    @Test
    void testActions() {
        Assertions.assertEquals(SponsorBlockAction.MUTE,
                SponsorBlockAction.fromApiName("mute"));
        Assertions.assertEquals(SponsorBlockAction.FULL,
                SponsorBlockAction.fromApiName("full"));
        Assertions.assertEquals(SponsorBlockAction.CHAPTER,
                SponsorBlockAction.fromApiName("chapter"));
        Assertions.assertEquals("mute", SponsorBlockAction.MUTE.getApiName());
    }

    @Test
    void testSegmentMetadata() {
        final SponsorBlockSegment segment = new SponsorBlockSegment(
                "uuid-1234", 1000, 5000,
                SponsorBlockCategory.SPONSOR, SponsorBlockAction.SKIP)
                .setMetadata(10, true, 600.0, "Sponsor section");

        Assertions.assertEquals("uuid-1234", segment.uuid);
        Assertions.assertEquals(10, segment.votes);
        Assertions.assertTrue(segment.locked);
        Assertions.assertEquals(600.0, segment.videoDuration);
        Assertions.assertEquals("Sponsor section", segment.description);
    }

    @Test
    void testApiSettings() {
        final SponsorBlockApiSettings settings = new SponsorBlockApiSettings();
        settings.includeHookCategory = true;
        settings.includeExclusiveAccessCategory = true;
        settings.includeChapterCategory = true;
        settings.localUserId = "a".repeat(32);

        Assertions.assertTrue(settings.includeHookCategory);
        Assertions.assertEquals(32, settings.localUserId.length());
    }
}
