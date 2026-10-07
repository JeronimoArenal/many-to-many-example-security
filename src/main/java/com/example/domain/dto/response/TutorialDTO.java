package com.example.domain.dto.response;

import java.util.Set;

public record TutorialDTO(
        Long id,
        String title,
        String description,
        boolean published,
        Set<String> tagNames // Simplificamos los objetos Tag a solo una lista de nombres de etiquetas
) {}
