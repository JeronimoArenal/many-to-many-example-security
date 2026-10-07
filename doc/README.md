# Spring Boot Security + JWT + Roles

Plantilla reutilizable para implementar autenticación y autorización con **Spring Boot, Spring Security, JWT, BCrypt y roles**, incluyendo validación de peticiones y manejo global de errores.

---

## 1. Objetivo

Este proyecto implementa una arquitectura de seguridad basada en:

* Spring Security
* JWT
* Login mediante email
* BCrypt para contraseñas
* Roles `ROLE_USER` y `ROLE_ADMIN`
* `@PreAuthorize` para autorización a nivel de endpoint
* Registro público de usuarios
* Creación administrativa de usuarios y roles
* Validación mediante Jakarta Bean Validation
* Manejo global de errores
* Aplicación stateless

La idea es que esta estructura pueda reutilizarse como base en otros proyectos Spring Boot.

---

# 2. Arquitectura general

El flujo de autenticación es:

```text
                    LOGIN
                      │
                      ▼
             AuthenticationManager
                      │
                      ▼
          CustomUserDetailsService
                      │
                      ▼
                  Database
                      │
                      ▼
               BCryptPassword
                      │
                      ▼
              Authentication OK
                      │
                      ▼
                  JwtService
                      │
                      ▼
                     JWT
```

En las siguientes peticiones:

```text
HTTP Request
     │
     │ Authorization: Bearer <JWT>
     ▼
JwtAuthenticationFilter
     │
     ▼
Extraer email del JWT
     │
     ▼
CustomUserDetailsService
     │
     ▼
Buscar usuario en BD
     │
     ▼
Cargar authorities
     │
     ▼
SecurityContextHolder
     │
     ▼
@PreAuthorize
     │
     ├── ROLE_USER
     │
     └── ROLE_ADMIN
```

La aplicación no utiliza sesiones HTTP tradicionales.

```java
SessionCreationPolicy.STATELESS
```

Cada petición protegida debe proporcionar su JWT.

---

# 3. Dependencias principales

Como mínimo se necesitan:

* Spring Web
* Spring Security
* Spring Data JPA
* Validation
* JWT
* BCrypt
* Lombok, si se utiliza

Ejemplo de dependencias JWT:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
```

La versión concreta debe adaptarse a la versión de Spring Boot/JJWT utilizada por el proyecto.

---

# 4. Modelo de usuario

El usuario debe almacenar como mínimo:

```text
User
├── id
├── username
├── email
├── passwordHash
├── enabled
├── accountLocked
├── accountExpired
├── credentialsExpired
└── roles
```

Ejemplo conceptual:

```java
@Entity
@Table(name = "security_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private boolean enabled = true;

    private boolean accountLocked = false;

    private boolean accountExpired = false;

    private boolean credentialsExpired = false;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}
```

La contraseña **nunca debe almacenarse en texto plano**.

---

# 5. Roles

Los roles se almacenan utilizando el prefijo `ROLE_`.

Ejemplo:

```text
ROLE_USER
ROLE_ADMIN
```

Spring Security permite posteriormente utilizar:

```java
@PreAuthorize("hasRole('USER')")
```

y:

```java
@PreAuthorize("hasRole('ADMIN')")
```

No se debe escribir:

```java
hasRole('ROLE_ADMIN')
```

porque `hasRole()` añade internamente el prefijo `ROLE_`.

Por tanto:

```text
Base de datos       @PreAuthorize

ROLE_USER     →     hasRole('USER')

ROLE_ADMIN    →     hasRole('ADMIN')
```

---

# 6. Login utilizando email

Aunque Spring Security utiliza el concepto de `username`, en esta arquitectura el valor utilizado como username de Spring será el **email**.

Por tanto:

```java
@Override
public String getUsername() {
    return user.getEmail();
}
```

Esto permite que:

```java
Authentication.getName()
```

devuelva el email.

El JWT tendrá el email como `subject`.

---

# 7. CustomUserDetails

`CustomUserDetails` adapta nuestra entidad `User` al modelo que espera Spring Security.

Debe proporcionar:

```java
getUsername()
getPassword()
getAuthorities()
isEnabled()
isAccountNonLocked()
isAccountNonExpired()
isCredentialsNonExpired()
```

La parte más importante es:

```java
@Override
public String getUsername() {
    return user.getEmail();
}
```

y:

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
}
```

