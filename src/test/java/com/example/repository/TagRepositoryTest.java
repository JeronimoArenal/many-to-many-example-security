package com.example.repository;

import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.util.TutorialTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TutorialRepository tutorialRepository;

    @Test
    @DisplayName("Buscar etiquetas por tutorial Id")
    void shouldFindTagsByTutorialId() {

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
        List<Tag> result = tagRepository.findTagsByTutorialsId(tutorial.getId());

        assertThat(result)
                .hasSize(2)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("Devolver únicamente las etiquetas (tags) que pertenecen al tutorial")
    void shouldReturnEmptyListWhenTutorialHasNoTags() {

        Tutorial tutorial = TutorialTestDataFactory.createTutorial1();

        tutorialRepository.save(tutorial);
        List<Tag> result = tagRepository.findTagsByTutorialsId(tutorial.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Buscar las etiquetas de un tutorial que ni siquiera existe en la base de datos.")
    void shouldReturnEmptyListWhenTutorialDoesNotExist() {

        List<Tag> result =
                tagRepository.findTagsByTutorialsId(999L);

        assertThat(result).isEmpty();
    }
}