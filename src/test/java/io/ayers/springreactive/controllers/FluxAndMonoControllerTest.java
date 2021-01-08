package io.ayers.springreactive.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest
class FluxAndMonoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void returnIntegerFluxStream() {
        var responseBody = webTestClient.get()
                                        .uri("/reactive")
                                        .accept(MediaType.APPLICATION_JSON)
                                        .exchange()
                                        .expectStatus()
                                        .isOk()
                                        .expectHeader()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .expectBodyList(Integer.class)
                                        .returnResult()
                                        .getResponseBody();

        assertEquals(responseBody, List.of(1, 2, 3, 4, 5, 6, 7));
    }
}