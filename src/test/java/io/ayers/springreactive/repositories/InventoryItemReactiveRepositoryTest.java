package io.ayers.springreactive.repositories;

import io.ayers.springreactive.document.InventoryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@DataMongoTest
@DirtiesContext
class InventoryItemReactiveRepositoryTest {

    String staticId = "ABCD1234";

    List<InventoryItem> inventoryItems = List.of(
            InventoryItem.builder()
                         .id(staticId)
                         .description("Samsung TV")
                         .price(400.0)
                         .build(),
            InventoryItem.builder()
                         .description("LG TV")
                         .price(550.0)
                         .build(),
            InventoryItem.builder()
                         .description("Apple Watch")
                         .price(299.99)
                         .build(),
            InventoryItem.builder()
                         .description("Beats Headphones")
                         .price(159.99)
                         .build()
    );

    @Autowired
    InventoryItemReactiveRepository inventoryItemReactiveRepository;

    @BeforeEach
    void setUp() {
        inventoryItemReactiveRepository.deleteAll()
                                       .thenMany(Flux.fromIterable(inventoryItems)
                                                     .flatMap(inventoryItemReactiveRepository::save)
                                                     .doOnNext(inventoryItem -> System.out.println("Item added: " + inventoryItem.getDescription())))
                                       .blockLast(); // Wait until all items are saved before continuing
    }

    @Test
    void getAllItems_Success() {

        Flux<InventoryItem> repositoryAll = inventoryItemReactiveRepository.findAll();

        StepVerifier.create(repositoryAll)
                    .expectSubscription()
                    .expectNextCount(inventoryItems.size())
                    .verifyComplete();
    }

    @Test
    void getItemById_Success() {
        InventoryItem inventoryItem = inventoryItems.get(0);
        Mono<InventoryItem> byId = inventoryItemReactiveRepository.findById(staticId);

        StepVerifier.create(byId)
                    .expectSubscription()
                    .expectNextMatches(i -> i.equals(inventoryItem))
                    .verifyComplete();
    }

    @Test
    void getItemBySubscription_Success() {
        InventoryItem inventoryItem = inventoryItems.get(1);
        Flux<InventoryItem> byDescription = inventoryItemReactiveRepository.findByDescription(inventoryItem.getDescription());

        StepVerifier.create(byDescription)
                    .expectSubscription()
                    .expectNextMatches(i -> i.equals(inventoryItem))
                    .verifyComplete();
    }
}