---

# 8. CustomUserDetailsService

Este servicio conecta Spring Security con la base de datos.

Conceptualmente:

```java
@Override
@Transactional(readOnly = true)
public UserDetails loadUserByUsername(String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new UsernameNotFoundException(
                            "User not found: " + email
                    )
            );

    List<GrantedAuthority> authorities =
            user.getRoles()
                    .stream()
                    .map(role ->
                            (GrantedAuthority)
                                    new SimpleGrantedAuthority(
                                            role.getCode()
                                    )
                    )
                    .toList();

    return new CustomUserDetails(user, authorities);
}
```

El método se llama `loadUserByUsername()` porque es una interfaz de Spring Security, pero en esta arquitectura recibe un **email**.

---

# 9. PasswordEncoder

Las contraseñas deben almacenarse utilizando BCrypt.

Configuración:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Al registrar un usuario:

```java
user.setPasswordHash(
        passwordEncoder.encode(request.password())
);
```

Nunca:

```java
user.setPasswordHash(request.password());
```

La contraseña original nunca debe guardarse en la base de datos.

---

# 10. AuthenticationProvider

Spring Security utiliza un `DaoAuthenticationProvider` para comprobar:

```text
email + password
       │
       ▼
UserDetailsService
       │
       ▼
User
       │
       ▼
PasswordEncoder
       │
       ▼
Authentication
```

Configuración:

```java
@Bean
public AuthenticationProvider authenticationProvider() {

    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

    provider.setPasswordEncoder(passwordEncoder());

    return provider;
}
```

---

# 11. AuthenticationManager

El `AuthenticationManager` es utilizado durante el login.

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) {

    return config.getAuthenticationManager();
}
```

El servicio de autenticación puede hacer:

```java
Authentication authentication =
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );
```

Si las credenciales son correctas, se genera el JWT.

---

# 12. AuthService

La responsabilidad del servicio de autenticación es comprobar las credenciales y generar el token.

Ejemplo:

```java
public String login(String email, String password) {

    Authentication authentication =
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );

    return jwtService.generateToken(
            authentication.getName()
    );
}
```

El controller no debe realizar directamente estas operaciones.

---

# 13. JWT

El JWT representa la autenticación del usuario después del login.

Conceptualmente:

```text
HEADER
   +
PAYLOAD
   +
SIGNATURE
   =
JWT
```

El `subject` contiene el email:

```java
.subject(email)
```

El token debe tener una fecha de expiración.

Ejemplo:

```java
return Jwts.builder()
        .subject(username)
        .issuedAt(new Date())
        .expiration(
                new Date(
                        System.currentTimeMillis()
                        + expirationTime
                )
        )
        .signWith(getSigningKey())
        .compact();
```

---

# 14. Secret del JWT

El secreto utilizado para firmar el JWT debe estar fuera del código fuente.

Ejemplo:

```properties
app.security.secret=${JWT_SECRET}
```

No se debe hacer:

```java
private String secret = "mi-clave-super-secreta";
```

en código fuente.

La clave debe tener suficiente longitud para el algoritmo utilizado.

En producción:

* variable de entorno
* secret manager
* Docker secret
* Kubernetes Secret
* Vault
* etc.

---

# 15. JwtAuthenticationFilter

El filtro intercepta las peticiones HTTP.

Busca:

```http
Authorization: Bearer <token>
```

Si no existe token:

```text
seguir cadena
```

Si existe:

```text
Bearer JWT
    │
    ▼
extraer JWT
    │
    ▼
extraer email
    │
    ▼
buscar usuario
    │
    ▼
validar JWT
    │
    ▼
crear Authentication
    │
    ▼
SecurityContextHolder
```

Ejemplo:

```java
String authHeader =
        request.getHeader("Authorization");

if (authHeader == null ||
        !authHeader.startsWith("Bearer ")) {

    filterChain.doFilter(request, response);
    return;
}
```

Después:

```java
String token = authHeader.substring(7);

String email = jwtService.extractUsername(token);

UserDetails userDetails =
        userDetailsService.loadUserByUsername(email);
```

Si el token es válido:

```java
UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

