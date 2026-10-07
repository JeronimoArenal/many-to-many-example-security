package com.example.domain.entity;

import com.example.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tutorials")
public class Tutorial extends BaseEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Column(length = 75, nullable = false)
    private String title;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean published = false;


    // .......................... Relationship ...........................................
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "tutorial_tags",
            joinColumns = @JoinColumn(name = "tutorial_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();


    /**
     * Java dicta una regla sagrada en su documentación
     * "Si dos objetos son iguales según el método equals, ambos deben devolver obligatoriamente el mismo hashCode.
     * "Y lo más importante para las bases de datos:"El valor del hashCode de un objeto no debe cambiar jamás mientras ese objeto esté
     * dentro de una colección como un HashSet."
     *
     */


    // Determina que dos tutoriales son iguales únicamente si comparten la misma dirección de memoria,
    // o si ambos ya existen en la base de datos (id != null) y comparten el mismo ID.
//    //COMENTADO PORQUE LO HEREDAMOS DE BASEENTITY
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;                           // Compara las referencias en la memoria RAM
//        if (!(o instanceof Tutorial tutorial)) return false;  // Pattern Matching: protege contra nulos, soporta proxies de Hibernate y castea automáticamente
//        return id != null && id.equals(tutorial.id);
//    }
//
//    // Al devolver el hash de la clase, el valor siempre es la misma constante durante la ejecución.
//    // Esto garantiza la estabilidad del objeto dentro de colecciones Set antes y después de ser persistido.
//    @Override
//    public int hashCode() {
//        return getClass().hashCode();
//    }


//    // .......................... M E T H O D S ...........................................
//    public void addTag(Tag tag) {
//        this.tags.add(tag);
//        tag.getTutorials().add(this);
//    }

    // .......................... M E T H O D S ...........................................
    public void addTag(Tag tag) {
        // Aseguramos que la lista del tutorial no sea nula
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag);

        // EVITA EL ERROR 500: Si la nueva etiqueta tiene su Set en null, lo inicializamos
        if (tag.getTutorials() == null) {
            tag.setTutorials(new HashSet<>());
        }
        tag.getTutorials().add(this);
    }


    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getTutorials().remove(this);
    }

    public void removeTag(Long tagId) {
        tags.stream()
                .filter(tag -> Objects.equals(tag.getId(), tagId))
                .findFirst()
                .ifPresent(this::removeTag);
    }

}
