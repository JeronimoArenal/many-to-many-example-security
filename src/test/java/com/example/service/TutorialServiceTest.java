package com.example.service;

import com.example.domain.dto.response.TutorialDTO;
import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.domain.mapper.TutorialMapper;
import com.example.exception.ResourceNotFoundException;
import com.example.projection.interfacebased.closed.TutorialClosed;
import com.example.repository.TagRepository;
import com.example.repository.TutorialRepository;
import com.example.util.TutorialTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorialServiceTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TutorialMapper tutorialMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TutorialService tutorialService;

    private Tutorial tutorial1;
    private Tutorial tutorial2;

    private Tag javaTag;
    private Tag springTag;

    @BeforeEach
    void setUp() {
        tutorial1 = TutorialTestDataFactory.createTutorial1();
        tutorial2 = TutorialTestDataFactory.createTutorial2();

        javaTag = TutorialTestDataFactory.createJavaTag();
        springTag = TutorialTestDataFactory.createSpringTag();

        tutorial1.addTag(javaTag);
        tutorial2.addTag(springTag);
    }

    // ============================================================
    // getTutorials
    // ============================================================

    @Test
    @DisplayName("Debe devolver todos los tutoriales cuando el título es null")
    void shouldGetAllTutorialsWhenTitleIsNull() {

        List<Tutorial> expected = List.of(tutorial1, tutorial2);

        when(tutorialRepository.findAll()).thenReturn(expected);

        List<Tutorial> result = tutorialService.getTutorials(null);

        assertThat(result)
                .containsExactlyElementsOf(expected);

        verify(tutorialRepository).findAll();
        verify(tutorialRepository, never()).findByTitleContaining(any());
    }

    @Test
    @DisplayName("Debe devolver todos los tutoriales cuando el título está vacío")
    void shouldGetAllTutorialsWhenTitleIsBlank() {

        List<Tutorial> expected = List.of(tutorial1, tutorial2);

        when(tutorialRepository.findAll()).thenReturn(expected);

        List<Tutorial> result = tutorialService.getTutorials("   ");

        assertThat(result)
                .containsExactlyElementsOf(expected);

        verify(tutorialRepository).findAll();
        verify(tutorialRepository, never()).findByTitleContaining(any());
    }

    @Test
    @DisplayName("Debe buscar tutoriales por título")
    void shouldFindTutorialsByTitle() {

        when(tutorialRepository.findByTitleContaining("Spring"))
                .thenReturn(List.of(tutorial1, tutorial2));

        List<Tutorial> result = tutorialService.getTutorials("Spring");

        assertThat(result)
                .containsExactly(tutorial1, tutorial2);

        verify(tutorialRepository).findByTitleContaining("Spring");
        verify(tutorialRepository, never()).findAll();
    }

    // ============================================================
    // findById
    // ============================================================

    @Test
    @DisplayName("Debe encontrar un tutorial por ID")
    void shouldFindTutorialById() {

        Long tutorialId = 1L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        Tutorial result = tutorialService.findById(tutorialId);

        assertThat(result)
                .isSameAs(tutorial1);

        verify(tutorialRepository).findById(tutorialId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tutorial no existe")
    void shouldThrowExceptionWhenTutorialDoesNotExist() {

        Long tutorialId = 99L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tutorialService.findById(tutorialId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tutorial no encontrado con id: " + tutorialId);

        verify(tutorialRepository).findById(tutorialId);
    }

    // ============================================================
    // getPublishedTutorials
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tutoriales publicados")
    void shouldGetPublishedTutorials() {

        when(tutorialRepository.findByPublished(true))
                .thenReturn(List.of(tutorial1));

        List<Tutorial> result = tutorialService.getPublishedTutorials();

        assertThat(result).containsExactly(tutorial1);
        verify(tutorialRepository).findByPublished(true);
    }

    // ============================================================
    // getTutorialsByTag
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tutoriales asociados a un tag")
    void shouldGetTutorialsByTag() {

        Long tagId = 1L;

        when(tutorialRepository.findTutorialsByTagsId(tagId))
                .thenReturn(List.of(tutorial1));

        List<Tutorial> result = tutorialService.getTutorialsByTag(tagId);

        assertThat(result).containsExactly(tutorial1);
        verify(tutorialRepository).findTutorialsByTagsId(tagId);
    }

    // ============================================================
    // getAllTagsByTutorialId
    // ============================================================

    @Test
    @DisplayName("Debe devolver todos los tags de un tutorial")
    void shouldGetAllTagsByTutorialId() {

        Long tutorialId = 1L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        Set<Tag> result = tutorialService.getAllTagsByTutorialId(tutorialId);

        assertThat(result).containsExactly(javaTag);
        verify(tutorialRepository).findById(tutorialId);
    }

    @Test
    @DisplayName("Debe lanzar excepción al buscar tags de un tutorial inexistente")
    void shouldThrowExceptionWhenGettingTagsOfNonExistingTutorial() {

        Long tutorialId = 99L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tutorialService.getAllTagsByTutorialId(tutorialId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tutorial no encontrado con id: " + tutorialId);
    }

    // ============================================================
    // createTutorial
    // ============================================================

    @Test
    @DisplayName("Debe crear un tutorial")
    void shouldCreateTutorial() {

        Tutorial tutorial = TutorialTestDataFactory.createTutorial1();

        when(tutorialRepository.save(tutorial))
                .thenReturn(tutorial);

        Tutorial result = tutorialService.createTutorial(tutorial);

        assertThat(result).isSameAs(tutorial);
        verify(tutorialRepository).save(tutorial);
    }

    @Test
    @DisplayName("Debe inicializar los tags cuando son null al crear un tutorial")
    void shouldInitializeTagsWhenCreatingTutorialWithNullTags() {

        Tutorial tutorial = TutorialTestDataFactory.createTutorial1();
        tutorial.setTags(null);

        when(tutorialRepository.save(tutorial))
                .thenReturn(tutorial);

        Tutorial result = tutorialService.createTutorial(tutorial);

        assertThat(result.getTags())
                .isNotNull()
                .isEmpty();
        verify(tutorialRepository).save(tutorial);
    }

    // ============================================================
    // updateTutorial
    // ============================================================

    @Test
    @DisplayName("Debe actualizar completamente un tutorial")
    void shouldUpdateTutorial() {

        Long tutorialId = 1L;

        Tutorial newData = Tutorial.builder()
                .title("Nuevo título")
                .description("Nueva descripción")
                .published(false)
                .tags(new HashSet<>())
                .build();

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        when(tutorialRepository.save(tutorial1))
                .thenReturn(tutorial1);

        Tutorial result = tutorialService.updateTutorial(tutorialId, newData);

        assertThat(result).isSameAs(tutorial1);
        assertThat(tutorial1.getTitle()).isEqualTo("Nuevo título");
        assertThat(tutorial1.getDescription()).isEqualTo("Nueva descripción");
        assertThat(tutorial1.isPublished()).isFalse();
        assertThat(tutorial1.getTags()).isEmpty();

        verify(tutorialRepository).findById(tutorialId);
        verify(tutorialRepository).save(tutorial1);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar un tutorial inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistingTutorial() {

        Long tutorialId = 99L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.empty());

        Tutorial newData = TutorialTestDataFactory.createTutorial1();

        assertThatThrownBy(() ->
                tutorialService.updateTutorial(tutorialId, newData)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tutorial no encontrado con id: " + tutorialId);

        verify(tutorialRepository, never()).save(any());
    }

    // ============================================================
    // patchTutorial
    // ============================================================

    @Test
    @DisplayName("Debe aplicar parcialmente los cambios de un tutorial")
    void shouldPatchTutorial() {

        Long tutorialId = 1L;

        Map<String, Object> updates = Map.of("title", "Título actualizado");

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        when(tutorialRepository.save(tutorial1))
                .thenReturn(tutorial1);

        Tutorial result = tutorialService.patchTutorial(tutorialId, updates);

        assertThat(result).isSameAs(tutorial1);

        verify(objectMapper).updateValue(tutorial1, updates);
        verify(tutorialRepository).save(tutorial1);
    }

    @Test
    @DisplayName("Debe rechazar la modificación del ID mediante PATCH")
    void shouldRejectIdModificationInPatch() {

        Long tutorialId = 1L;

        Map<String, Object> updates = Map.of("id", 99L);

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        assertThatThrownBy(() -> tutorialService.patchTutorial(tutorialId, updates)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se permite modificar el identificador (id).");

        verify(objectMapper, never()).updateValue(any(), any());
        verify(tutorialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe rechazar la modificación de tags mediante PATCH")
    void shouldRejectTagsModificationInPatch() {

        Long tutorialId = 1L;

        Map<String, Object> updates = Map.of("tags", List.of());

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        assertThatThrownBy(() -> tutorialService.patchTutorial(tutorialId, updates)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Para gestionar etiquetas use los endpoints específicos de asociación."
                );

        verify(objectMapper, never()).updateValue(any(), any());
        verify(tutorialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando falla el procesamiento del PATCH")
    void shouldThrowExceptionWhenPatchProcessingFails() throws Exception {

        Long tutorialId = 1L;

        Map<String, Object> updates = Map.of("title", "Nuevo título");

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        doThrow(new RuntimeException("Jackson error"))
                .when(objectMapper)
                .updateValue(tutorial1, updates);

        assertThatThrownBy(() -> tutorialService.patchTutorial(tutorialId, updates)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Error al procesar la actualización parcial del tutorial"
                )
                .hasCauseInstanceOf(RuntimeException.class);

        verify(tutorialRepository, never()).save(any());
    }

    // ============================================================
    // addTagToTutorial
    // ============================================================

    @Test
    @DisplayName("Debe añadir un tag a un tutorial")
    void shouldAddTagToTutorial() {

        Long tutorialId = 1L;
        Long tagId = 10L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        when(tagRepository.findById(tagId))
                .thenReturn(Optional.of(springTag));

        when(tutorialRepository.save(tutorial1))
                .thenReturn(tutorial1);

        Tutorial result = tutorialService.addTagToTutorial(tutorialId, tagId);

        assertThat(result.getTags()).contains(javaTag, springTag);

        verify(tutorialRepository).findById(tutorialId);
        verify(tagRepository).findById(tagId);
        verify(tutorialRepository).save(tutorial1);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el tutorial no existe al añadir tag")
    void shouldThrowExceptionWhenAddingTagToNonExistingTutorial() {

        Long tutorialId = 99L;
        Long tagId = 10L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tutorialService.addTagToTutorial(tutorialId, tagId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tutorial no encontrado con id: " + tutorialId);

        verify(tagRepository, never()).findById(any());
        verify(tutorialRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el tag no existe")
    void shouldThrowExceptionWhenAddingNonExistingTag() {

        Long tutorialId = 1L;
        Long tagId = 99L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        when(tagRepository.findById(tagId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tutorialService.addTagToTutorial(tutorialId, tagId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Etiqueta no encontrada con id: " + tagId);

        verify(tutorialRepository, never()).save(any());
    }

    // ============================================================
    // createAndAddTagToTutorial
    // ============================================================

    @Test
    @DisplayName("Debe crear y asociar un nuevo tag al tutorial")
    void shouldCreateAndAddTagToTutorial() {

        Long tutorialId = 1L;

        Tag newTag = Tag.builder()
                .name("Hibernate")
                .build();

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        when(tutorialRepository.save(tutorial1))
                .thenReturn(tutorial1);

        Tutorial result = tutorialService.createAndAddTagToTutorial(tutorialId, newTag);

        assertThat(result.getTags()).contains(javaTag, newTag);

        verify(tutorialRepository).findById(tutorialId);
        verify(tutorialRepository).save(tutorial1);
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear tag para tutorial inexistente")
    void shouldThrowExceptionWhenCreatingTagForNonExistingTutorial() {

        Long tutorialId = 99L;

        Tag newTag = Tag.builder()
                .name("Hibernate")
                .build();

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tutorialService.createAndAddTagToTutorial(tutorialId, newTag)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tutorial no encontrado con id: " + tutorialId);

        verify(tutorialRepository, never()).save(any());
    }

    // ============================================================
    // getTutorialsByTagId
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tutoriales de un tag existente")
    void shouldGetTutorialsByTagId() {

        Long tagId = 1L;

        when(tagRepository.existsById(tagId))
                .thenReturn(true);

        when(tutorialRepository.findTutorialsByTagsId(tagId))
                .thenReturn(List.of(tutorial1));

        List<Tutorial> result = tutorialService.getTutorialsByTagId(tagId);

        assertThat(result).containsExactly(tutorial1);

        verify(tagRepository).existsById(tagId);
        verify(tutorialRepository).findTutorialsByTagsId(tagId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el tag no existe")
    void shouldThrowExceptionWhenTagDoesNotExist() {

        Long tagId = 99L;

        when(tagRepository.existsById(tagId))
                .thenReturn(false);

        assertThatThrownBy(() -> tutorialService.getTutorialsByTagId(tagId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Not found Tag with id = " + tagId);

        verify(tutorialRepository, never()).findTutorialsByTagsId(any());
    }

    // ============================================================
    // removeTagFromTutorial
    // ============================================================

    @Test
    @DisplayName("Debe eliminar un tag de un tutorial")
    void shouldRemoveTagFromTutorial() {

        Long tutorialId = 1L;
        Long tagId = javaTag.getId();

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.of(tutorial1));

        when(tutorialRepository.save(tutorial1))
                .thenReturn(tutorial1);

        Tutorial result = tutorialService.removeTagFromTutorial(tutorialId, tagId);

        assertThat(result.getTags()).doesNotContain(javaTag);

        verify(tutorialRepository).findById(tutorialId);
        verify(tutorialRepository).save(tutorial1);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar tag de tutorial inexistente")
    void shouldThrowExceptionWhenRemovingTagFromNonExistingTutorial() {

        Long tutorialId = 99L;
        Long tagId = 1L;

        when(tutorialRepository.findById(tutorialId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> tutorialService.removeTagFromTutorial(tutorialId, tagId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tutorial no encontrado con id: " + tutorialId);

        verify(tutorialRepository, never()).save(any());
    }

    // ============================================================
    // getTutorialsDTO
    // ============================================================

    @Test
    @DisplayName("Debe convertir tutoriales a DTO")
    void shouldGetTutorialsAsDto() {

        when(tutorialRepository.findAll())
                .thenReturn(List.of(tutorial1, tutorial2));

        List<TutorialDTO> result = tutorialService.getTutorialsDTO(null);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TutorialDTO::title)
                .containsExactly(
                        tutorial1.getTitle(),
                        tutorial2.getTitle()
                );
        assertThat(result)
                .extracting(TutorialDTO::description)
                .containsExactly(
                        tutorial1.getDescription(),
                        tutorial2.getDescription()
                );

        verify(tutorialRepository).findAll();
    }

    @Test
    @DisplayName("Debe convertir tutoriales filtrados a DTO")
    void shouldGetFilteredTutorialsAsDto() {

        when(tutorialRepository.findByTitleContaining("Spring"))
                .thenReturn(List.of(tutorial1, tutorial2));

        List<TutorialDTO> result = tutorialService.getTutorialsDTO("Spring");

        assertThat(result).hasSize(2);

        verify(tutorialRepository).findByTitleContaining("Spring");
        verify(tutorialRepository, never()).findAll();
    }

    // ============================================================
    // getTutorialsDTOMap
    // ============================================================

    @Test
    @DisplayName("Debe convertir tutoriales a DTO usando el mapper")
    void shouldGetTutorialsAsDtoUsingMapper() {

        TutorialDTO dto1 = mock(TutorialDTO.class);
        TutorialDTO dto2 = mock(TutorialDTO.class);

        when(tutorialRepository.findAll())
                .thenReturn(List.of(tutorial1, tutorial2));

        when(tutorialMapper.toDto(tutorial1))
                .thenReturn(dto1);

        when(tutorialMapper.toDto(tutorial2))
                .thenReturn(dto2);

        List<TutorialDTO> result = tutorialService.getTutorialsDTOMap(null);

        assertThat(result).containsExactly(dto1, dto2);

        verify(tutorialMapper).toDto(tutorial1);
        verify(tutorialMapper).toDto(tutorial2);
    }

    @Test
    @DisplayName("Debe buscar y mapear tutoriales filtrados usando el mapper")
    void shouldGetFilteredTutorialsAsDtoUsingMapper() {

        TutorialDTO dto1 = mock(TutorialDTO.class);

        when(tutorialRepository.findByTitleContaining("Spring"))
                .thenReturn(List.of(tutorial1));

        when(tutorialMapper.toDto(tutorial1))
                .thenReturn(dto1);

        List<TutorialDTO> result = tutorialService.getTutorialsDTOMap("Spring");

        assertThat(result).containsExactly(dto1);

        verify(tutorialRepository).findByTitleContaining("Spring");
        verify(tutorialMapper).toDto(tutorial1);
    }

    // ============================================================
    // findBy
    // ============================================================

    @Test
    @DisplayName("Debe devolver tutoriales usando closed projection")
    void shouldFindTutorialsUsingClosedProjection() {

        TutorialClosed projection1 = mock(TutorialClosed.class);
        TutorialClosed projection2 = mock(TutorialClosed.class);

        when(tutorialRepository.findBy())
                .thenReturn(List.of(projection1, projection2));

        List<TutorialClosed> result = tutorialService.findBy();

        assertThat(result).containsExactly(projection1, projection2);

        verify(tutorialRepository).findBy();
    }
}
