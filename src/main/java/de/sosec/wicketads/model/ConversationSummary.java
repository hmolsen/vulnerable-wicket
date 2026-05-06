package de.sosec.wicketads.model;

import java.io.Serializable;

public class ConversationSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private int otherUserId;
    private String otherUsername;
    private int adId;
    private String adTitle;

    public ConversationSummary(int otherUserId, String otherUsername, int adId, String adTitle) {
        this.otherUserId = otherUserId;
        this.otherUsername = otherUsername;
        this.adId = adId;
        this.adTitle = adTitle;
    }

    public int getOtherUserId() { return otherUserId; }
    public String getOtherUsername() { return otherUsername; }
    public int getAdId() { return adId; }
    public String getAdTitle() { return adTitle; }
}
