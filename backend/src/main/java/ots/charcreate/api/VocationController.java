package ots.charcreate.api;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ots.charcreate.domain.Vocation;

/** {@code GET /api/vocations}, per {@code specs/03-api.md}. */
@RestController
public class VocationController {

    @GetMapping("/api/vocations")
    public List<VocationResponse> listVocations() {
        return Arrays.stream(Vocation.values())
                .sorted(Comparator.comparingInt(Vocation::id))
                .map(vocation -> new VocationResponse(vocation.id(), vocation.displayName()))
                .toList();
    }
}
