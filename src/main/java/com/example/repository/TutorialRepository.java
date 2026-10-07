package com.example.repository;

import com.example.domain.entity.Tutorial;
import com.example.projection.interfacebased.closed.TutorialClosed;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

    @EntityGraph(attributePaths = "tags")
    List<Tutorial> findByPublished(boolean published);

    @EntityGraph(attributePaths = "tags")
    List<Tutorial> findByTitleContaining(String title);

    @EntityGraph(attributePaths = {"tags"}) //Spring Data JPA genera una consulta con un JOIN para traer los tags evitando consulta N+1
    List<Tutorial> findTutorialsByTagsId(Long tagId);

    @Override
    @EntityGraph(attributePaths = "tags")
    List<Tutorial> findAll();

    // Closed Interface Based
    List<TutorialClosed> findBy();

}