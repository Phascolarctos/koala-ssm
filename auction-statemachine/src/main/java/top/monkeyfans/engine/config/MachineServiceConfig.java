package top.monkeyfans.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.data.redis.RedisPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.redis.RedisRepositoryStateMachine;
import org.springframework.statemachine.data.redis.RedisStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;
import top.monkeyfans.engine.state.Events;
import top.monkeyfans.engine.state.States;

@Configuration
public class MachineServiceConfig {
    @Bean
    public RedisRepositoryStateMachine redisRepositoryStateMachine() {
        return new RedisRepositoryStateMachine();
    }

    @Bean
    public StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister(RedisStateMachineRepository redisStateMachineRepository) {
        return new RedisPersistingStateMachineInterceptor<>(redisStateMachineRepository);
    }

    @Bean
    public StateMachineService<States, Events> stateMachineService(StateMachineFactory<States, Events> stateMachineFactory, StateMachinePersist<States, Events, String> stateMachinePersist) {
        return new DefaultStateMachineService<>(stateMachineFactory, stateMachinePersist);
    }
}
