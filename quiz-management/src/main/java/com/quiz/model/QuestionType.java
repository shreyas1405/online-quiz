package com.quiz.model;

public enum QuestionType {
    SINGLE_CHOICE("Single Choice"),
    MULTIPLE_CHOICE("Multiple Choice"),
    SHORT_ANSWER("Short Answer"),
    LONG_ANSWER("Long Answer / Essay");

    private final String displayName;

    QuestionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