SecurityContextHolder
        .getContext()
        .setAuthentication(authToken);
```

---

# 16. ¿De dónde salen los roles?

En esta arquitectura, los roles se cargan desde la base de datos.

```text
JWT
 │
 └── email
       │
       ▼
UserDetailsService
       │
       ▼
Database
       │
       ▼
ROLE_USER / ROLE_ADMIN
```

Esto significa que no es necesario confiar en un claim `roles` del JWT para construir las authorities.

Una ventaja es que los cambios de roles en la base de datos se reflejan en las siguientes peticiones.

---

# 17. SecurityConfig

La configuración central debe:

* desactivar CSRF para una API stateless si corresponde al modelo de despliegue
* utilizar sesiones stateless
* permitir login
* permitir registro
* proteger el resto
* registrar el AuthenticationProvider
* registrar el filtro JWT
* activar method security

Ejemplo:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .authenticationProvider(
                        authenticationProvider()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }
}
```

---

# 18. Endpoints públicos

Solo deben declararse explícitamente como públicos los endpoints que realmente lo necesiten.

Ejemplo:

```text
POST /api/auth/login
POST /api/auth/register
```

No utilizar:

```java
.requestMatchers("/api/auth/**").permitAll()
```

si existen endpoints administrativos dentro de `/api/auth`.

Por ejemplo:

```text
/api/auth/login       → público
/api/auth/register    → público
/api/auth/users       → ADMIN
/api/auth/roles       → ADMIN
```

Por eso se deben especificar individualmente.

---

# 19. Registro público

El registro público debe crear únicamente usuarios normales.

DTO:

```java
public record RegisterRequest(

        @NotBlank(message = "El username es obligatorio")
        @Size(
                min = 3,
                max = 30,
                message = "El username debe tener entre 3 y 30 caracteres"
        )
        String username,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(
                min = 6,
                message = "La contraseña debe tener al menos 6 caracteres"
        )
        String password
) {}
```

Importante:

**El DTO público no debe aceptar roles.**

No hacer:

```java
Set<String> rolesCodes
```

en el registro público.

El servidor asigna:

```text
ROLE_USER
```

automáticamente.

---

# 20. Controller de registro

El controller debe ser pequeño:

```java
@PostMapping("/register")
public ResponseEntity<MessageResponse> registerUser(
        @Valid @RequestBody RegisterRequest request) {

    userService.registerUser(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                    new MessageResponse(
                            "Usuario registrado correctamente."
                    )
            );
}
```

El controller no debería:

* acceder directamente al repository
* buscar roles
* cifrar contraseñas
* crear entidades
* realizar comprobaciones de negocio
* guardar usuarios

Estas responsabilidades pertenecen al service.

---

# 21. Registro administrativo de usuarios

Debe existir una operación diferente para los administradores.

```text
POST /api/auth/users
```

Protegida con:

```java
@PreAuthorize("hasRole('ADMIN')")
```

Este endpoint sí puede aceptar roles.

Por ejemplo:

```json
{
    "username": "usuario",
    "email": "usuario@test.com",
    "password": "123456",
    "rolesCodes": [
        "ROLE_USER"
    ]
}
```

La diferencia fundamental es:

```text
/register
    ↓
Público
    ↓
Siempre ROLE_USER


/users
    ↓
ADMIN
    ↓
Puede asignar roles
```

---

# 22. Creación de roles

La creación de roles también debe estar protegida:

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/roles")
```

Ejemplo:

```json
{
    "code": "ROLE_EDITOR",
    "description": "Editor de contenido"
}
```

No debe ser una operación pública.

---

# 23. Autorización mediante @PreAuthorize

Para proteger métodos se utiliza:

```java
@PreAuthorize("hasRole('ADMIN')")
```

o:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
```

Ejemplo de lectura:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@GetMapping
public ResponseEntity<?> getAll() {
    ...
}
```

Ejemplo de escritura:

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<?> create(...) {
    ...
}
```

---

# 24. Política de autorización recomendada

