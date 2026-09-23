package ots.charcreate.unit.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ots.charcreate.api.VocationController;
import ots.charcreate.api.VocationResponse;

class VocationControllerTest {

    @Test
    void listsAllFiveVocationsOrderedById() {
        List<VocationResponse> vocations = new VocationController().listVocations();

        assertEquals(List.of(
                new VocationResponse(0, "None"),
                new VocationResponse(1, "Sorcerer"),
                new VocationResponse(2, "Druid"),
                new VocationResponse(3, "Paladin"),
                new VocationResponse(4, "Knight")),
                vocations);
    }
}
