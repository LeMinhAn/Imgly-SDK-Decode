package com.camerafilter.plugins.analytics;

/**
 * Created by Le Minh An on 9/20/2016.
 */
public abstract class AbstractAnalyticsPlugin {

    /**
     * Is called on change the current Screen,
     * @param string screen name
     * {@inheritDoc}
     */
    public abstract void changeScreen(String string);

    /**
     * Is called on any Event.
     * @param category Event category
     * @param action Event action
     * {@inheritDoc}
     */
    public abstract void sendEvent(String category, String action);
    /**
     * Is called on any Event.
     * @param category Event category
     * @param action Event action
     * @param label Event description
     * {@inheritDoc}
     */
    public abstract void sendEvent(String category, String action, String label);

}