| Operación          | Público | USER | ADMIN |
| ------------------ | ------: | ---: | ----: |
| Login              |       ✅ |    — |     — |
| Registro propio    |       ✅ |    — |     — |
| Consultar recursos |       ❌ |    ✅ |     ✅ |
| Crear recursos     |       ❌ |    ❌ |     ✅ |
| Modificar recursos |       ❌ |    ❌ |     ✅ |
| Eliminar recursos  |       ❌ |    ❌ |     ✅ |
| Asociar recursos   |       ❌ |    ❌ |     ✅ |
| Crear usuarios     |       ❌ |    ❌ |     ✅ |
| Crear roles        |       ❌ |    ❌ |     ✅ |

Esta política debe adaptarse a los requisitos de cada proyecto.

---

# 25. Validación de peticiones

Los DTO de entrada deben utilizar Jakarta Validation.

Ejemplo:

```java
@NotBlank
private String username;
```

```java
@Email
private String email;
```

```java
@Size(min = 6)
private String password;
```

Y el controller:

```java
@Valid @RequestBody RegisterRequest request
```

La validación debe realizarse sobre los DTO, no depender exclusivamente de las validaciones de las entidades JPA.

---

# 26. Login validado

El login también debe validarse.

Ejemplo:

```java
public record LoginRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
```

Controller:

```java
@PostMapping("/login")
public LoginResponse login(
        @Valid @RequestBody LoginRequest request) {

    String token = authService.login(
            request.email(),
            request.password()
    );

    return new LoginResponse(token);
}
```

---

# 27. Manejo global de errores

Para evitar repetir `try/catch` en todos los controllers se utiliza:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    ...
}
```

La idea es centralizar errores como:

```text
400 Bad Request
404 Not Found
409 Conflict
500 Internal Server Error
```

---

# 28. Errores de validación

Para:

```java
@Valid
```

se debe manejar:

```java
MethodArgumentNotValidException
```

Ejemplo:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<?> handleValidationErrors(
        MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                    errors.put(
                            error.getField(),
                            error.getDefaultMessage()
                    )
            );

    return ResponseEntity
            .badRequest()
            .body(errors);
}
```

De esta forma se pueden devolver múltiples errores en una misma respuesta.

---

# 29. JSON mal formado

Un JSON incorrecto puede provocar:

```java
HttpMessageNotReadableException
```

Debe manejarse como:

```text
400 Bad Request
```

Ejemplo:

```json
{
    "username": "pepe"
    "email": "pepe@test.com"
}
```

El JSON anterior es inválido porque falta una coma.

---

# 30. Errores de seguridad

Los errores de validación HTTP y los errores de Spring Security ocurren en capas diferentes.

Por ejemplo:

```text
JSON inválido
    ↓
Controller
    ↓
@RestControllerAdvice
```

Pero:

```text
JWT inválido
    ↓
Security Filter Chain
```

Por ello, un `@RestControllerAdvice` no debe utilizarse como mecanismo principal para manejar `401 Unauthorized` y `403 Forbidden`.

Si se necesita un formato JSON personalizado para estos casos, se pueden configurar:

```java
AuthenticationEntryPoint
```

para `401`.

Y:

```java
AccessDeniedHandler
```

para `403`.

---

# 31. Diferencia entre 401 y 403

### 401 Unauthorized

El cliente no está autenticado.

Ejemplos:

```text
No JWT
JWT inválido
JWT expirado
```

### 403 Forbidden

El usuario está autenticado pero no tiene permisos.

Ejemplo:

```text
USER intenta:

POST /tags
```

El usuario tiene:

```text
ROLE_USER
```

pero se requiere:

```text
ROLE_ADMIN
```

Resultado:

```text
403 Forbidden
```

---

# 32. DataInitializer

En un proyecto que requiere un administrador inicial puede utilizarse un `CommandLineRunner`.

Debe crear:

```text
ROLE_ADMIN
ROLE_USER
```

y un usuario administrador inicial.

Ejemplo conceptual:

```text
ROLE_ADMIN
    │
    └── admin@test.com

ROLE_USER
    │
    └── user@test.com
```

La contraseña inicial debe almacenarse utilizando:

```java
passwordEncoder.encode(password)
```

No guardar nunca:

```text
123456
```

directamente en la columna de contraseña.

En proyectos reales, las credenciales iniciales deben externalizarse.

---

# 33. Estructura recomendada de paquetes

Una estructura posible:

