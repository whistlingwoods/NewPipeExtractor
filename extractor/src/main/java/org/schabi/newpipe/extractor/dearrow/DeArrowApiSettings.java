package org.schabi.newpipe.extractor.dearrow;

public class DeArrowApiSettings {
    public static final String DEFAULT_API_URL = "https://sponsor.ajay.app";
    public static final String DEFAULT_THUMBNAIL_URL = "https://dearrow-thumb.ajay.app";
    public static final String DEFAULT_SERVICE = "YouTube";

    public String apiUrl = DEFAULT_API_URL;
    public String thumbnailUrl = DEFAULT_THUMBNAIL_URL;
    public String service = DEFAULT_SERVICE;
    public boolean useHashPrefix = true;
    public boolean fetchAll = false;
    public boolean returnUserId = false;
    public String localUserId = null;
    public String licenseKey = null;
    public String userAgent = "NewPipeExtractor/1.0";
}
