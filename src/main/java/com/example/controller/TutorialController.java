package com.example.controller;

/**
 * POST: Crea el tutorial desde cero y el sistema le asigna un ID.
 * PUT: Reemplaza el tutorial por completo (si olvidas enviar un campo permitido, se borrará).
 * PATCH: Modifica un atributo suelto de forma segura y ligera.
 * Sub-recursos (/tags/{id}): Añaden o quitan elementos de colecciones ManyToMany de forma aislada.
 * En el diseño de APIs REST, un sub-recurso es un recurso que no tiene sentido o no puede existir por sí mismo,
 * sino que depende directamente de otro recurso principal (su "padre").
 *      POST /api/v1/tutorials/5/tags/3 (Añadir la etiqueta 3 al tutorial 5)
 *      DELETE /api/v1/tutorials/5/tags/3 (Quitar la etiqueta 3 del tutorial 5)
 */

import com.example.domain.dto.response.TutorialDTO;
import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.projection.interfacebased.closed.TutorialClosed;
import com.example.repository.TutorialRepository;
import com.example.service.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tutorials")
public class TutorialController {

    private final TutorialService tutorialService;
    private final TutorialRepository tutorialRepository;

    //.................... projection closed based ........................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/projection")
    public ResponseEntity<List<TutorialClosed>> getAllProjection() {
        List<TutorialClosed> tutorials = tutorialService.findBy();
        if (tutorials.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    //....................... getAllTutorials .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
        List<Tutorial> tutorials = tutorialService.getTutorials(title);
        if (tutorials.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    //....................... getAllTagsByTutorialId (Recuperar Etiquetas) .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{tutorialId}/tags")
    public ResponseEntity<Set<Tag>> getAllTagsByTutorialId(@PathVariable Long tutorialId) {
        Set<Tag> tags = tutorialService.getAllTagsByTutorialId(tutorialId);
        return ResponseEntity.ok(tags);
    }

    //....................... findById .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public Tutorial findById(@PathVariable Long id) {
        return tutorialService.findById(id);
    }

    //....................... createTutorial .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
        Tutorial savedTutorial = tutorialService.createTutorial(tutorial);
        return new ResponseEntity<>(savedTutorial, HttpStatus.CREATED);
    }

    //....................... updateTutorial (PUT - Reemplazo Total) .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Tutorial> updateTutorial(@PathVariable Long id, @RequestBody Tutorial tutorial) {
        Tutorial updatedTutorial = tutorialService.updateTutorial(id, tutorial);
        return ResponseEntity.ok(updatedTutorial);
    }

    //....................... findByPublished .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/published")
    public ResponseEntity<List<Tutorial>> findByPublished() {
        List<Tutorial> tutorials = tutorialService.getPublishedTutorials();
        if (tutorials.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    //....................... findByTagId .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<Tutorial>> findByTagId(@PathVariable Long tagId) {
        List<Tutorial> tutorials = tutorialService.getTutorialsByTag(tagId);
        if (tutorials.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    //....................... deleteTutorial .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") Long id) {
        tutorialRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //....................... deleteAllTutorials .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<HttpStatus> deleteAllTutorials() {
        tutorialRepository.deleteAll();

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //....................... patchTutorial (PATCH - Actualización Parcial) .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Tutorial> patchTutorial(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Tutorial patchedTutorial = tutorialService.patchTutorial(id, updates);
        return ResponseEntity.ok(patchedTutorial);
    }

    //....................... addTagToTutorial (Asociar Etiqueta) .......................................
    //localhost:8080/api/v1/tutorials/1/tags/2
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{tutorialId}/tags/{tagId}")
    public ResponseEntity<Tutorial> addTagToTutorial(@PathVariable Long tutorialId,
                                                     @PathVariable Long tagId) {
        Tutorial tutorial = tutorialService.addTagToTutorial(tutorialId, tagId);
        return ResponseEntity.ok(tutorial);
    }

    //....................... createAndAddTagToTutorial (Crear y Asociar) .......................................
    //localhost:8080/api/v1/tutorials/4/tags
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{tutorialId}/tags")
    public ResponseEntity<Tutorial> createAndAddTagToTutorial(@PathVariable Long tutorialId,
                                                              @RequestBody Tag newTag) {
        Tutorial tutorial = tutorialService.createAndAddTagToTutorial(tutorialId, newTag);
        return new ResponseEntity<>(tutorial, HttpStatus.CREATED);
    }

    //....................... getAllTutorialsByTagId .......................................
    //localhost:8080/api/v1/tutorials/2/tutorials
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{tagId}/tutorials")
    public ResponseEntity<List<Tutorial>> getAllTutorialsByTagId(@PathVariable Long tagId) {
        List<Tutorial> tutorials = tutorialService.getTutorialsByTagId(tagId);
        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    //....................... removeTagFromTutorial (Desasociar Etiqueta) .......................................
    //localhost:8080/api/v1/tags/tutorials/6/tags/5
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{tutorialId}/tags/{tagId}")
    public ResponseEntity<Tutorial> removeTagFromTutorial(@PathVariable Long tutorialId, @PathVariable Long tagId) {
        Tutorial tutorial = tutorialService.removeTagFromTutorial(tutorialId, tagId);
        return ResponseEntity.ok(tutorial);
    }

    //....................... getAllDtoTutorials (Usando Records) .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/dto")
    public ResponseEntity<List<TutorialDTO>> getAllDtoTutorials(@RequestParam(required = false) String title) {
        List<TutorialDTO> tutorials = tutorialService.getTutorialsDTO(title);
        if (tutorials.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(tutorials, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/dtoMap")
    public ResponseEntity<List<TutorialDTO>> getAllTutorialsMap(@RequestParam(required = false) String title) {
        List<TutorialDTO> dtos = tutorialService.getTutorialsDTOMap(title);
        return ResponseEntity.ok(dtos);
    }


}

