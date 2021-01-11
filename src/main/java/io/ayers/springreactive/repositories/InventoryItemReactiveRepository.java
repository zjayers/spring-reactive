package io.ayers.springreactive.repositories;

import io.ayers.springreactive.document.InventoryItem;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface InventoryItemReactiveRepository
        extends ReactiveMongoRepository<InventoryItem, String> {

    Flux<InventoryItem> findByDescription(String description);

}
