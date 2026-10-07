package com.example.util;

import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;

import java.util.ArrayList;
import java.util.List;

public final class TutorialTestDataFactory {

    private TutorialTestDataFactory() {
        // Utility class
    }

    // ============================================================
    // TAGS
    // ============================================================

    public static Tag createJavaTag() {
        return Tag.builder()
                .name("Java")
                .build();
    }

    public static Tag createSpringTag() {
        return Tag.builder()
                .name("Spring")
                .build();
    }

    public static Tag createJpaTag() {
        return Tag.builder()
                .name("JPA")
                .build();
    }

    // ============================================================
    // TUTORIALS
    // ============================================================

    public static Tutorial createTutorial1() {
        return Tutorial.builder()
                .title("Spring Boot desde cero")
                .description("Introducción a Spring Boot")
                .published(true)
                .build();
    }

    public static Tutorial createTutorial2() {
        return Tutorial.builder()
                .title("Spring Security con JWT")
                .description("Autenticación y autorización con JWT")
                .published(true)
                .build();
    }

    public static Tutorial createTutorial3() {
        return Tutorial.builder()
                .title("JPA y Hibernate")
                .description("Persistencia de datos con JPA")
                .published(false)
                .build();
    }

    // ============================================================
    // RELATIONSHIPS
    // ============================================================

    public static Tutorial createTutorial1WithTags() {
        Tutorial tutorial = createTutorial1();

        tutorial.addTag(createJavaTag());
        tutorial.addTag(createSpringTag());

        return tutorial;
    }

    public static Tutorial createTutorial2WithTags() {
        Tutorial tutorial = createTutorial2();

        tutorial.addTag(createSpringTag());
        tutorial.addTag(createJpaTag());

        return tutorial;
    }

    public static Tutorial createTutorial3WithTags() {
        Tutorial tutorial = createTutorial3();

        tutorial.addTag(createJpaTag());

        return tutorial;
    }

    // ============================================================
    // LIST
    // ============================================================

    public static List<Tutorial> createTutorialList() {
        List<Tutorial> tutorials = new ArrayList<>();

        tutorials.add(createTutorial1());
        tutorials.add(createTutorial2());
        tutorials.add(createTutorial3());

        return tutorials;
    }
}
