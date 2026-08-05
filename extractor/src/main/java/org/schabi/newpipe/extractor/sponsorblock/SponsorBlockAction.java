package org.schabi.newpipe.extractor.sponsorblock;

public enum SponsorBlockAction {
    SKIP("skip"),
    POI("poi"),
    MUTE("mute"),
    FULL("full"),
    CHAPTER("chapter");

    private final String apiName;

    SponsorBlockAction(final String apiName) {
        this.apiName = apiName;
    }

    public static SponsorBlockAction fromApiName(final String apiName) {
        switch (apiName) {
            case "skip":
                return SponsorBlockAction.SKIP;
            case "poi":
                return SponsorBlockAction.POI;
            case "mute":
                return SponsorBlockAction.MUTE;
            case "full":
                return SponsorBlockAction.FULL;
            case "chapter":
                return SponsorBlockAction.CHAPTER;
            default:
                throw new IllegalArgumentException("Invalid API name");
        }
    }

    public String getApiName() {
        return apiName;
    }
}
