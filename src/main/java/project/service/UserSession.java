package project.service;

import project.model.Reminders;

public class UserSession {
    private String currentStep;
    private Reminders reminder;

    public UserSession() {
        this.currentStep = "";
        this.reminder = null;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public Reminders getReminder() {
        return reminder;
    }

    public void setReminder(Reminders reminder) {
        this.reminder = reminder;
    }
}
