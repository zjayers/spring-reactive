package io.ayers.springreactive.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reactive")
public class FluxAndMonoController {
    @GetMapping("/flux")
    public Flux<Integer> returnIntegerFluxStream() {
        return Flux.just(1, 2, 3, 4, 5, 6, 7);
    }

    @GetMapping("/mono")
    public Mono<Integer> returnMono() {
        return Mono.just(1);
    }
}
