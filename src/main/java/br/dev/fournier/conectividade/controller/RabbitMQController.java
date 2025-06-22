package br.dev.fournier.conectividade.controller;

import br.dev.fournier.conectividade.RabbitMQConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "RabbitMQ", description = "Endpoints para manipulação de filas no RabbitMQ")
@RestController
@RequestMapping("/rabbitmq")
public class RabbitMQController {
    private static final Logger logger = LoggerFactory.getLogger(RedisController.class);

    private final RabbitTemplate rabbitTemplate; // Usado para enviar mensagens
    private static int messageCounter = 0; // Contador simples para mensagens

    @Autowired
    public RabbitMQController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Endpoint para enviar uma mensagem para a fila RabbitMQ.
     * Exemplo: POST http://localhost:8080/rabbitmq/send
     * Body: { "message": "Minha primeira mensagem!" }
     */
    @Operation(summary = "Envia uma mensagem para o RabbitMQ",
            description = "Recebe um JSON ou um Texto e envia para fila")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensagem enviada com sucesso",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Requisição inválida por falta de 'chave' ou 'valor' no corpo",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody String message) {
        // Envia a mensagem para a fila definida em RabbitMQConfig.QUEUE_NAME
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        messageCounter++;
        logger.info(" [x] Enviado '" + message + "' para a fila: " + RabbitMQConfig.QUEUE_NAME);
        return ResponseEntity.ok("Mensagem enviada com sucesso: " + message);
    }

    /**
     * Endpoint para ler uma mensagem da fila RabbitMQ.
     * Nota: O 'receive' bloqueia até que uma mensagem esteja disponível.
     * Para consumo contínuo e assíncrono, veja o @RabbitListener abaixo.
     * Exemplo: GET http://localhost:8080/rabbitmq/read
     */
    @GetMapping("/read")
    public ResponseEntity<String> readMessage() {
        // Lê e remove uma mensagem da fila. Retorna null se não houver mensagens.
        Object message = rabbitTemplate.receiveAndConvert(RabbitMQConfig.QUEUE_NAME);
        if (message != null) {
            String receivedMessage = (String) message;
            logger.info(" [x] Lido '" + receivedMessage + "' da fila: " + RabbitMQConfig.QUEUE_NAME);
            return ResponseEntity.ok("Mensagem lida: " + receivedMessage);
        } else {
            logger.warn(" [x] Nenhuma mensagem na fila: " + RabbitMQConfig.QUEUE_NAME);
            return ResponseEntity.noContent().build(); // Retorna 204 No Content
        }
    }

    /**
     * Exemplo de Listener de Mensagens RabbitMQ:
     * Este método será invocado automaticamente quando uma mensagem for recebida na fila.
     * Não precisa ser chamado via endpoint REST. Ele age como um consumidor assíncrono.
     *
     * Descomente para ativar este listener.
     */
    // @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    // public void receiveMessageFromQueue(String message) {
    //     System.out.println(" [x] Recebido (via Listener) '" + message + "' da fila: " + RabbitMQConfig.QUEUE_NAME);
    //     // Aqui você pode adicionar a lógica de processamento da mensagem
    //     // Por padrão, o Spring AMQP envia um ACK automático após o método ser concluído com sucesso.
    // }
}
