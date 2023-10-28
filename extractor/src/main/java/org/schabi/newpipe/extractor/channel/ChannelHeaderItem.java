package org.schabi.newpipe.extractor.channel;

public final class ChannelHeaderItem {
    private final String continuationToken;
    private final String headerText;
    private final boolean isSelected;

    public ChannelHeaderItem(String headerText, String continuationToken, boolean isSelected) {
        this.headerText = headerText;
        this.continuationToken = continuationToken;
        this.isSelected = isSelected;
    }

    public String getHeaderText() {
        return this.headerText;
    }

    public String getContinuationToken() {
        return this.continuationToken;
    }

    public boolean isSelected() {
        return this.isSelected;
    }
}