package ots.charcreate.unit.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import ots.charcreate.persistence.AccountEntity;
import ots.charcreate.persistence.AccountRepository;
import ots.charcreate.persistence.PlayerEntity;
import ots.charcreate.persistence.PlayerRepository;
import ots.charcreate.persistence.TownEntity;
import ots.charcreate.persistence.TownRepository;

/**
 * Boots the real Flyway-migrated schema with {@code ddl-auto: validate} and
 * round-trips each entity. If an entity disagrees with {@code V1__init.sql},
 * this fails at context startup - that is the point of the Persistence
 * backlog item. Each test rolls back so it doesn't leak fixtures into the
 * next one.
 */
@SpringBootTest
@Transactional
class PersistenceSmokeTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void townsAreSeededByTheMigration() {
        List<TownEntity> towns = townRepository.findAll();
        assertEquals(6, towns.size());

        TownEntity thais = towns.stream().filter(t -> t.getName().equals("Thais")).findFirst().orElseThrow();
        assertEquals(32369, thais.getPosX());
        assertEquals(32241, thais.getPosY());
        assertEquals(7, thais.getPosZ());
    }

    @Test
    void accountRoundTrips() {
        AccountEntity saved = newAccount();

        AccountEntity found = accountRepository.findById(saved.getId()).orElseThrow();
        assertEquals("smoketest", found.getName());
        assertEquals("smoketest@example.invalid", found.getEmail());
    }

    @Test
    void playerRoundTrips() {
        AccountEntity account = newAccount();

        PlayerEntity player = new PlayerEntity(account.getId(), "Smoke Tester", "smoke tester", 1, 1, 1,
                8, 4200L, 185, 185, 210, 210, 470, 130, 78, 68, 58, 76, 32369, 32241, 7, LocalDateTime.now());
        PlayerEntity saved = playerRepository.save(player);

        PlayerEntity found = playerRepository.findById(saved.getId()).orElseThrow();
        assertEquals(account.getId(), found.getAccountId());
        assertEquals("Smoke Tester", found.getName());
        assertEquals("smoke tester", found.getNameKey());
        assertEquals(1, found.getTownId());
        assertEquals(8, found.getLevel());
        assertEquals(4200L, found.getExperience());
        assertEquals(185, found.getHealthMax());
        assertEquals(210, found.getManaMax());
        assertEquals(470, found.getCapacity());
        assertEquals(130, found.getLookType());
    }

    /**
     * Accounts are pre-existing in the real world (specs/05-schema.md) - the
     * app never inserts one, so the fixture goes in over JDBC, same as
     * {@code AcceptanceTest} seeds its accounts.
     */
    private AccountEntity newAccount() {
        jdbc.update("INSERT INTO accounts (name, password_hash, email) VALUES (?, ?, ?)",
                "smoketest", "not-a-real-hash", "smoketest@example.invalid");
        Long id = jdbc.queryForObject("SELECT id FROM accounts WHERE name = ?", Long.class, "smoketest");
        return accountRepository.findById(id).orElseThrow();
    }
}
