package br.dev.fournier.conectividade.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Construtor para injeção de dependência do StringRedisTemplate.
     * @param stringRedisTemplate O template injetado pelo Spring para operações com Strings no Redis.
     */
    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Grava um par de chave-valor no Redis.
     * Este método utiliza a operação SET do Redis.
     *
     * @param chave A chave sob a qual o valor será armazenado. Não deve ser nula.
     * @param valor O valor a ser armazenado. Não deve ser nulo.
     */
    public void gravarChave(String chave, String valor) {
        stringRedisTemplate.opsForValue().set(chave, valor);
    }

    /**
     * Lê um valor do Redis com base na chave fornecida.
     * Este método utiliza a operação GET do Redis.
     *
     * @param chave A chave cujo valor se deseja obter. Não deve ser nula.
     * @return um {@link Optional} contendo o valor se a chave existir, ou um Optional vazio caso contrário.
     */
    public Optional<String> lerChave(String chave) {
        String valor = stringRedisTemplate.opsForValue().get(chave);
        return Optional.ofNullable(valor);
    }
}
