package com.example.config;

import com.example.domain.entity.Tag;
import com.example.domain.entity.Tutorial;
import com.example.repository.TagRepository;
import com.example.repository.TutorialRepository;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TutorialRepository tutorialRepository;
    private final TagRepository tagRepository;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        log.info("========================================");
        log.info("INICIALIZANDO DATOS...");
        log.info("========================================");

        // =========================================================
        // 1. ROLES
        // =========================================================

        Role adminRole = createRole(
                "ROLE_ADMIN",
                "Administrador del sistema"
        );

        Role userRole = createRole(
                "ROLE_USER",
                "Usuario estándar"
        );

        // =========================================================
        // 2. USUARIOS INICIALES
        // =========================================================

        /*
         * Este usuario sirve como ADMIN inicial del sistema.
         *
         * Importante:
         * Los usuarios normales NO podrán crear otros usuarios.
         * Solamente un usuario con ROLE_ADMIN podrá hacerlo.
         */
        createUserIfNotExists(
                "admin",
                "admin@test.com",
                "123456",
                adminRole
        );

        /*
         * Usuario normal para poder probar las restricciones
         * de ROLE_USER.
         */
        createUserIfNotExists(
                "user",
                "user@test.com",
                "123456",
                userRole
        );

        // =========================================================
        // 3. TAGS Y TUTORIALES
        // =========================================================

        initializeTutorialData();

        log.info("========================================");
        log.info("DATOS INICIALIZADOS CORRECTAMENTE");
        log.info("========================================");
    }

    // =============================================================
    // CREAR ROL
    // =============================================================

    private Role createRole(String code, String description) {

        return roleRepository.findByCode(code)
                .orElseGet(() -> {

                    Role role = new Role();

                    role.setCode(code);
                    role.setDescription(description);

                    Role savedRole = roleRepository.save(role);

                    log.info("Rol '{}' creado.", code);

                    return savedRole;
                });
    }

    // =============================================================
    // CREAR USUARIO
    // =============================================================

    private void createUserIfNotExists(
            String username,
            String email,
            String password,
            Role role) {

        /*
         * Comprobamos por username.
         *
         * Como username y email son únicos en la entidad,
         * también podríamos comprobar por email.
         */
        if (userRepository.findByUsername(username).isPresent()) {

            log.info(
                    "El usuario '{}' ya existe. No se crea de nuevo.",
                    username
            );

            return;
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);

        // La contraseña se almacena siempre codificada con BCrypt.
        user.setPasswordHash(
                passwordEncoder.encode(password)
        );

        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setAccountExpired(false);
        user.setCredentialsExpired(false);

        user.getRoles().add(role);

        userRepository.save(user);

        log.info(
                "Usuario '{}' creado con rol '{}'.",
                username,
                role.getCode()
        );
    }

    // =============================================================
    // TAGS + TUTORIALES
    // =============================================================

    private void initializeTutorialData() {

        /*
         * Si ya existen datos, no los volvemos a insertar.
         *
         * Este control es independiente de la creación de usuarios
         * y roles.
         */
        if (tagRepository.count() > 0
                || tutorialRepository.count() > 0) {

            log.info(
                    "La base de datos ya contiene Tags/Tutoriales. " +
                            "Se omite su inicialización."
            );

            return;
        }

        log.info("Inicializando Tags y Tutoriales...");

        // =========================================================
        // TAGS
        // =========================================================

        Tag javaTag = Tag.builder()
                .name("Java")
                .build();

        Tag springTag = Tag.builder()
                .name("Spring Boot")
                .build();

        Tag dockerTag = Tag.builder()
                .name("Docker")
                .build();

        Tag webTag = Tag.builder()
                .name("Web")
                .build();

        Tag dbTag = Tag.builder()
                .name("Databases")
                .build();

        Tag testingTag = Tag.builder()
                .name("Testing")
                .build();

        tagRepository.saveAll(
                List.of(
                        javaTag,
                        springTag,
                        dockerTag,
                        webTag,
                        dbTag,
                        testingTag
                )
        );

        // =========================================================
        // TUTORIAL 1
        // =========================================================

        Tutorial springIntro = Tutorial.builder()
                .title("Introducción a Spring Boot")
                .description(
                        "Aprende a configurar tu primer proyecto web desde cero."
                )
                .published(true)
                .build();

        springIntro.addTag(javaTag);
        springIntro.addTag(springTag);
        springIntro.addTag(webTag);

        // =========================================================
        // TUTORIAL 2
        // =========================================================

        Tutorial dockerBasics = Tutorial.builder()
                .title("Docker para Principiantes")
                .description(
                        "Conceptos básicos de contenedores, imágenes y volúmenes."
                )
                .published(true)
                .build();

        dockerBasics.addTag(dockerTag);

        // =========================================================
        // TUTORIAL 3
        // =========================================================

        Tutorial advancedJava = Tutorial.builder()
                .title("Java Avanzado: Programación Reactiva")
                .description(
                        "Domina Project Reactor y flujos de datos asíncronos."
                )
                .published(false)
                .build();

        advancedJava.addTag(javaTag);

        // =========================================================
        // TUTORIAL 4
        // =========================================================

        Tutorial microservices = Tutorial.builder()
                .title("Microservicios con Spring Cloud y Docker")
                .description(
                        "Estrategias de descentralización, descubrimiento " +
                                "de servicios y despliegue en contenedores."
                )
                .published(true)
                .build();

        microservices.addTag(springTag);
        microservices.addTag(dockerTag);
        microservices.addTag(webTag);

        // =========================================================
        // TUTORIAL 5
        // =========================================================

        Tutorial springTesting = Tutorial.builder()
                .title("Pruebas Unitarias Efectivas en Spring")
                .description(
                        "Aprende a usar JUnit 5, Mockito y Testcontainers " +
                                "para asegurar tu código."
                )
                .published(false)
                .build();

        springTesting.addTag(javaTag);
        springTesting.addTag(springTag);
        springTesting.addTag(testingTag);

        // =========================================================
        // PERSISTENCIA
        // =========================================================

        tutorialRepository.saveAll(
                List.of(
                        springIntro,
                        dockerBasics,
                        advancedJava,
                        microservices,
                        springTesting
                )
        );

        log.info("Tags y Tutoriales inicializados correctamente.");
    }
}