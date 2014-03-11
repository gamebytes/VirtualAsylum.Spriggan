package org.virtualAsylum.spriggan;

/**
 * Created by Morgan on 11/03/14.
 */
public enum SprigganAddonState {
    IDLE("Idle", true),
    UPDATE_CHECK("Checking for Updates", false),
    DOWNLOADING("Downloading",  false),
    ERROR("Error", true),
    UPDATING("Updating", false),
    INSTALLING("Installing", false);

    public final String text;
    public final Boolean isIdle;

    SprigganAddonState(String text, boolean isIdle) {
        this.text = text;
        this.isIdle = isIdle;
    }
}
