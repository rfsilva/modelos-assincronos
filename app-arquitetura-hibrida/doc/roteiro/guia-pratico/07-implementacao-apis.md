# 🌐 ETAPA 07: IMPLEMENTAÇÃO DE APIS REST
## REST Controllers - Exposição de Comandos e Queries

### 🎯 **OBJETIVO DA ETAPA**

Implementar APIs REST seguindo princípios RESTful, expondo comandos (write) e queries (read) através de controllers otimizados, com validações robustas, documentação Swagger/OpenAPI e tratamento de erros padronizado.

**⏱️ Duração Estimada:** 3-5 horas  
**👥 Participantes:** Desenvolvedor + Tech Lead  
**📋 Pré-requisitos:** Etapa 06 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **🎯 1. ESTRUTURA DE CONTROLLERS**

#### **📦 Organização de Pacotes:**
- [ ] **Pacote controller** criado: `com.seguradora.hibrida.[dominio].controller`
- [ ] **Pacote dto.request** criado: `com.seguradora.hibrida.[dominio].dto.request`
- [ ] **Pacote dto.response** criado: `com.seguradora.hibrida.[dominio].dto.response`
- [ ] **Pacote exception** criado: `com.seguradora.hibrida.[dominio].exception`

#### **🎯 Controller Base para Comandos:**
```java
@RestController
@RequestMapping("/api/v1/[dominio]")
@Validated
@Tag(name = "[Dominio]", description = "APIs para gerenciamento de [dominio]")
@Slf4j
public class [Dominio]CommandController {
    
    private final CommandBus commandBus;
    private final [Dominio]DtoMapper mapper;
    
    // ========== CONSTRUTOR ==========
    public [Dominio]CommandController(CommandBus commandBus, [Dominio]DtoMapper mapper) {
        this.commandBus = commandBus;
        this.mapper = mapper;
    }
    
    // ========== CRIAR ==========
    @PostMapping
    @Operation(summary = "Criar novo [dominio]", description = "Cria um novo [dominio] no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Criado com sucesso",
            content = @Content(schema = @Schema(implementation = [Dominio]Response.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Não autorizado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<[Dominio]Response> criar(
            @Valid @RequestBody Criar[Dominio]Request request,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Recebida requisição para criar [dominio]: {}", request);
        
        // Mapear DTO para comando
        Criar[Dominio]Command command = mapper.toCommand(request, userId);
        
        // Executar comando
        CommandResult result = commandBus.send(command);
        
        // Validar resultado
        if (!result.isSuccess()) {
            throw new CommandExecutionException(result.getError(), result.getErrorCode());
        }
        
        // Construir resposta
        [Dominio]Response response = [Dominio]Response.builder()
            .id(result.getMetadata("aggregateId"))
            .message("Criado com sucesso")
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/[dominio]/" + response.getId())
            .body(response);
    }
    
    // ========== ATUALIZAR ==========
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar [dominio]", description = "Atualiza dados de um [dominio] existente")
    public ResponseEntity<[Dominio]Response> atualizar(
            @PathVariable String id,
            @Valid @RequestBody Atualizar[Dominio]Request request,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Recebida requisição para atualizar [dominio] {}: {}", id, request);
        
        Atualizar[Dominio]Command command = mapper.toCommand(id, request, userId);
        CommandResult result = commandBus.send(command);
        
        if (!result.isSuccess()) {
            throw new CommandExecutionException(result.getError(), result.getErrorCode());
        }
        
        [Dominio]Response response = [Dominio]Response.builder()
            .id(id)
            .message("Atualizado com sucesso")
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    // ========== DELETAR ==========
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar [dominio]", description = "Remove um [dominio] do sistema")
    public ResponseEntity<Void> deletar(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Recebida requisição para deletar [dominio] {}", id);
        
        Deletar[Dominio]Command command = Deletar[Dominio]Command.builder()
            .aggregateId(id)
            .userId(userId)
            .build();
        
        CommandResult result = commandBus.send(command);
        
        if (!result.isSuccess()) {
            throw new CommandExecutionException(result.getError(), result.getErrorCode());
        }
        
        return ResponseEntity.noContent().build();
    }
}
```

