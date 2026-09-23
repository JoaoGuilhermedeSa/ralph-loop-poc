package ots.charcreate.api;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ots.charcreate.persistence.TownEntity;
import ots.charcreate.persistence.TownRepository;

/** {@code GET /api/towns}, per {@code specs/03-api.md}. */
@RestController
public class TownController {

    private final TownRepository towns;

    public TownController(TownRepository towns) {
        this.towns = towns;
    }

    @GetMapping("/api/towns")
    public List<TownResponse> listTowns() {
        return towns.findAll().stream()
                .sorted(Comparator.comparingInt(TownEntity::getId))
                .map(town -> new TownResponse(town.getId(), town.getName()))
                .toList();
    }
}
