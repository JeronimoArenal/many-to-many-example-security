package com.example.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TutorialRelationshipTest {

    @Test
    @DisplayName("Debería eliminar la etiqueta correctamente usando equals por ID")
    void testRemoveTagConEqualsYHashCode() {
        // 1. Configuramos el escenario inicial (Simulamos datos de la BD con ID = 1)
        Tutorial tutorial = new Tutorial();
        tutorial.setId(1L);
        tutorial.setTitle("Spring Boot Guide");

        // Creamos la etiqueta original en memoria
        Tag tagOriginal = new Tag();
        tagOriginal.setId(10L);
        tagOriginal.setName("Java");

        // Asociamos la etiqueta al tutorial
        tutorial.addTag(tagOriginal);

        assertEquals(1, tutorial.getTags().size(), "El tutorial debe tener 1 etiqueta");

        // 2. Simulamos una nueva petición HTTP o consulta donde llega la misma etiqueta
        // pero es una instancia DIFERENTE en la memoria RAM (diferente dirección)
        Tag tagInstanciaDiferente = new Tag();
        tagInstanciaDiferente.setId(10L); // Mismo ID de la base de datos
        tagInstanciaDiferente.setName("Java");

        // 3. Intentamos eliminar la etiqueta usando la nueva instancia
        tutorial.removeTag(tagInstanciaDiferente);

        // 4. VERIFICACIÓN DEL ÉXITO (Gracias a tus métodos implementados)
        // Como tu equals compara por ID (10L == 10L), Java la encuentra y la elimina.
        assertTrue(tutorial.getTags().isEmpty(), "La lista de etiquetas debería estar vacía");
    }

    @Test
    @DisplayName("Demostración de por qué SÍ se necesitan equals y hashCode")
    void pruebaDeFuego() {
        Tutorial tutorial = new Tutorial();
        tutorial.setId(1L);

        // Creamos dos instancias FÍSICAMENTE distintas en la memoria (dos 'new')
        Tag tag1 = new Tag();
        tag1.setId(99L);
        tag1.setName("Java");

        Tag tag2 = new Tag();
        tag2.setId(99L); // Mismo ID de base de datos
        tag2.setName("Java");

        // Añadimos la primera al tutorial
        tutorial.addTag(tag1);

        // Intentamos borrar usando la segunda instancia
        // Esto llamará a tags.remove(tag2)
        tutorial.removeTag(tag2);

        // Si tienes equals/hashCode comentados, esto VA A FALLAR (el tamaño seguirá siendo 1)
        // Si los tienes desomentados, esto PASARÁ (el tamaño será 0)
        assertEquals(0, tutorial.getTags().size(),
                "¡FALTA EQUALS! El Set no pudo encontrar la etiqueta por ID y no la borró.");
    }

}
