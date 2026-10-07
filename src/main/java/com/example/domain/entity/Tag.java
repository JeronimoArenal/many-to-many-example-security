package com.example.domain.entity;

import com.example.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Column(length = 200)
    private String name;

    // .......................... Relationship ...........................................
    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude // EVITA EL BUCLE INFINITO
    @Builder.Default
    @JsonIgnore
    private Set<Tutorial> tutorials = new HashSet<>();

    /**
     * Java dicta una regla sagrada en su documentación
     * "Si dos objetos son iguales según el método equals, ambos deben devolver obligatoriamente el mismo hashCode.
     * "Y lo más importante para las bases de datos:"El valor del hashCode de un objeto no debe cambiar jamás mientras ese objeto esté
     * dentro de una colección como un HashSet."
     *
     */

    // Determina que dos tags son iguales únicamente si comparten la misma dirección de memoria,
    // o si ambos ya existen en la base de datos (id != null) y comparten el mismo ID.
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;                     // Compara las referencias en la memoria RAM
//        if (!(o instanceof Tag tag)) return false;      // Pattern Matching: protege contra nulos, soporta proxies de Hibernate y castea automáticamente
//        return id != null && id.equals(tag.id);
//    }
//
//    // Al devolver el hash de la clase, el valor siempre es la misma constante durante la ejecución.
//    // Esto garantiza la estabilidad del objeto dentro de colecciones Set antes y después de ser persistido.
//    @Override
//    public int hashCode() {
//        return getClass().hashCode();
//    }
}
