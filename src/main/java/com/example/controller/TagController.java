package com.example.controller;

import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.TagRepository;
import com.example.repository.TutorialRepository;
import com.example.service.TagService;
import com.example.service.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;
    private final TagRepository tagRepository;
    private final TutorialRepository tutorialRepository;


    //....................... getAllTags .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        if (tags.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(tags); // 200 OK con la lista
    }

    //....................... getTagById .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(tag -> ResponseEntity.ok(tag)) // 200 OK si existe
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found si no existe
    }

    //....................... getTagsByTutorialId .......................................
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/tutorial/{tutorialId}")
    public ResponseEntity<List<Tag>> getTagsByTutorialId(@PathVariable Long tutorialId) {
        List<Tag> tags = tagService.getTagsByTutorialId(tutorialId);
        if (tags.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(tags); // 200 OK
    }

    // Crear una nueva etiqueta (Ej: podría lanzar duplicados si el nombre es único)
    //....................... createTag .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        Tag savedTag = tagService.createTag(tag);
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(savedTag);
    }

    //....................... updateTag .......................................
    @PreAuthorize("hasRole('ADMIN')")
//    @PutMapping("/tags/{id}")
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(@PathVariable Long id, @RequestBody Tag tagRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TagId " + id + "not found"));

        tag.setName(tagRequest.getName());

        return new ResponseEntity<>(tagRepository.save(tag), HttpStatus.OK);
    }

    //....................... deleteTag .......................................
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build(); // 204 No Content al eliminar con éxito
    }

}