#### **📊 Controller Base para Queries:**
```java
@RestController
@RequestMapping("/api/v1/[dominio]")
@Validated
@Tag(name = "[Dominio]", description = "APIs para consulta de [dominio]")
@Slf4j
public class [Dominio]QueryController {
    
    private final [Dominio]QueryService queryService;
    private final [Dominio]DtoMapper mapper;
    
    // ========== CONSTRUTOR ==========
    public [Dominio]QueryController([Dominio]QueryService queryService, [Dominio]DtoMapper mapper) {
        this.queryService = queryService;
        this.mapper = mapper;
    }
    
    // ========== BUSCAR POR ID ==========
    @GetMapping("/{id}")
    @Operation(summary = "Buscar [dominio] por ID", description = "Retorna detalhes de um [dominio] específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Encontrado com sucesso",
            content = @Content(schema = @Schema(implementation = [Dominio]DetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    public ResponseEntity<[Dominio]DetailResponse> buscarPorId(@PathVariable UUID id) {
        log.debug("Buscando [dominio] por ID: {}", id);
        
        return queryService.findById(id)
            .map(mapper::toDetailResponse)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Dominio", id));
    }
    
    // ========== LISTAR COM FILTROS ==========
    @GetMapping
    @Operation(summary = "Listar [dominio]", description = "Lista [dominio] com filtros e paginação")
    public ResponseEntity<Page<[Dominio]ListResponse>> listar(
            @ParameterObject [Dominio]FilterRequest filter,
            @ParameterObject Pageable pageable) {
        
        log.debug("Listando [dominio] com filtros: {}", filter);
        
        Page<[Dominio]ListView> result = queryService.findWithFilters(
            mapper.toFilter(filter),
            pageable
        );
        
        Page<[Dominio]ListResponse> response = result.map(mapper::toListResponse);
        
        return ResponseEntity.ok(response);
    }
    
    // ========== BUSCA TEXTUAL ==========
    @GetMapping("/search")
    @Operation(summary = "Buscar [dominio]", description = "Busca textual em [dominio]")
    public ResponseEntity<Page<[Dominio]ListResponse>> buscar(
            @RequestParam String termo,
            @ParameterObject Pageable pageable) {
        
        log.debug("Buscando [dominio] com termo: {}", termo);
        
        Page<[Dominio]ListView> result = queryService.search(termo, pageable);
        Page<[Dominio]ListResponse> response = result.map(mapper::toListResponse);
        
        return ResponseEntity.ok(response);
    }
    
    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard de [dominio]", description = "Retorna estatísticas e dados agregados")
    public ResponseEntity<[Dominio]DashboardResponse> dashboard() {
        log.debug("Carregando dashboard de [dominio]");
        
        [Dominio]DashboardView dashboard = queryService.getDashboard();
        [Dominio]DashboardResponse response = mapper.toDashboardResponse(dashboard);
        
        return ResponseEntity.ok(response);
    }
}
```

#### **✅ Checklist de Controllers:**
- [ ] **Command controller** implementado
- [ ] **Query controller** implementado
- [ ] **Anotações OpenAPI** completas
- [ ] **Validações** de entrada configuradas
- [ ] **Headers** obrigatórios documentados

---

### **📝 2. DTOS DE REQUEST**

#### **🎯 DTOs de Comando:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para criar [dominio]")
public class Criar[Dominio]Request {
    
    @NotBlank(message = "[Campo1] é obrigatório")
    @Size(max = 255, message = "[Campo1] deve ter no máximo 255 caracteres")
    @Schema(description = "Descrição do campo1", example = "Valor exemplo")
    private String [campo1];
    
    @NotBlank(message = "[Campo2] é obrigatório")
    @Schema(description = "Descrição do campo2", example = "Valor exemplo")
    private String [campo2];
    
