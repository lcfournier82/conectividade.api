package br.dev.fournier.conectividade.controller;

import br.dev.fournier.conectividade.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para demonstrar e testar as operações do RedisService.
 * Fornece endpoints para gravar e ler chaves de um servidor Redis.
 */
@Tag(name = "Redis", description = "Endpoints para manipulação de chaves no Redis")
@RestController
@RequestMapping("/api/redis")
public class RedisController {

    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

    private final RedisService redisService;

    /**
     * Construtor para injeção de dependência do RedisService.
     * @param redisService O serviço que encapsula a lógica de negócio com o Redis.
     */
    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * Endpoint para gravar um par de chave-valor no Redis.
     * Espera uma requisição POST com um corpo JSON contendo "chave" e "valor".
     *
     * Exemplo de corpo da requisição (JSON):
     * {
     * "chave": "usuario:1:nome",
     * "valor": "Ana"
     * }
     *
     * @param payload O corpo da requisição contendo a chave e o valor.
     * @return Uma resposta HTTP 200 OK com uma mensagem de sucesso.
     */
    @Operation(summary = "Grava uma chave e valor no Redis",
            description = "Recebe um JSON com uma chave e um valor e os armazena no Redis usando o comando SET.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave gravada com sucesso",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Requisição inválida por falta de 'chave' ou 'valor' no corpo",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/gravar")
    public ResponseEntity<String> gravar(@RequestBody Map<String, String> payload) {
        String chave = payload.get("chave");
        String valor = payload.get("valor");

        if (chave == null || valor == null) {
            logger.error("Tentativa de gravação com payload inválido. Chave ou valor nulos.");
            return ResponseEntity.badRequest().body("A 'chave' e o 'valor' não podem ser nulos.");
        }

        logger.info("Recebida requisição para gravar a chave: '{}'", chave);
        redisService.gravarChave(chave, valor);

        String mensagem = String.format("Chave '%s' gravada com sucesso!", chave);
        logger.info("Chave '{}' gravada com sucesso no Redis.", chave);

        return ResponseEntity.ok(mensagem);
    }

    /**
     * Endpoint para ler um valor do Redis usando uma chave.
     * A chave é passada como uma variável no caminho (path variable) da URL.
     *
     * Exemplo de chamada via cURL:
     * curl http://localhost:8080/api/redis/ler/usuario:1:nome
     *
     * @param chave A chave a ser buscada no Redis.
     * @return Uma resposta HTTP 200 OK com o valor encontrado, ou uma resposta HTTP 404 Not Found
     * se a chave não existir.
     */
    @Operation(summary = "Lê uma chave do Redis",
            description = "Busca e retorna o valor associado a uma chave específica no Redis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valor encontrado para a chave",
                    content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada no Redis",
                    content = @Content)
    })
    @GetMapping("/ler/{chave}")
    public ResponseEntity<String> ler(@PathVariable String chave) {
        logger.info("Recebida requisição para ler a chave: '{}'", chave);

        return redisService.lerChave(chave)
                .map(valor -> {
                    logger.info("Chave '{}' encontrada com sucesso.", chave);
                    return ResponseEntity.ok(valor);
                })
                .orElseGet(() -> {
                    // 4. Log de AVISO
                    logger.warn("Chave '{}' não foi encontrada no Redis.", chave);
                    return ResponseEntity.notFound().build();
                });    }
}