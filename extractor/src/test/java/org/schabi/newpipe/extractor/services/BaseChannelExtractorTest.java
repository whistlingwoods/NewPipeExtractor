package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BaseChannelExtractorTest extends BaseExtractorTest {
    void testAvatarUrl() throws Exception;
    void testBannerUrl() throws Exception;
    void testFeedUrl() throws Exception;
    void testSubscriberCount() throws Exception;
    void testDescription() throws Exception;
    void testTabs() throws Exception;
}
