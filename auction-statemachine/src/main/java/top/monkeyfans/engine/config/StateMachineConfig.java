package top.monkeyfans.engine.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.state.State;
import top.monkeyfans.engine.Bid;
import top.monkeyfans.engine.state.Events;
import top.monkeyfans.engine.state.States;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {
    @Autowired
    private StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister;

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withPersistence()
                .runtimePersister(stateMachineRuntimePersister)
                .and()
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states
                .withStates()
                .initial(States.CREATED)
                .state(States.ONGOING)
                .end(States.SUCCEEDED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
                .withExternal()
                .source(States.CREATED)
                .target(States.ONGOING)
                .event(Events.START_AUCTION)
                .and()
                .withExternal()
                .source(States.CREATED)
                .target(States.ONGOING)
                .event(Events.NEW_BID)
                .and()
                .withExternal()
                .source(States.ONGOING)
                .target(States.ONGOING)
                .event(Events.NEW_BID)
                .action(bidAction());
    }

    @Bean
    public Action<States, Events> bidAction() {
        return context -> {
            Bid bid = context.getMessageHeaders().get("Bid", Bid.class);
            if (bid != null) {
                System.out.println("收到竞价：" + bid.getPrice());
            }
        };
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                System.out.println("State changed from " + from + " to " + to);
                // 记录日志 手动实现log向采集器发送，或者通过log打印捕捉
            }
        };
    }
}
