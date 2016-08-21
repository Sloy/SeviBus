package com.sloy.sevibus.ui.other;

public interface CardWizardManager {

    /**
     * The action to be performed when one of the cards wants to go to the next step.
     */
    void next();

    /**
     * @return A descriptive name of this CardWizardManager instance for tracking purposes.
     */
    String getDescription();
}
