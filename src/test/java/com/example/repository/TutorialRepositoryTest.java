package com.example.repository;

import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.projection.interfacebased.closed.TutorialClosed;
import com.example.util.TutorialTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TutorialRepositoryTest {

    @Autowired
    private TutorialRepository tutorialRepository;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tutorialRepository.deleteAll();
        tagRepository.deleteAll();
    }

    // ============================================================
    // findByPublished
    // ============================================================
    @Test
    @DisplayName("Buscar todos los tutoriales publicados")
    void shouldFindPublishedTutorials() {

        Tutorial publishedTutorial1 = TutorialTestDataFactory.createTutorial1();
        Tutorial publishedTutorial2 = TutorialTestDataFactory.createTutorial2();
        Tutorial unpublishedTutorial = TutorialTestDataFactory.createTutorial3();

        tutorialRepository.saveAll(List.of(
                publishedTutorial1,
                publishedTutorial2,
                unpublishedTutorial
        ));

        List<Tutorial> result = tutorialRepository.findByPublished(true);

        assertThat(result)
                .hasSize(2)
                .extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder("Spring Boot desde cero", "Spring Security con JWT");
    }

    @Test
    @DisplayName("Buscar todos los tutoriales no publicados")
    void shouldFindUnpublishedTutorials() {

        Tutorial publishedTutorial = TutorialTestDataFactory.createTutorial1();
        Tutorial unpublishedTutorial = TutorialTestDataFactory.createTutorial3();

        tutorialRepository.saveAll(List.of(
                publishedTutorial,
                unpublishedTutorial
        ));

        List<Tutorial> result = tutorialRepository.findByPublished(false);

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Tutorial::getTitle)
                .isEqualTo("JPA y Hibernate");
    }

    // ============================================================
    // findByTitleContaining
    // ============================================================
    @Test
    @DisplayName("Buscar todos los tutoriales en cuyo titulo figure Spring")
    void shouldFindTutorialsContainingTitle() {

        tutorialRepository.saveAll(List.of(
                TutorialTestDataFactory.createTutorial1(),
                TutorialTestDataFactory.createTutorial2(),
                TutorialTestDataFactory.createTutorial3()
        ));

        List<Tutorial> result = tutorialRepository.findByTitleContaining("Spring");

        assertThat(result)
                .hasSize(2)
                .extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder("Spring Boot desde cero", "Spring Security con JWT");
    }

    @Test
    @DisplayName("Tutoriaes cuando no existe a paabra en e título")
    void shouldReturnEmptyListWhenTitleDoesNotExist() {

        tutorialRepository.save(TutorialTestDataFactory.createTutorial1());

        List<Tutorial> result = tutorialRepository.findByTitleContaining("Python");

        assertThat(result).isEmpty();
    }

    // ============================================================
    // findAll
    // ============================================================

    @Test
    @DisplayName("Buscar todos los tutoriales")
    void shouldFindAllTutorials() {

        tutorialRepository.saveAll(List.of(
                TutorialTestDataFactory.createTutorial1(),
                TutorialTestDataFactory.createTutorial2(),
                TutorialTestDataFactory.createTutorial3()
        ));

        List<Tutorial> result = tutorialRepository.findAll();

        assertThat(result)
                .hasSize(3)
                .extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder(
                        "Spring Boot desde cero",
                        "Spring Security con JWT",
                        "JPA y Hibernate"
                );
    }

    // ============================================================
    // findTutorialsByTagsId
    // ============================================================
    @Test
    @DisplayName("Buscar todos los tutoriales por tadId")
    void shouldFindTutorialsByTagId() {

        Tag javaTag = TutorialTestDataFactory.createJavaTag();
        Tag springTag = TutorialTestDataFactory.createSpringTag();

        tagRepository.saveAll(List.of(javaTag, springTag));

        Tutorial tutorial1 = TutorialTestDataFactory.createTutorial1();
        Tutorial tutorial2 = TutorialTestDataFactory.createTutorial2();

        tutorial1.addTag(javaTag);
        tutorial2.addTag(springTag);

        tutorialRepository.saveAll(List.of(
                tutorial1,
                tutorial2
        ));

        List<Tutorial> result = tutorialRepository.findTutorialsByTagsId(javaTag.getId());

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Tutorial::getTitle)
                .isEqualTo("Spring Boot desde cero");
    }

    @Test
    @DisplayName("Devoollver listado de tags sin tutorial")
    void shouldReturnEmptyListWhenTagDoesNotHaveTutorials() {

        Tag tag = TutorialTestDataFactory.createJavaTag();

        tagRepository.save(tag);

        List<Tutorial> result = tutorialRepository.findTutorialsByTagsId(tag.getId());

        assertThat(result).isEmpty();
    }

    // ============================================================
    // findBy - CLOSED PROJECTION
    // ============================================================
    @Test
    @DisplayName(" Quiero los tutoriales, pero solo me interesan los campos que declara TutorialClosed")
    void shouldFindTutorialsUsingClosedProjection() {

        tutorialRepository.saveAll(List.of(
                TutorialTestDataFactory.createTutorial1(),
                TutorialTestDataFactory.createTutorial2()
        ));

        List<TutorialClosed> result = tutorialRepository.findBy();

        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(TutorialClosed::getTitle)
                .containsExactlyInAnyOrder(
                        "Spring Boot desde cero",
                        "Spring Security con JWT"
                );

        assertThat(result)
                .extracting(TutorialClosed::getDescription)
                .containsExactlyInAnyOrder(
                        "Introducción a Spring Boot",
                        "Autenticación y autorización con JWT"
                );
    }

    // ============================================================
    // EntityGraph + tags
    // ============================================================
    @Test
    @DisplayName("Cargar etiquetas al buscar tutoriales por fecha de publicacion")
    void shouldLoadTagsWhenFindingTutorialsByPublished() {

        Tag javaTag = TutorialTestDataFactory.createJavaTag();
        Tag springTag = TutorialTestDataFactory.createSpringTag();

        tagRepository.saveAll(List.of(
                javaTag,
                springTag
        ));

        Tutorial tutorial = TutorialTestDataFactory.createTutorial1();

        tutorial.addTag(javaTag);
        tutorial.addTag(springTag);

        tutorialRepository.save(tutorial);

        List<Tutorial> result = tutorialRepository.findByPublished(true);

        assertThat(result)
                .hasSize(1);

        Tutorial found = result.get(0);

        assertThat(found.getTags())
                .hasSize(2)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring");
    }
}
