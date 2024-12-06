package project.service;

import project.model.Reminders;
/*
Этот класс представляет собой сессию конкретного пользователя
и содержит данные о текущем состоянии взаимодействия с ботом.
 */
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

    public void setReminder(Reminders reminder) {
        this.reminder = reminder;
    }

    public Reminders getReminder() {
        return reminder;
    }
}
