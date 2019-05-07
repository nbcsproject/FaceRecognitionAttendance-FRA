package com.android.fra;

public class About {
    private String titleText;
    private String authorText;
    private String descriptionText;

    public About(String titleText, String authorText, String descriptionText) {
        this.titleText = titleText;
        this.authorText = authorText;
        this.descriptionText = descriptionText;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getAuthorText() {
        return authorText;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

}
