package ots.charcreate.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ots.charcreate.character.CharacterService;

/** {@code POST /api/characters}, per {@code specs/03-api.md}. */
@RestController
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping("/api/characters")
    public ResponseEntity<CharacterResponse> create(@RequestBody CreateCharacterRequest request) {
        CharacterResponse character = characterService.create(request);
        return ResponseEntity.created(URI.create("/api/characters/" + character.id())).body(character);
    }
}
