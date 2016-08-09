package com.sloy.sevibus.resources;

import com.sloy.sevibus.BuildConfig;

public class FeatureToggle {

    private static final boolean DEFAULT_ENABLED = BuildConfig.DEBUG;

    private final RemoteConfiguration remoteConfiguration;

    public FeatureToggle(RemoteConfiguration remoteConfiguration) {
        this.remoteConfiguration = remoteConfiguration;
    }

    public boolean isLoginEnabled() {
        return remoteConfiguration.getBoolean("feature_login_enabled", DEFAULT_ENABLED);
    }

    public boolean isAwarenessBonobusEnabled() {
        return remoteConfiguration.getBoolean("feature_awareness_bonobus_enabled", DEFAULT_ENABLED);
    }
}
