package ots.charcreate.unit.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ots.charcreate.api.TownController;
import ots.charcreate.api.TownResponse;
import ots.charcreate.persistence.TownEntity;
import ots.charcreate.persistence.TownRepository;

@ExtendWith(MockitoExtension.class)
class TownControllerTest {

    @Mock
    private TownRepository townRepository;

    @Test
    void listsTownsOrderedByIdRegardlessOfRepositoryOrder() {
        when(townRepository.findAll()).thenReturn(List.of(
                new TownEntity(3, "Venore", 0, 0, 0),
                new TownEntity(1, "Thais", 32369, 32241, 7),
                new TownEntity(2, "Carlin", 0, 0, 0)));

        List<TownResponse> towns = new TownController(townRepository).listTowns();

        assertEquals(List.of(
                new TownResponse(1, "Thais"),
                new TownResponse(2, "Carlin"),
                new TownResponse(3, "Venore")),
                towns);
    }
}
