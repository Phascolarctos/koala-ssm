package top.monkeyfans.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import top.monkeyfans.engine.Bid;
import top.monkeyfans.engine.state.Events;
import top.monkeyfans.engine.state.States;

@RestController
@RequestMapping("/api/v1/auction")
public class BidController {
    private final StateMachineService<States, Events> statesEventsStateMachineService;

    public BidController(StateMachineService<States, Events> statesEventsStateMachineService) {
        this.statesEventsStateMachineService = statesEventsStateMachineService;
    }

    @PostMapping("/place-bid")
    public Mono<ResponseEntity<String>> placeBid(@RequestBody Bid bid) {
        return Mono.fromCallable(() -> {
                    return statesEventsStateMachineService.acquireStateMachine(bid.getAuctionId());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(stateMachine -> {
                    Message<Events> message = MessageBuilder
                            .withPayload(Events.NEW_BID)
                            .setHeader("Bid", bid)
                            .build();

                    return stateMachine.sendEvent(Mono.just(message))
                            .collectList()
                            .map(results -> {
                                boolean accepted = results.stream()
                                        .anyMatch(r -> r.getResultType() == StateMachineEventResult.ResultType.ACCEPTED);
                                return accepted ? ResponseEntity.ok("成功") : ResponseEntity.badRequest().body("失败");
                            })
                            .doFinally(signalType -> statesEventsStateMachineService.releaseStateMachine(bid.getAuctionId()));
                });
    }
}
