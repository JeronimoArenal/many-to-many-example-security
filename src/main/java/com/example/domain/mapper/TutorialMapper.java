package com.example.domain.mapper;

import com.example.domain.dto.response.TutorialDTO;
import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")      //Hace que la clase generada tenga @Component
public interface TutorialMapper {

    // Mapea un solo Tutorial a TutorialDTO
    //Con @Mapping indicamos de dónde debe sacar los datos para una variable que se llama en origen: tags, y en destino: tagNames
    @Mapping(target = "tagNames", source = "tags")
    TutorialDTO toDto(Tutorial tutorial);

    // MapStruct usará este método automáticamente para convertir el Set<Tag> a Set<String>
    default Set<String> mapTagsToNames(Set<Tag> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }
}
