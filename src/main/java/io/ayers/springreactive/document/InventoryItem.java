package io.ayers.springreactive.document;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class InventoryItem {

    @Id
    private String id;

    private String description;
    private Double price;
}
