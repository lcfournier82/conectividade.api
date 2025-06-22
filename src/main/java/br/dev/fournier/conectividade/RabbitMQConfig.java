package br.dev.fournier.conectividade;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "filaTeste"; // Define o nome da fila

    @Bean
    public Queue myQueue() {
        // Declara a fila. O 'durable(true)' faz com que a fila persista em caso de reinício do RabbitMQ.
        return new Queue(QUEUE_NAME, true);
    }
}
