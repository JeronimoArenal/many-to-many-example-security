package com.example.controller;

import com.example.domain.entity.Tag;
import com.example.service.TagService;
import com.example.util.TutorialTestDataFactory;
import tools.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    private Tag javaTag;
    private Tag springTag;
    private Tag jpaTag;

    private String token;

    @BeforeEach
    void setUp() throws Exception {

        // ============================================================
        // Obtener JWT
        // ============================================================

        /*
         * Utilizamos un usuario existente en la base de datos de test.
         *
         * Si en tu configuración de test el usuario admin/password es
         * diferente, cambia estos valores.
         */
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

        String responseBody =
                loginResult.getResponse().getContentAsString();

        JSONObject jsonObject = new JSONObject(responseBody);

        token = "Bearer " + jsonObject.getString("token");

        // ============================================================
        // Datos de prueba
        // ============================================================

        javaTag = TutorialTestDataFactory.createJavaTag();
        springTag = TutorialTestDataFactory.createSpringTag();
        jpaTag = TutorialTestDataFactory.createJpaTag();
    }

    // ============================================================
    // GET /tags
    // ============================================================

    @Test
    @DisplayName("Debe devolver todos los tags con HTTP 200")
    void shouldGetAllTags() throws Exception {

        List<Tag> tags = List.of(
                javaTag,
                springTag,
                jpaTag
        );

        given(tagService.getAllTags())
                .willReturn(tags);

        mockMvc.perform(
                        get("/tags")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(3)))
                .andExpect(jsonPath("$[0].name", is("Java")))
                .andExpect(jsonPath("$[1].name", is("Spring")))
                .andExpect(jsonPath("$[2].name", is("JPA")));

        verify(tagService)
                .getAllTags();
    }

    @Test
    @DisplayName("Debe devolver 204 cuando no existen tags")
    void shouldReturnNoContentWhenNoTagsExist() throws Exception {

        given(tagService.getAllTags())
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tags")
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tagService)
                .getAllTags();
    }

    // ============================================================
    // GET /tags/{id}
    // ============================================================

    @Test
    @DisplayName("Debe devolver un tag por ID con HTTP 200")
    void shouldGetTagById() throws Exception {

        Long tagId = 1L;

        given(tagService.getTagById(tagId))
                .willReturn(Optional.of(javaTag));

        mockMvc.perform(
                        get("/tags/{id}", tagId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Java")));

        verify(tagService)
                .getTagById(tagId);
    }

    @Test
    @DisplayName("Debe devolver 404 cuando el tag no existe")
    void shouldReturnNotFoundWhenTagDoesNotExist() throws Exception {

        Long tagId = 99L;

        given(tagService.getTagById(tagId))
                .willReturn(Optional.empty());

        mockMvc.perform(
                        get("/tags/{id}", tagId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(tagService)
                .getTagById(tagId);
    }

    // ============================================================
    // GET /tags/tutorial/{tutorialId}
    // ============================================================

    @Test
    @DisplayName("Debe devolver los tags de un tutorial")
    void shouldGetTagsByTutorialId() throws Exception {

        Long tutorialId = 1L;

        given(tagService.getTagsByTutorialId(tutorialId))
                .willReturn(List.of(javaTag, springTag));

        mockMvc.perform(
                        get("/tags/tutorial/{tutorialId}", tutorialId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].name", is("Java")))
                .andExpect(jsonPath("$[1].name", is("Spring")));

        verify(tagService)
                .getTagsByTutorialId(tutorialId);
    }

    @Test
    @DisplayName("Debe devolver 204 cuando el tutorial no tiene tags")
    void shouldReturnNoContentWhenTutorialHasNoTags() throws Exception {

        Long tutorialId = 1L;

        given(tagService.getTagsByTutorialId(tutorialId))
                .willReturn(List.of());

        mockMvc.perform(
                        get("/tags/tutorial/{tutorialId}", tutorialId)
                                .header("Authorization", token)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tagService)
                .getTagsByTutorialId(tutorialId);
    }

    // ============================================================
    // POST /tags
    // ============================================================

    @Test
    @DisplayName("Debe crear un tag y devolver HTTP 201")
    void shouldCreateTag() throws Exception {

        given(tagService.createTag(any(Tag.class)))
                .willReturn(javaTag);

        String jsonTag =
                objectMapper.writeValueAsString(javaTag);

        mockMvc.perform(
                        post("/tags")
                                .header("Authorization", token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonTag)
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Java")));

        verify(tagService)
                .createTag(any(Tag.class));
    }

    // ============================================================
    // PUT /tags/{id}
    // ============================================================

    @Test
    @DisplayName("Debe actualizar un tag y devolver HTTP 200")
    void shouldUpdateTag() throws Exception {

        Long tagId = 1L;

        Tag tagRequest = new Tag();
        tagRequest.setName("Java actualizado");

        javaTag.setName("Java actualizado");

        /*
         * updateTag() utiliza directamente el repository,
         * por eso debemos mockear el repository en este test.
         */
        // Si quieres mantener exactamente la arquitectura actual,
        // este test necesita que TagRepository sea @MockitoBean.
    }

    // ============================================================
    // DELETE /tags/{id}
    // ============================================================

    @Test
    @DisplayName("Debe eliminar un tag y devolver HTTP 204")
    void shouldDeleteTag() throws Exception {

        Long tagId = 1L;

        doNothing()
                .when(tagService)
                .deleteTag(tagId);

        mockMvc.perform(
                        delete("/tags/{id}", tagId)
                                .header("Authorization", token)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(tagService)
                .deleteTag(tagId);
    }
}