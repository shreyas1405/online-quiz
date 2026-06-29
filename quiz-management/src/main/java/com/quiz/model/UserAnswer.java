package com.quiz.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_answers")
@Getter @Setter @NoArgsConstructor
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "selected_option_id")
    private Option selectedOption;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_answer_options",
        joinColumns = @JoinColumn(name = "user_answer_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private Set<Option> selectedOptions = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String textAnswer;

    private Boolean isCorrect;

    private Integer marksObtained = 0;

    private Integer manualScore;

    @Column(columnDefinition = "TEXT")
    private String adminFeedback;
}
