package ots.charcreate.unit.character;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import ots.charcreate.api.CharacterResponse;
import ots.charcreate.api.CreateCharacterRequest;
import ots.charcreate.character.CharacterLimitReachedException;
import ots.charcreate.character.CharacterService;
import ots.charcreate.character.FieldRequiredException;
import ots.charcreate.character.InvalidSexException;
import ots.charcreate.character.InvalidVocationException;
import ots.charcreate.character.NameTakenException;
import ots.charcreate.character.NameTooShortException;
import ots.charcreate.character.UnknownAccountException;
import ots.charcreate.character.UnknownTownException;
import ots.charcreate.persistence.AccountEntity;
import ots.charcreate.persistence.AccountRepository;
import ots.charcreate.persistence.PlayerRepository;
import ots.charcreate.persistence.TownEntity;
import ots.charcreate.persistence.TownRepository;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private AccountRepository accounts;

    @Mock
    private TownRepository towns;

    @Mock
    private PlayerRepository players;

    private CharacterService service;

    @BeforeEach
    void setUp() {
        service = new CharacterService(accounts, towns, players, 8, 1);
    }

    @Test
    void resolvesStartingStatsForAVocationThatStartsAtTheDefaultLevel() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account(1L)));
        when(towns.findById(1)).thenReturn(Optional.of(new TownEntity(1, "Thais", 32369, 32241, 7)));
        when(players.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CharacterResponse response = service.create(new CreateCharacterRequest(1L, "Bubble", 1, 1, 1));

        assertEquals("Bubble", response.name());
        assertEquals(1, response.vocation());
        assertEquals("Sorcerer", response.vocationName());
        assertEquals(1, response.sex());
        assertEquals(1, response.townId());
        assertEquals("Thais", response.townName());
        assertEquals(8, response.level());
        assertEquals(4200L, response.experience());
        assertEquals(185, response.health());
        assertEquals(185, response.healthMax());
        assertEquals(210, response.mana());
        assertEquals(210, response.manaMax());
        assertEquals(470, response.capacity());
        assertEquals(130, response.lookType());
        assertEquals(78, response.lookHead());
        assertEquals(68, response.lookBody());
        assertEquals(58, response.lookLegs());
        assertEquals(76, response.lookFeet());
        assertEquals(32369, response.posX());
        assertEquals(32241, response.posY());
        assertEquals(7, response.posZ());
    }

    @Test
    void vocationNoneUsesItsOwnConfiguredStartLevel() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account(1L)));
        when(towns.findById(1)).thenReturn(Optional.of(new TownEntity(1, "Thais", 32369, 32241, 7)));
        when(players.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CharacterResponse response = service.create(new CreateCharacterRequest(1L, "Rookie", 0, 1, 1));

        assertEquals(1, response.level());
        assertEquals(0L, response.experience());
        assertEquals(150, response.health());
        assertEquals(0, response.mana());
        assertEquals(400, response.capacity());
        assertEquals(128, response.lookType());
    }

    @Test
    void femaleOutfitDiffersFromMale() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account(1L)));
        when(towns.findById(1)).thenReturn(Optional.of(new TownEntity(1, "Thais", 32369, 32241, 7)));
        when(players.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CharacterResponse response = service.create(new CreateCharacterRequest(1L, "Lady", 0, 0, 1));

        assertEquals(136, response.lookType());
    }

    @Test
    void unknownAccountIsRejected() {
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UnknownAccountException.class,
                () -> service.create(new CreateCharacterRequest(999L, "Bubble", 1, 1, 1)));
    }

    @Test
    void unknownTownIsRejected() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account(1L)));
        when(towns.findById(999)).thenReturn(Optional.empty());

        assertThrows(UnknownTownException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 1, 1, 999)));
    }

    @Test
    void invalidVocationIsRejected() {
        assertThrows(InvalidVocationException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 9, 1, 1)));
    }

    @Test
    void invalidSexIsRejected() {
        assertThrows(InvalidSexException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 1, 9, 1)));
    }

    @Test
    void missingAccountIdIsRejected() {
        FieldRequiredException ex = assertThrows(FieldRequiredException.class,
                () -> service.create(new CreateCharacterRequest(null, "Bubble", 1, 1, 1)));
        assertEquals("accountId", ex.field());
    }

    @Test
    void missingNameIsRejected() {
        FieldRequiredException ex = assertThrows(FieldRequiredException.class,
                () -> service.create(new CreateCharacterRequest(1L, null, 1, 1, 1)));
        assertEquals("name", ex.field());
    }

    @Test
    void missingVocationIsRejected() {
        FieldRequiredException ex = assertThrows(FieldRequiredException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", null, 1, 1)));
        assertEquals("vocation", ex.field());
    }

    @Test
    void missingSexIsRejected() {
        FieldRequiredException ex = assertThrows(FieldRequiredException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 1, null, 1)));
        assertEquals("sex", ex.field());
    }

    @Test
    void missingTownIdIsRejected() {
        FieldRequiredException ex = assertThrows(FieldRequiredException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 1, 1, null)));
        assertEquals("townId", ex.field());
    }

    @Test
    void nameRulesAreCheckedBeforeVocation() {
        assertThrows(NameTooShortException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Ab", 9, 9, 1)));
    }

    @Test
    void characterLimitIsRejectedAtTenExistingCharacters() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account(1L)));
        when(towns.findById(1)).thenReturn(Optional.of(new TownEntity(1, "Thais", 32369, 32241, 7)));
        when(players.countByAccountId(1L)).thenReturn(10L);

        assertThrows(CharacterLimitReachedException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 1, 1, 1)));
    }

    @Test
    void nameTakenMapsTheUniqueConstraintViolationInsteadOfSurfacingIt() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account(1L)));
        when(towns.findById(1)).thenReturn(Optional.of(new TownEntity(1, "Thais", 32369, 32241, 7)));
        when(players.save(any())).thenThrow(new DataIntegrityViolationException("uq_players_name_key"));

        assertThrows(NameTakenException.class,
                () -> service.create(new CreateCharacterRequest(1L, "Bubble", 1, 1, 1)));
    }

    private static AccountEntity account(Long id) {
        return new AccountEntity(id, "account" + id, "not-a-real-hash", "account" + id + "@example.invalid",
                LocalDateTime.now());
    }
}
