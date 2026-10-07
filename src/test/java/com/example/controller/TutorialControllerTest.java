package com.example.controller;

import com.example.domain.dto.response.TutorialDTO;
import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.projection.interfacebased.closed.TutorialClosed;
import com.example.repository.TutorialRepository;
import com.example.service.TutorialService;
import com.example.util.TutorialTestDataFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TutorialService tutorialService;

    @MockitoBean
    private TutorialRepository tutorialRepository;

    private Tutorial tutorial1;
    private Tutorial tutorial2;

    private Tag javaTag;
    private Tag springTag;

    private String token;

    @BeforeEach
    void setUp() throws Exception {

        // ============================================================
        // Obtener JWT
        // ============================================================

        String loginJson = """
                {
                    "email": "admin@test.com",
                    "password": "123456"
                }
                """;

        MvcResult loginResult = mockMvc
                .perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();

        JSONObject jsonObject = new JSONObject(responseBody);

        token = "Bearer " + jsonObject.getString("token");

        // ============================================================
        // Datos de prueba
        // ============================================================

        tutorial1 = TutorialTestDataFactory.createTutorial1();
        tutorial2 = TutorialTestDataFactory.createTutorial2();
        javaTag = TutorialTestDataFactory.createJavaTag();
        springTag = TutorialTestDataFactory.createSpringTag();
    }

    // ============================================================
    // GET /tutorials
    // ============================================================

    @Test
    @DisplayName("Debe devolver todos los tutoriales con HTTP 200")
    void shouldGetAllTutorials() throws Exception {

        given(tutorialService.getTutorials(null))
                .willReturn(List.of(tutorial1, tutorial2));

        mockMvc.perform(
                        get("/tutorials")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].title",
                        is("Spring Boot desde cero")))
                .andExpect(jsonPath("$[1].title",
                        is("Spring Security con JWT")));

        verify(tutorialService)
                .getTutorials(null);
    }

    @Test
    @DisplayName("Debe filtrar tutoriales por título")
    void shouldGetTutorialsByTitle() throws Exception {

        given(tutorialService.getTutorials("Spring"))
                .willReturn(List.of(tutorial1));

        mockMvc.perform(
                        get("/tutorials")
                                .param("title", "Spring")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .getTutorials("Spring");
    }

    @Test
    @DisplayName("Debe devolver 204 cuando no existen tutoriales")
    void shouldReturnNoContentWhenNoTutorialsExist() throws Exception {

        given(tutorialService.getTutorials(null))
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tutorials")
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialService)
                .getTutorials(null);
    }

    // ============================================================
    // GET /tutorials/{id}
    // ============================================================

    @Test
    @DisplayName("Debe devolver un tutorial por ID")
    void shouldFindTutorialById() throws Exception {

        Long tutorialId = 1L;

        given(tutorialService.findById(tutorialId))
                .willReturn(tutorial1);

        mockMvc.perform(
                        get("/tutorials/{id}", tutorialId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title",
                        is("Spring Boot desde cero")))
                .andExpect(jsonPath("$.description",
                        is("Introducción a Spring Boot")));

        verify(tutorialService)
                .findById(tutorialId);
    }

    // ============================================================
    // GET /tutorials/published
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tutoriales publicados")
    void shouldGetPublishedTutorials() throws Exception {

        given(tutorialService.getPublishedTutorials())
                .willReturn(List.of(tutorial1));

        mockMvc.perform(
                        get("/tutorials/published")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .getPublishedTutorials();
    }

    @Test
    @DisplayName("Debe devolver 204 cuando no existen tutoriales publicados")
    void shouldReturnNoContentWhenNoPublishedTutorialsExist()
            throws Exception {

        given(tutorialService.getPublishedTutorials())
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tutorials/published")
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialService)
                .getPublishedTutorials();
    }

    // ============================================================
    // GET /tutorials/tag/{tagId}
    // ============================================================

    @Test
    @DisplayName("Debe devolver tutoriales asociados a un tag")
    void shouldGetTutorialsByTag() throws Exception {

        Long tagId = 1L;

        given(tutorialService.getTutorialsByTag(tagId))
                .willReturn(List.of(tutorial1, tutorial2));

        mockMvc.perform(
                        get("/tutorials/tag/{tagId}", tagId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        verify(tutorialService)
                .getTutorialsByTag(tagId);
    }

    @Test
    @DisplayName("Debe devolver 204 cuando el tag no tiene tutoriales")
    void shouldReturnNoContentWhenTagHasNoTutorials()
            throws Exception {

        Long tagId = 1L;

        given(tutorialService.getTutorialsByTag(tagId))
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tutorials/tag/{tagId}", tagId)
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialService)
                .getTutorialsByTag(tagId);
    }

    // ============================================================
    // GET /tutorials/{tutorialId}/tags
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tags de un tutorial")
    void shouldGetAllTagsByTutorialId() throws Exception {

        Long tutorialId = 1L;

        Set<Tag> tags =
                Set.of(javaTag, springTag);

        given(tutorialService.getAllTagsByTutorialId(tutorialId))
                .willReturn(tags);

        mockMvc.perform(
                        get("/tutorials/{tutorialId}/tags", tutorialId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        verify(tutorialService)
                .getAllTagsByTutorialId(tutorialId);
    }

    // ============================================================
    // GET /tutorials/{tagId}/tutorials
    // ============================================================

    @Test
    @DisplayName("Debe devolver todos los tutoriales de un tag")
    void shouldGetAllTutorialsByTagId() throws Exception {

        Long tagId = 1L;

        given(tutorialService.getTutorialsByTagId(tagId))
                .willReturn(List.of(tutorial1, tutorial2));

        mockMvc.perform(
                        get("/tutorials/{tagId}/tutorials", tagId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));

        verify(tutorialService)
                .getTutorialsByTagId(tagId);
    }

    // ============================================================
    // GET /tutorials/projection
    // ============================================================

    @Test
    @DisplayName("Debe devolver las proyecciones")
    void shouldGetAllProjection() throws Exception {

        TutorialClosed projection1 =
                new TutorialClosed() {
                    @Override
                    public Long getId() {
                        return 1L;
                    }

                    @Override
                    public String getTitle() {
                        return "Spring Boot desde cero";
                    }

                    @Override
                    public String getDescription() {
                        return "Introducción a Spring Boot";
                    }
                };

        TutorialClosed projection2 =
                new TutorialClosed() {
                    @Override
                    public Long getId() {
                        return 2L;
                    }

                    @Override
                    public String getTitle() {
                        return "Spring Security con JWT";
                    }

                    @Override
                    public String getDescription() {
                        return "Autenticación y autorización con JWT";
                    }
                };

        given(tutorialService.findBy())
                .willReturn(List.of(projection1, projection2));

        mockMvc.perform(
                        get("/tutorials/projection")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].title",
                        is("Spring Boot desde cero")))
                .andExpect(jsonPath("$[1].title",
                        is("Spring Security con JWT")));

        verify(tutorialService)
                .findBy();
    }

    @Test
    @DisplayName("Debe devolver 204 cuando no existen proyecciones")
    void shouldReturnNoContentWhenNoProjectionsExist()
            throws Exception {

        given(tutorialService.findBy())
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tutorials/projection")
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialService)
                .findBy();
    }

    // ============================================================
    // POST /tutorials
    // ============================================================

    @Test
    @DisplayName("Debe crear un tutorial y devolver HTTP 201")
    void shouldCreateTutorial() throws Exception {

        given(tutorialService.createTutorial(any(Tutorial.class)))
                .willReturn(tutorial1);

        String jsonTutorial =
                objectMapper.writeValueAsString(tutorial1);

        mockMvc.perform(
                        post("/tutorials")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonTutorial)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title",
                        is("Spring Boot desde cero")))
                .andExpect(jsonPath("$.description",
                        is("Introducción a Spring Boot")));

        verify(tutorialService)
                .createTutorial(any(Tutorial.class));
    }

    // ============================================================
    // PUT /tutorials/{id}
    // ============================================================

    @Test
    @DisplayName("Debe actualizar completamente un tutorial")
    void shouldUpdateTutorial() throws Exception {

        Long tutorialId = 1L;

        given(tutorialService.updateTutorial(
                any(Long.class),
                any(Tutorial.class)
        )).willReturn(tutorial2);

        String jsonTutorial =
                objectMapper.writeValueAsString(tutorial2);

        mockMvc.perform(
                        put("/tutorials/{id}", tutorialId)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonTutorial)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title",
                        is("Spring Security con JWT")));

        verify(tutorialService)
                .updateTutorial(
                        any(Long.class),
                        any(Tutorial.class)
                );
    }

    // ============================================================
    // PATCH /tutorials/{id}
    // ============================================================

    @Test
    @DisplayName("Debe actualizar parcialmente un tutorial")
    void shouldPatchTutorial() throws Exception {

        Long tutorialId = 1L;

        Map<String, Object> updates =
                Map.of("title", "Nuevo título");

        given(tutorialService.patchTutorial(
                any(Long.class),
                any(Map.class)
        )).willReturn(tutorial1);

        String jsonUpdates =
                objectMapper.writeValueAsString(updates);

        mockMvc.perform(
                        patch("/tutorials/{id}", tutorialId)
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonUpdates)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .patchTutorial(
                        any(Long.class),
                        any(Map.class)
                );
    }

    // ============================================================
    // POST /tutorials/{tutorialId}/tags/{tagId}
    // ============================================================

    @Test
    @DisplayName("Debe añadir un tag a un tutorial")
    void shouldAddTagToTutorial() throws Exception {

        Long tutorialId = 1L;
        Long tagId = 2L;

        given(tutorialService.addTagToTutorial(
                tutorialId,
                tagId
        )).willReturn(tutorial1);

        mockMvc.perform(
                        post(
                                "/tutorials/{tutorialId}/tags/{tagId}",
                                tutorialId,
                                tagId
                        )
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .addTagToTutorial(tutorialId, tagId);
    }

    // ============================================================
    // POST /tutorials/{tutorialId}/tags
    // ============================================================

    @Test
    @DisplayName("Debe crear y asociar un nuevo tag")
    void shouldCreateAndAddTagToTutorial() throws Exception {

        Long tutorialId = 1L;

        given(tutorialService.createAndAddTagToTutorial(
                any(Long.class),
                any(Tag.class)
        )).willReturn(tutorial1);

        String jsonTag =
                objectMapper.writeValueAsString(javaTag);

        mockMvc.perform(
                        post(
                                "/tutorials/{tutorialId}/tags",
                                tutorialId
                        )
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonTag)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .createAndAddTagToTutorial(
                        any(Long.class),
                        any(Tag.class)
                );
    }

    // ============================================================
    // DELETE /tutorials/{id}
    // ============================================================

    @Test
    @DisplayName("Debe eliminar un tutorial y devolver HTTP 204")
    void shouldDeleteTutorial() throws Exception {

        Long tutorialId = 1L;

        doNothing()
                .when(tutorialRepository)
                .deleteById(tutorialId);

        mockMvc.perform(
                        delete("/tutorials/{id}", tutorialId)
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialRepository)
                .deleteById(tutorialId);
    }

    // ============================================================
    // DELETE /tutorials
    // ============================================================

    @Test
    @DisplayName("Debe eliminar todos los tutoriales")
    void shouldDeleteAllTutorials() throws Exception {

        doNothing()
                .when(tutorialRepository)
                .deleteAll();

        mockMvc.perform(
                        delete("/tutorials")
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialRepository)
                .deleteAll();
    }

    // ============================================================
    // DELETE /tutorials/{tutorialId}/tags/{tagId}
    // ============================================================

    @Test
    @DisplayName("Debe quitar un tag de un tutorial")
    void shouldRemoveTagFromTutorial() throws Exception {

        Long tutorialId = 1L;
        Long tagId = 2L;

        given(tutorialService.removeTagFromTutorial(
                tutorialId,
                tagId
        )).willReturn(tutorial1);

        mockMvc.perform(
                        delete(
                                "/tutorials/{tutorialId}/tags/{tagId}",
                                tutorialId,
                                tagId
                        )
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .removeTagFromTutorial(tutorialId, tagId);
    }

    // ============================================================
    // GET /tutorials/dto
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tutoriales como DTO")
    void shouldGetAllDtoTutorials() throws Exception {

        TutorialDTO dto1 = new TutorialDTO(
                1L,
                "Spring Boot desde cero",
                "Introducción a Spring Boot",
                true,
                Set.of("Java")
        );

        TutorialDTO dto2 = new TutorialDTO(
                2L,
                "Spring Security con JWT",
                "Autenticación y autorización con JWT",
                true,
                Set.of("Spring")
        );

        given(tutorialService.getTutorialsDTO(null))
                .willReturn(List.of(dto1, dto2));

        mockMvc.perform(
                        get("/tutorials/dto")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].title",
                        is("Spring Boot desde cero")))
                .andExpect(jsonPath("$[1].title",
                        is("Spring Security con JWT")));

        verify(tutorialService)
                .getTutorialsDTO(null);
    }

    @Test
    @DisplayName("Debe devolver 204 cuando no existen DTOs")
    void shouldReturnNoContentWhenNoDtoTutorialsExist()
            throws Exception {

        given(tutorialService.getTutorialsDTO(null))
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tutorials/dto")
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tutorialService)
                .getTutorialsDTO(null);
    }

    // ============================================================
    // GET /tutorials/dtoMap
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tutoriales DTO usando MapStruct")
    void shouldGetAllTutorialsMap() throws Exception {

        TutorialDTO dto = new TutorialDTO(
                1L,
                "Spring Boot desde cero",
                "Introducción a Spring Boot",
                true,
                Set.of("Java")
        );

        given(tutorialService.getTutorialsDTOMap(null))
                .willReturn(List.of(dto));

        mockMvc.perform(
                        get("/tutorials/dtoMap")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].title",
                        is("Spring Boot desde cero")));

        verify(tutorialService)
                .getTutorialsDTOMap(null);
    }

    @Test
    @DisplayName("Debe devolver una lista vacía en dtoMap")
    void shouldReturnEmptyListForDtoMap() throws Exception {

        given(tutorialService.getTutorialsDTOMap(null))
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tutorials/dtoMap")
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(0)));

        verify(tutorialService)
                .getTutorialsDTOMap(null);
    }
}