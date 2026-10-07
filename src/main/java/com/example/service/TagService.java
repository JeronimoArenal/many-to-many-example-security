package com.example.service;

import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.TagRepository;
import com.example.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;


    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }


    //....................... getTagsByTutorialId (Transaccional) .......................................
    @Transactional(readOnly = true)
    public List<Tag> getTagsByTutorialId(Long tutorialId) {
        // Usamos tu método personalizado del repositorio
        return tagRepository.findTagsByTutorialsId(tutorialId);
    }

    @Transactional
    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
}
