package com.example.service;

import com.example.domain.entity.Tag;
import com.example.repository.TagRepository;
import com.example.util.TutorialTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag javaTag;
    private Tag springTag;
    private Tag jpaTag;

    @BeforeEach
    void setUp() {
        javaTag = TutorialTestDataFactory.createJavaTag();
        springTag = TutorialTestDataFactory.createSpringTag();
        jpaTag = TutorialTestDataFactory.createJpaTag();
    }

    // ============================================================
    // getAllTags
    // ============================================================

    @Test
    @DisplayName("Debe devolver todos los tags")
    void shouldGetAllTags() {

        List<Tag> expected = List.of(
                javaTag,
                springTag,
                jpaTag
        );

        when(tagRepository.findAll())
                .thenReturn(expected);

        List<Tag> result = tagService.getAllTags();

        assertThat(result).containsExactly(
                        javaTag,
                        springTag,
                        jpaTag
                );

        verify(tagRepository).findAll();
    }

    @Test
    @DisplayName("Debe devolver una lista vacía cuando no existen tags")
    void shouldReturnEmptyListWhenNoTagsExist() {

        when(tagRepository.findAll())
                .thenReturn(List.of());

        List<Tag> result = tagService.getAllTags();

        assertThat(result).isEmpty();

        verify(tagRepository).findAll();
    }

    // ============================================================
    // getTagById
    // ============================================================

    @Test
    @DisplayName("Debe encontrar un tag por ID")
    void shouldGetTagById() {

        Long tagId = 1L;

        when(tagRepository.findById(tagId))
                .thenReturn(Optional.of(javaTag));

        Optional<Tag> result = tagService.getTagById(tagId);

        assertThat(result)
                .isPresent()
                .contains(javaTag);

        verify(tagRepository).findById(tagId);
    }

    @Test
    @DisplayName("Debe devolver Optional vacío cuando el tag no existe")
    void shouldReturnEmptyOptionalWhenTagDoesNotExist() {

        Long tagId = 99L;

        when(tagRepository.findById(tagId))
                .thenReturn(Optional.empty());

        Optional<Tag> result = tagService.getTagById(tagId);

        assertThat(result).isEmpty();

        verify(tagRepository).findById(tagId);
    }

    // ============================================================
    // getTagsByTutorialId
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tags asociados a un tutorial")
    void shouldGetTagsByTutorialId() {

        Long tutorialId = 1L;

        when(tagRepository.findTagsByTutorialsId(tutorialId))
                .thenReturn(List.of(
                        javaTag,
                        springTag
                ));

        List<Tag> result = tagService.getTagsByTutorialId(tutorialId);

        assertThat(result).containsExactly(
                        javaTag,
                        springTag
                );

        verify(tagRepository).findTagsByTutorialsId(tutorialId);
    }

    @Test
    @DisplayName("Debe devolver una lista vacía cuando el tutorial no tiene tags")
    void shouldReturnEmptyListWhenTutorialHasNoTags() {

        Long tutorialId = 1L;

        when(tagRepository.findTagsByTutorialsId(tutorialId))
                .thenReturn(List.of());

        List<Tag> result = tagService.getTagsByTutorialId(tutorialId);

        assertThat(result).isEmpty();

        verify(tagRepository).findTagsByTutorialsId(tutorialId);
    }

    // ============================================================
    // createTag
    // ============================================================

    @Test
    @DisplayName("Debe crear un tag")
    void shouldCreateTag() {

        when(tagRepository.save(javaTag))
                .thenReturn(javaTag);

        Tag result = tagService.createTag(javaTag);

        assertThat(result).isSameAs(javaTag);

        verify(tagRepository).save(javaTag);
    }

    // ============================================================
    // deleteTag
    // ============================================================

    @Test
    @DisplayName("Debe eliminar un tag por ID")
    void shouldDeleteTag() {

        Long tagId = 1L;

        doNothing()
                .when(tagRepository)
                .deleteById(tagId);

        tagService.deleteTag(tagId);

        verify(tagRepository).deleteById(tagId);
    }
}
