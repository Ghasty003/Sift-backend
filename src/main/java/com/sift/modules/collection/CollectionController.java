package com.sift.modules.collection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(
            CollectionService collectionService
    ) {
        this.collectionService = collectionService;
    }

    @PostMapping("/create")
    public ResponseEntity<CollectionResponseDTO> createCollection(
            Authentication authentication,
            @RequestBody CreateCollectionRequestDTO request
    ) {
        CollectionResponseDTO response =
                collectionService.createCollection(
                        authentication,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponseDTO>> getCollections(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                collectionService.getCollections(authentication)
        );
    }
}
