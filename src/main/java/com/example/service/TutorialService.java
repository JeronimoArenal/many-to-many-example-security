package com.example.service;

import com.example.domain.dto.response.TutorialDTO;
import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.domain.mapper.TutorialMapper;
import com.example.exception.ResourceNotFoundException;
import com.example.projection.interfacebased.closed.TutorialClosed;
import com.example.repository.TagRepository; // IMPORTANTE: Añade esta importación
import com.example.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorialService {

    private final TutorialRepository tutorialRepository;
    private final TutorialMapper tutorialMapper;
    private final TagRepository tagRepository; // Inyectado para buscar etiquetas al asociarlas
    private final ObjectMapper objectMapper;   // Inyectado para procesar la magia del PATCH

    //....................... getTutorials .......................................
    @Transactional(readOnly = true)
    public List<Tutorial> getTutorials(String title) {
        if (title == null || title.trim().isEmpty()) {
            return tutorialRepository.findAll();
        }
        return tutorialRepository.findByTitleContaining(title);
    }

    //....................... findById .......................................
    public Tutorial findById(Long id) {
        return tutorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial no encontrado con id: " + id));
    }

    //....................... getPublishedTutorials .......................................
    @Transactional(readOnly = true)
    public List<Tutorial> getPublishedTutorials() {
        return tutorialRepository.findByPublished(true);
    }

    @Transactional(readOnly = true)
    public List<Tutorial> getTutorialsByTag(Long tagId) {
        return tutorialRepository.findTutorialsByTagsId(tagId);
    }

    //....................... getAllTagsByTutorialId (Transaccional ReadOnly) .......................................
    @Transactional(readOnly = true)
    public Set<Tag> getAllTagsByTutorialId(Long tutorialId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial no encontrado con id: " + tutorialId));

        return tutorial.getTags();
    }

    //....................... createTutorial .......................................
    @Transactional
    public Tutorial createTutorial(Tutorial tutorial) {
        // Si los tags vienen nulos desde el JSON, inicializa una lista vacía
        if (tutorial.getTags() == null) {
            tutorial.setTags(new HashSet<>()); // O HashSet<> según tu entidad
        }
        return tutorialRepository.save(tutorial);
    }

    //....................... updateTutorial (PUT - Reemplazo Total) .......................................
    @Transactional
    public Tutorial updateTutorial(Long id, Tutorial newTutorialData) {
        Tutorial existingTutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial no encontrado con id: " + id));

        // PUT sobrescribe explícitamente cada campo plano
        existingTutorial.setTitle(newTutorialData.getTitle());
        existingTutorial.setDescription(newTutorialData.getDescription());
        existingTutorial.setPublished(newTutorialData.isPublished());
        // Cuidado: Si newTutorialData.getTags() viene vacío o nulo, PUT desasociará todas las etiquetas actuales
        existingTutorial.setTags(newTutorialData.getTags());

        return tutorialRepository.save(existingTutorial);
    }

    //....................... patchTutorial (PATCH - Actualización Parcial) .......................................
    @Transactional
    public Tutorial patchTutorial(Long id, Map<String, Object> updates) {
        Tutorial existingTutorial = tutorialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial no encontrado con id: " + id));

        // Seguridad: Bloquear mutación de claves primarias
        if (updates.containsKey("id")) {
            throw new IllegalArgumentException("No se permite modificar el identificador (id).");
        }

        // Seguridad ManyToMany: Bloquear sobreescritura accidental de la colección mapeada automáticamente
        if (updates.containsKey("tags")) {
            throw new IllegalArgumentException("Para gestionar etiquetas use los endpoints específicos de asociación.");
        }

        try {
            // Jackson mezcla el mapa sobreescribiendo SOLO las propiedades enviadas en el JSON
            objectMapper.updateValue(existingTutorial, updates);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error al procesar la actualización parcial del tutorial", e);
        }

        return tutorialRepository.save(existingTutorial);
    }

    //....................... addTagToTutorial (PATCH Semántico) .......................................
    @Transactional
    public Tutorial addTagToTutorial(Long tutorialId, Long tagId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial no encontrado con id: " + tutorialId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con id: " + tagId));

        // Usamos tu método helper de la entidad para sincronizar la bidireccionalidad en memoria
        tutorial.addTag(tag);

        return tutorialRepository.save(tutorial);
    }

    //....................... createAndAddTagToTutorial (Transaccional) .......................................
    @Transactional
    public Tutorial createAndAddTagToTutorial(Long tutorialId, Tag newTag) {

        log.info("Añadiendo tag {} al tutorial {}", newTag.getName(), tutorialId);

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tutorial no encontrado con id: " + tutorialId));

        tutorial.addTag(newTag);

        return tutorialRepository.save(tutorial);
    }

    //....................... getTutorialsByTagId .......................................
    @Transactional(readOnly = true)
    public List<Tutorial> getTutorialsByTagId(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new ResourceNotFoundException("Not found Tag with id = " + tagId);
        }
        return tutorialRepository.findTutorialsByTagsId(tagId);
    }


    //....................... removeTagFromTutorial (PATCH Semántico) .......................................
    @Transactional
    public Tutorial removeTagFromTutorial(Long tutorialId, Long tagId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial no encontrado con id: " + tutorialId));

        // Usamos tu método helper sobrecargado que busca por ID dentro del Set y desasocia
        tutorial.removeTag(tagId);

        return tutorialRepository.save(tutorial);
    }

    //....................... getTutorialsDTO (Transformación Masiva) .......................................
    @Transactional(readOnly = true)
    public List<TutorialDTO> getTutorialsDTO(String title) {
        // Recuperamos la lista de entidades originales de la base de datos
        List<Tutorial> tutorials = (title == null || title.trim().isEmpty())
                ? tutorialRepository.findAll()
                : tutorialRepository.findByTitleContaining(title);

        // Abrimos el Stream y mapeamos de forma masiva a Records
        return tutorials.stream()
                .map(tutorial -> new TutorialDTO(
                        tutorial.getId(),
                        tutorial.getTitle(),
                        tutorial.getDescription(),
                        tutorial.isPublished(),
                        // Transformamos también su Set<Tag> interno a un Set<String> de nombres
                        tutorial.getTags().stream()
                                .map(Tag::getName)
                                .collect(java.util.stream.Collectors.toSet())
                ))
                .toList(); // En Java moderno (Java 16+) .toList() genera una lista inmutable ideal
    }

    @Transactional(readOnly = true)
    public List<TutorialDTO> getTutorialsDTOMap(String title) {
        //  Recuperamos las entidades aplicando el filtro
        List<Tutorial> tutorials = (title == null || title.trim().isEmpty())
                ? tutorialRepository.findAll()
                : tutorialRepository.findByTitleContaining(title);

        // Transformamos usando el stream y el mapper inyectado
        return tutorials.stream()
                .map(tutorialMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TutorialClosed> findBy(){
        return tutorialRepository.findBy();
    }

}