    @Valid
    @Schema(description = "Dados adicionais")
    private [ValueObject]Request [valueObject];
    
    @AssertTrue(message = "Dados inconsistentes")
    public boolean isValid() {
        // Validações customizadas
        return true;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para atualizar [dominio]")
public class Atualizar[Dominio]Request {
    
    @Size(max = 255)
    @Schema(description = "[Campo1] para atualização (opcional)")
    private String [campo1];
    
    @Schema(description = "[Campo2] para atualização (opcional)")
    private String [campo2];
    
    @Valid
    private [ValueObject]Request [valueObject];
}
```

#### **🔍 DTOs de Query:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filtros para busca de [dominio]")
public class [Dominio]FilterRequest {
    
    @Schema(description = "Filtro por [campo1]", example = "valor")
    private String [campo1];
    
    @Schema(description = "Filtro por [campo2]", example = "valor")
    private String [campo2];
    
    @Schema(description = "Data de início", example = "2024-01-01T00:00:00Z")
    private Instant dataInicio;
    
    @Schema(description = "Data de fim", example = "2024-12-31T23:59:59Z")
    private Instant dataFim;
    
    @Schema(description = "Tags para filtrar")
    private List<String> tags;
}
```

#### **✅ Checklist de DTOs Request:**
- [ ] **Validações Bean Validation** implementadas
- [ ] **Documentação OpenAPI** completa
- [ ] **Exemplos** fornecidos nos schemas
- [ ] **Validações customizadas** quando necessário
- [ ] **Builders** para facilitar testes

---

### **📊 3. DTOS DE RESPONSE**

#### **📄 DTOs de Resposta:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resposta de operação de comando")
public class [Dominio]Response {
    
    @Schema(description = "ID do [dominio]", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;
    
    @Schema(description = "Mensagem de retorno", example = "Operação realizada com sucesso")
    private String message;
    
    @Schema(description = "Timestamp da operação", example = "2024-03-09T10:00:00Z")
    private Instant timestamp;
    
    @Schema(description = "Metadados adicionais")
    private Map<String, Object> metadata;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Detalhes completos de [dominio]")
public class [Dominio]DetailResponse {
    
    @Schema(description = "ID do [dominio]")
    private UUID id;
    
    @Schema(description = "[Campo1]")
    private String [campo1];
    
    @Schema(description = "[Campo2]")
    private String [campo2];
    
    @Schema(description = "Tags associadas")
    private List<String> tags;
    
    @Schema(description = "Data de criação")
    private Instant createdAt;
    
    @Schema(description = "Data de atualização")
    private Instant updatedAt;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Item de listagem de [dominio]")
public class [Dominio]ListResponse {
    
    @Schema(description = "ID do [dominio]")
    private UUID id;
    
    @Schema(description = "[Campo1]")
    private String [campo1];
    
    @Schema(description = "[Campo2]")
    private String [campo2];
    
    @Schema(description = "Data de criação")
    private Instant createdAt;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Dashboard de [dominio]")
public class [Dominio]DashboardResponse {
    
    @Schema(description = "Total de registros")
    private Long total;
    
    @Schema(description = "Estatísticas por categoria")
    private Map<String, Long> estatisticas;
    
    @Schema(description = "Dados recentes")
    private List<[Dominio]DailyStatsResponse> recentes;
    
    @Schema(description = "Última atualização")
    private Instant ultimaAtualizacao;
}
```

#### **❌ DTO de Erro:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Resposta de erro")
public class ErrorResponse {
    
    @Schema(description = "Código do erro", example = "VALIDATION_ERROR")
    private String code;
    
    @Schema(description = "Mensagem de erro", example = "Dados inválidos")
    private String message;
    
    @Schema(description = "Timestamp do erro", example = "2024-03-09T10:00:00Z")
    private Instant timestamp;
    
    @Schema(description = "Detalhes dos erros de validação")
    private List<ValidationError> errors;
    
    @Schema(description = "Caminho da requisição", example = "/api/v1/dominio")
    private String path;
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Erro de validação")
public class ValidationError {
    
    @Schema(description = "Campo com erro", example = "campo1")
    private String field;
    
    @Schema(description = "Valor rejeitado")
    private Object rejectedValue;
    
    @Schema(description = "Mensagem de erro", example = "Campo obrigatório")
    private String message;
}
```

#### **✅ Checklist de DTOs Response:**
- [ ] **Schemas OpenAPI** completos
- [ ] **Exemplos** fornecidos
- [ ] **Erro padronizado** implementado
- [ ] **Builders** para facilitar criação
- [ ] **Serialização JSON** testada

---

### **🔄 4. MAPPERS**

#### **📐 DTO Mapper:**
```java
@Component
public class [Dominio]DtoMapper {
    
    // ========== REQUEST → COMMAND ==========
    public Criar[Dominio]Command toCommand(Criar[Dominio]Request request, String userId) {
        return Criar[Dominio]Command.builder()
            .aggregateId(UUID.randomUUID().toString())
            .userId(userId)
            .[campo1](request.get[Campo1]())
            .[campo2](request.get[Campo2]())
            .correlationId(UUID.randomUUID())
            .build();
    }
    
    public Atualizar[Dominio]Command toCommand(String id, Atualizar[Dominio]Request request, String userId) {
        return Atualizar[Dominio]Command.builder()
            .aggregateId(id)
            .userId(userId)
            .[campo1](request.get[Campo1]())
            .[campo2](request.get[Campo2]())
            .correlationId(UUID.randomUUID())
            .build();
    }
    
    // ========== REQUEST → FILTER ==========
    public [Dominio]Filter toFilter([Dominio]FilterRequest request) {
        return [Dominio]Filter.builder()
            .[campo1](request.get[Campo1]())
            .[campo2](request.get[Campo2]())
            .dataInicio(request.getDataInicio())
            .dataFim(request.getDataFim())
            .tags(request.getTags())
            .build();
    }
    
    // ========== VIEW → RESPONSE ==========
    public [Dominio]DetailResponse toDetailResponse([Dominio]DetailView view) {
        return [Dominio]DetailResponse.builder()
            .id(view.getId())
            .[campo1](view.get[Campo1]())
            .[campo2](view.get[Campo2]())
            .tags(view.getTags())
            .createdAt(view.getCreatedAt())
            .updatedAt(view.getUpdatedAt())
            .build();
    }
    
    public [Dominio]ListResponse toListResponse([Dominio]ListView view) {
        return [Dominio]ListResponse.builder()
            .id(view.getId())
            .[campo1](view.get[Campo1]())
            .[campo2](view.get[Campo2]())
            .createdAt(view.getCreatedAt())
            .build();
    }
    
    public [Dominio]DashboardResponse toDashboardResponse([Dominio]DashboardView view) {
        return [Dominio]DashboardResponse.builder()
            .total(view.getTotal())
            .estatisticas(view.getEstatisticasPor[Campo1]())
            .recentes(view.getEstatisticasRecentes().stream()
                .map(this::toDailyStatsResponse)
                .collect(Collectors.toList()))
            .ultimaAtualizacao(view.getUltimaAtualizacao())
            .build();
    }
    
    private [Dominio]DailyStatsResponse toDailyStatsResponse([Dominio]DailyStats stats) {
        return [Dominio]DailyStatsResponse.builder()
            .dia(stats.getDia())
            .total(stats.getTotal())
            .media(stats.getMedia())
            .build();
    }
}
```

#### **✅ Checklist de Mappers:**
- [ ] **Conversões Request → Command** implementadas
- [ ] **Conversões View → Response** implementadas
- [ ] **Mapeamento de filtros** funcional
- [ ] **Tratamento de valores nulos** adequado
- [ ] **Testes unitários** dos mappers

---

### **🚨 5. TRATAMENTO DE ERROS**

#### **⚠️ Exception Handler Global:**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // ========== VALIDAÇÃO ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        log.warn("Erro de validação: {}", ex.getMessage());
        
        List<ValidationError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ValidationError.builder()
                .field(error.getField())
                .rejectedValue(error.getRejectedValue())
                .message(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());
        
        return ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Dados de entrada inválidos")
            .timestamp(Instant.now())
            .errors(errors)
            .path(request.getRequestURI())
            .build();
    }
    
    // ========== RECURSO NÃO ENCONTRADO ==========
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        
        return ErrorResponse.builder()
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
    }
    
    // ========== ERRO DE COMANDO ==========
    @ExceptionHandler(CommandExecutionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCommandExecutionException(
            CommandExecutionException ex,
            HttpServletRequest request) {
        
        log.error("Erro ao executar comando: {}", ex.getMessage());
        
        return ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
    }
    
    // ========== VIOLAÇÃO DE REGRA DE NEGÓCIO ==========
    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleBusinessRuleViolationException(
            BusinessRuleViolationException ex,
            HttpServletRequest request) {
        
        log.warn("Violação de regra de negócio: {}", ex.getMessage());
        
        List<ValidationError> errors = ex.getViolations()
            .stream()
            .map(violation -> ValidationError.builder()
                .field("business_rule")
                .message(violation)
                .build())
            .collect(Collectors.toList());
        
        return ErrorResponse.builder()
            .code("BUSINESS_RULE_VIOLATION")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .errors(errors)
            .path(request.getRequestURI())
            .build();
    }
    
    // ========== ERRO GENÉRICO ==========
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        log.error("Erro interno do servidor", ex);
        
        return ErrorResponse.builder()
            .code("INTERNAL_SERVER_ERROR")
            .message("Erro interno do servidor")
            .timestamp(Instant.now())
            .path(request.getRequestURI())
            .build();
    }
}
```

#### **✅ Checklist de Tratamento de Erros:**
- [ ] **Validação** tratada adequadamente
- [ ] **Recurso não encontrado** com status 404
- [ ] **Erros de negócio** com status 422
- [ ] **Erros internos** com status 500 e logs
- [ ] **Resposta padronizada** para todos os erros

---

### **📚 6. DOCUMENTAÇÃO OPENAPI**

#### **⚙️ Configuração do OpenAPI:**
```java
@Configuration
public class OpenApiConfiguration {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API [Dominio]")
                .version("1.0.0")
                .description("APIs para gerenciamento de [dominio] usando arquitetura CQRS/ES")
                .contact(new Contact()
                    .name("Equipe de Desenvolvimento")
                    .email("dev@seguradora.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Desenvolvimento"),
                new Server().url("https://api-homolog.seguradora.com").description("Homologação"),
                new Server().url("https://api.seguradora.com").description("Produção")))
            .addSecurityItem(new SecurityRequirement().addList("X-User-Id"))
            .components(new Components()
                .addSecuritySchemes("X-User-Id",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-User-Id")));
    }
    
    @Bean
    public GroupedOpenApi [dominio]Api() {
        return GroupedOpenApi.builder()
            .group("[dominio]")
            .pathsToMatch("/api/v1/[dominio]/**")
            .build();
    }
}
```

#### **✅ Checklist de Documentação:**
- [ ] **OpenAPI configurado** adequadamente
- [ ] **Informações básicas** completas
- [ ] **Servidores** listados (dev, homolog, prod)
- [ ] **Segurança** documentada
- [ ] **Grupos** de APIs organizados

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **🌐 APIs:**
- [ ] **Command endpoints** implementados e funcionais
- [ ] **Query endpoints** implementados e otimizados
- [ ] **Versionamento** (v1) implementado
- [ ] **Status codes** HTTP corretos
- [ ] **Headers** obrigatórios validados

#### **📝 DTOs:**
- [ ] **Request DTOs** com validações completas
- [ ] **Response DTOs** padronizados
- [ ] **Error DTOs** consistentes
- [ ] **Mappers** funcionais
- [ ] **Documentação OpenAPI** completa

#### **🚨 Tratamento de Erros:**
- [ ] **Exception handler** global implementado
- [ ] **Erros específicos** tratados adequadamente
- [ ] **Logs** estruturados
- [ ] **Respostas consistentes**
- [ ] **Status HTTP** apropriados

#### **📚 Documentação:**
- [ ] **Swagger UI** acessível
- [ ] **Exemplos** fornecidos
- [ ] **Descrições** claras
- [ ] **Schemas** completos
- [ ] **Testes** via Swagger funcionais

#### **🧪 Testes:**
- [ ] **Testes de integração** dos endpoints
- [ ] **Testes de validação** funcionais
- [ ] **Testes de erro** implementados
- [ ] **Testes de mapper** validados
- [ ] **Coverage** adequado (>80%)

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Controllers Acoplados:**
```java
// ❌ EVITAR: Controller com lógica de negócio
@PostMapping
public ResponseEntity criar(Request request) {
    // Lógica de negócio no controller
    if (request.getCampo() == null) {
        // Validação manual
    }
    // Processamento direto
    aggregate.processar();
}

// ✅ PREFERIR: Controller apenas coordena
@PostMapping
public ResponseEntity criar(@Valid Request request, @RequestHeader("X-User-Id") String userId) {
    Command command = mapper.toCommand(request, userId);
    CommandResult result = commandBus.send(command);
    return toResponse(result);
}
```

#### **🚫 Validações Inadequadas:**
```java
// ❌ EVITAR: Validações apenas no controller
@PostMapping
public ResponseEntity criar(Request request) {
    if (request.getCampo() == null) {
        throw new IllegalArgumentException();
    }
}

// ✅ PREFERIR: Bean Validation + validadores customizados
@Data
public class Request {
    @NotBlank(message = "Campo obrigatório")
    @Size(max = 255)
    private String campo;
}
```

#### **🚫 Documentação Incompleta:**
```java
// ❌ EVITAR: Endpoint sem documentação
@GetMapping("/{id}")
public ResponseEntity<Response> buscar(@PathVariable String id) { }

// ✅ PREFERIR: Documentação completa
@GetMapping("/{id}")
@Operation(summary = "Buscar por ID", description = "Detalhes completos")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Sucesso"),
    @ApiResponse(responseCode = "404", description = "Não encontrado")
})
public ResponseEntity<Response> buscar(@PathVariable UUID id) { }
```

### **✅ Boas Práticas:**

#### **🎯 Design de APIs:**
- **Sempre** seguir princípios RESTful
- **Sempre** versionar APIs (v1, v2)
- **Sempre** usar status HTTP corretos
- **Sempre** validar entrada com Bean Validation

#### **📝 DTOs:**
- **Sempre** separar request/response
- **Sempre** usar builders
- **Sempre** documentar com OpenAPI
- **Sempre** fornecer exemplos

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 08 - Testes & Validação](./08-testes-validacao.md)**
2. Implementar testes de integração
3. Testar validações
4. Validar documentação Swagger

### **📋 Preparação para Próxima Etapa:**
- [ ] **Estratégias de teste** revisadas
- [ ] **JUnit 5** e **MockMvc** estudados
- [ ] **Postman** ou **Rest Assured** configurado
- [ ] **Testes de API** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Práticas de Desenvolvimento](../12-praticas-desenvolvimento-README.md)**: Guia de boas práticas
- **Spring REST Docs**: Documentação de APIs
- **OpenAPI/Swagger**: Especificação de APIs

### **🛠️ Ferramentas:**
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Postman**: Testes manuais de APIs
- **RestAssured**: Testes automatizados
- **Insomnia**: Cliente HTTP alternativo

### **🧪 Exemplos:**
- **CommandControllerTest**: Testes de command endpoints
- **QueryControllerTest**: Testes de query endpoints
- **ExceptionHandlerTest**: Testes de tratamento de erros

---

**📋 Checklist Total:** 80+ itens de validação  
**⏱️ Tempo Médio:** 3-5 horas  
**🎯 Resultado:** APIs REST completas e documentadas  
**✅ Próxima Etapa:** Testes & Validação