```text
com.example
│
├── config
│   └── DataInitializer.java
│
├── controller
│   ├── TagController.java
│   ├── TutorialController.java
│   └── security
│       └── AuthController.java
│
├── domain
│   ├── entity
│   └── dto
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   └── dto
│       └── ErrorResponse.java
│
├── repository
│
├── service
│
└── security
    ├── config
    │   └── SecurityConfig.java
    │
    ├── jwt
    │   ├── JwtService.java
    │   └── JwtAuthenticationFilter.java
    │
    ├── model
    │   ├── User.java
    │   └── Role.java
    │
    ├── repository
    │   ├── UserRepository.java
    │   └── RoleRepository.java
    │
    ├── service
    │   ├── AuthService.java
    │   ├── UserService.java
    │   └── CustomUserDetailsService.java
    │
    ├── identity
    │   └── CustomUserDetails.java
    │
    └── payload
        ├── request
        │   ├── LoginRequest.java
        │   ├── RegisterRequest.java
        │   ├── UserCreateRequest.java
        │   └── RoleCreateRequest.java
        │
        └── response
            └── MessageResponse.java
```

La estructura exacta puede cambiar según el proyecto.

---

# 34. application.properties

Ejemplo:

```properties
spring.application.name=my-application

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/my_database
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.security.enabled=true
app.security.secret=${JWT_SECRET}
```

En producción no deben incluirse secretos reales dentro del repositorio.

---

# 35. Ejemplo de login

Request:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
    "email": "admin@test.com",
    "password": "123456"
}
```

Respuesta:

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

# 36. Utilizar el JWT

En las siguientes peticiones:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Ejemplo:

```http
GET /tags
Authorization: Bearer <JWT>
```

---

# 37. Ejemplo de autorización

### USER

Puede:

```http
GET /tags
GET /tutorials
GET /tutorials/1
```

No puede:

```http
POST /tags
PUT /tags/1
DELETE /tags/1
POST /tutorials
DELETE /tutorials/1
```

### ADMIN

Puede realizar ambas categorías.

---

# 38. Flujo completo de registro

```text
POST /api/auth/register
        │
        ▼
@Valid
        │
        ├── Error → 400
        │
        ▼
UserService
        │
        ├── username duplicado
        │
        ├── email duplicado
        │
        ▼
Buscar ROLE_USER
        │
        ▼
Codificar contraseña
        │
        ▼
Crear User
        │
        ▼
Asignar ROLE_USER
        │
        ▼
Guardar
        │
        ▼
201 Created
```

El usuario no obtiene privilegios administrativos mediante el registro público.

---

# 39. Flujo completo de login

```text
POST /api/auth/login
        │
        ▼
Validar DTO
        │
        ▼
AuthenticationManager
        │
        ▼
DaoAuthenticationProvider
        │
        ▼
CustomUserDetailsService
        │
        ▼
Buscar por email
        │
        ▼
Comparar BCrypt
        │
        ▼
Authentication OK
        │
        ▼
JwtService
        │
        ▼
JWT
        │
        ▼
200 OK
```

---

# 40. Flujo completo de una petición protegida

```text
GET /tutorials
Authorization: Bearer JWT
             │
             ▼
JwtAuthenticationFilter
             │
             ▼
Extraer email
             │
             ▼
Validar JWT
             │
             ▼
Cargar usuario
             │
             ▼
Cargar authorities
             │
             ▼
SecurityContext
             │
             ▼
@PreAuthorize
             │
             ▼
TutorialController
             │
             ▼
TutorialService
             │
             ▼
Repository
```

---

# 41. Separación de responsabilidades

Una regla importante:

## Controller

Se encarga de:

```text
HTTP
DTO
Validación
Status codes
Delegar al Service
```

No debería encargarse de:

```text
Persistencia
BCrypt
Reglas de negocio
Asignación de roles
Creación compleja de entidades
```

---

## Service

Se encarga de:

```text
Reglas de negocio
Comprobaciones
Creación de entidades
Asignación de roles
Transacciones
Coordinación de repositories
```

---

## Repository

Se encarga de:

```text
Acceso a datos
Queries
Persistencia
```

---

## Security

Se encarga de:

```text
Autenticación
JWT
Authorities
PasswordEncoder
SecurityContext
Autorización
```

---

# 42. Regla importante sobre roles

Nunca confiar en el rol enviado por un usuario público.

Incorrecto:

```json
{
    "username": "pepe",
    "email": "pepe@test.com",
    "password": "123456",
    "role": "ROLE_ADMIN"
}
```

Correcto:

```json
{
    "username": "pepe",
    "email": "pepe@test.com",
    "password": "123456"
}
```

Y el servidor decide:

```text
ROLE_USER
```

La asignación de privilegios debe estar controlada por el servidor.

---

# 43. Checklist reutilizable

Antes de considerar terminada la seguridad de un proyecto:

## Configuración

* [ ] Spring Security configurado
* [ ] `@EnableMethodSecurity`
* [ ] `SessionCreationPolicy.STATELESS`
* [ ] CSRF revisado según arquitectura
* [ ] `AuthenticationManager`
* [ ] `AuthenticationProvider`
* [ ] BCrypt configurado

## Usuarios

* [ ] Usuario implementa `UserDetails` mediante adaptador
* [ ] Login mediante email si es el requisito
* [ ] Password almacenada con BCrypt
* [ ] `UserDetailsService` conectado a BD
* [ ] Usuario habilitado/deshabilitado correctamente

## JWT

* [ ] Secret externalizado
* [ ] Secret suficientemente largo
* [ ] JWT firmado
* [ ] JWT con expiración
* [ ] Subject configurado
* [ ] JWT validado en cada petición
* [ ] Usuario cargado desde BD
* [ ] Authorities disponibles en `SecurityContext`

## Endpoints

* [ ] Login público
* [ ] Register público
* [ ] No utilizar `/api/auth/**` completo como público
* [ ] GET protegido
* [ ] POST protegido
* [ ] PUT protegido
* [ ] PATCH protegido
* [ ] DELETE protegido
* [ ] Operaciones administrativas protegidas

## Roles

* [ ] `ROLE_USER`
* [ ] `ROLE_ADMIN`
* [ ] `hasRole('USER')`
* [ ] `hasRole('ADMIN')`
* [ ] Usuarios públicos no pueden elegir roles
* [ ] Solo ADMIN puede crear usuarios privilegiados
* [ ] Solo ADMIN puede crear roles

## Validación

* [ ] DTOs con `@NotBlank`
* [ ] `@Email`
* [ ] `@Size`
* [ ] Controllers utilizan `@Valid`
* [ ] Errores de validación devueltos como JSON
* [ ] JSON mal formado controlado
* [ ] Parámetros inválidos controlados

## Excepciones

* [ ] `@RestControllerAdvice`
* [ ] `MethodArgumentNotValidException`
* [ ] `HttpMessageNotReadableException`
* [ ] `MethodArgumentTypeMismatchException`
* [ ] `ResourceNotFoundException`
* [ ] `DataIntegrityViolationException`
* [ ] Excepciones inesperadas → 500
* [ ] Revisar `AuthenticationEntryPoint` para 401 si se necesita JSON personalizado
* [ ] Revisar `AccessDeniedHandler` para 403 si se necesita JSON personalizado

## Datos iniciales

* [ ] `ROLE_USER`
* [ ] `ROLE_ADMIN`
* [ ] Usuario ADMIN inicial
* [ ] Contraseña inicial cifrada
* [ ] Secretos fuera del código fuente

---

# 44. Pruebas recomendadas

Una vez terminada la implementación, probar como mínimo:

### Sin autenticación

```text
GET /tags
GET /tutorials
```

Debe devolver:

```text
401 Unauthorized
```

### USER autenticado

```text
GET /tags
GET /tutorials
```

Debe funcionar.

Pero:

```text
POST /tags
DELETE /tags/1
POST /tutorials
DELETE /tutorials/1
```

debe devolver:

```text
403 Forbidden
```

### ADMIN autenticado

Debe poder:

```text
GET
POST
PUT
PATCH
DELETE
```

en las operaciones permitidas.

### Registro

Probar:

```text
POST /api/auth/register
```

y comprobar que el usuario recibe:

```text
ROLE_USER
```

aunque el cliente intente enviar roles.

### Login

Probar:

```text
POST /api/auth/login
```

con:

* email correcto
* password correcta
* email inexistente
* password incorrecta
* email vacío
* email inválido
* password vacía

---

# 45. Regla de oro de esta arquitectura

La seguridad debe funcionar en capas:

```text
                 HTTP
                  │
                  ▼
          Security Filter Chain
                  │
                  ▼
             JWT Filter
                  │
                  ▼
          Authentication
                  │
                  ▼
          SecurityContext
                  │
                  ▼
           @PreAuthorize
                  │
                  ▼
             Controller
                  │
                  ▼
               Service
                  │
                  ▼
             Repository
                  │
                  ▼
              Database
```

Cada capa tiene una responsabilidad concreta.

No conviene solucionar toda la seguridad dentro del controller.

---

# 46. Resumen para reutilizar en un nuevo proyecto

Cuando se cree un nuevo proyecto:

### Paso 1

Crear:

```text
User
Role
UserRepository
RoleRepository
```

### Paso 2

Crear:

```text
CustomUserDetails
CustomUserDetailsService
```

### Paso 3

Configurar:

```text
PasswordEncoder
AuthenticationProvider
AuthenticationManager
```

### Paso 4

Implementar:

```text
JwtService
JwtAuthenticationFilter
```

### Paso 5

Configurar:

```text
SecurityFilterChain
@EnableMethodSecurity
```

### Paso 6

Crear:

```text
LoginRequest
RegisterRequest
```

### Paso 7

Crear:

```text
AuthService
UserService
```

### Paso 8

Implementar:

```text
POST /api/auth/login
POST /api/auth/register
```

### Paso 9

Proteger endpoints con:

```java
@PreAuthorize(...)
```

### Paso 10

Configurar:

```text
@RestControllerAdvice
```

### Paso 11

Crear los roles iniciales.

### Paso 12

Crear un administrador inicial.

### Paso 13

Probar:

```text
401
403
200
201
400
404
409
```

### Paso 14

Crear tests unitarios e integración.

---

# 47. Concepto fundamental

La diferencia más importante que hay que recordar es:

```text
AUTENTICACIÓN
     │
     └── ¿Quién eres?
              │
              ▼
            JWT
```

frente a:

```text
AUTORIZACIÓN
     │
     └── ¿Qué puedes hacer?
              │
              ▼
         ROLE_USER
         ROLE_ADMIN
```

Spring Security utiliza ambas cosas conjuntamente:

```text
JWT válido
    +
ROLE adecuado
    =
Acceso permitido
```

Pero tener un JWT válido **no significa automáticamente tener permisos de administrador**.

---

# 48. Arquitectura final

La solución completa queda conceptualmente así:

```text
                         ┌───────────────┐
                         │    CLIENT     │
                         └───────┬───────┘
                                 │
                     email/password o JWT
                                 │
                                 ▼
                    ┌─────────────────────┐
                    │  Spring Security    │
                    │                     │
                    │ Authentication      │
                    │ Authorization       │
                    └──────────┬──────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
                ▼                             ▼
          Authentication                 JWT Filter
          Manager                            │
                │                            │
                ▼                            ▼
        UserDetailsService             JwtService
                │                            │
                └────────────┬───────────────┘
                             │
                             ▼
                         SecurityContext
                             │
                             ▼
                        @PreAuthorize
                             │
                             ▼
                         Controller
                             │
                             ▼
                           Service
                             │
                             ▼
                         Repository
                             │
                             ▼
                         Database
```

Esta estructura permite mantener separadas autenticación, autorización, lógica de negocio y persistencia, y puede utilizarse como base para futuros proyectos Spring Boot.

| Capa                        | Responsabilidad                        |
| --------------------------- | -------------------------------------- |
| `SecurityFilterChain`       | Decide qué peticiones están protegidas |
| `JwtAuthenticationFilter`   | Extrae y valida el JWT                 |
| `JwtService`                | Crea/lee JWT                           |
| `AuthenticationManager`     | Coordina la autenticación              |
| `DaoAuthenticationProvider` | Comprueba usuario/contraseña           |
| `CustomUserDetailsService`  | Busca usuario en BD                    |
| `PasswordEncoder`           | Comprueba BCrypt                       |
| `SecurityContext`           | Guarda quién está autenticado          |
| `@PreAuthorize`             | Comprueba permisos                     |
| `Controller`                | HTTP + DTO + delegación                |
| `Service`                   | Lógica de negocio                      |
| `Repository`                | Base de datos                          |

