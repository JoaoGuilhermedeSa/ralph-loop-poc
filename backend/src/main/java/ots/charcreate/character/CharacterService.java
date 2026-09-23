package ots.charcreate.character;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ots.charcreate.api.CharacterResponse;
import ots.charcreate.api.CreateCharacterRequest;
import ots.charcreate.domain.Experience;
import ots.charcreate.domain.Sex;
import ots.charcreate.domain.Vocation;
import ots.charcreate.persistence.AccountEntity;
import ots.charcreate.persistence.AccountRepository;
import ots.charcreate.persistence.PlayerEntity;
import ots.charcreate.persistence.PlayerRepository;
import ots.charcreate.persistence.TownEntity;
import ots.charcreate.persistence.TownRepository;

/**
 * Resolves a character's starting stats and persists it, per
 * {@code specs/01-domain.md} and {@code specs/03-api.md}. Name uniqueness
 * (the database-level {@code NAME_TAKEN} check) is not implemented yet - see
 * {@code fix_plan.md}.
 */
@Service
public class CharacterService {

    // Fixed for every new character, per specs/01-domain.md.
    private static final int OUTFIT_LOOK_HEAD = 78;
    private static final int OUTFIT_LOOK_BODY = 68;
    private static final int OUTFIT_LOOK_LEGS = 58;
    private static final int OUTFIT_LOOK_FEET = 76;

    private final AccountRepository accounts;
    private final TownRepository towns;
    private final PlayerRepository players;
    private final int defaultStartLevel;
    private final int noneStartLevel;

    public CharacterService(AccountRepository accounts, TownRepository towns, PlayerRepository players,
            @Value("${app.start-level.default}") int defaultStartLevel,
            @Value("${app.start-level.vocation-none}") int noneStartLevel) {
        this.accounts = accounts;
        this.towns = towns;
        this.players = players;
        this.defaultStartLevel = defaultStartLevel;
        this.noneStartLevel = noneStartLevel;
    }

    @Transactional
    public CharacterResponse create(CreateCharacterRequest request) {
        requireField(request.accountId(), "accountId");
        requireField(request.name(), "name");
        requireField(request.vocation(), "vocation");
        requireField(request.sex(), "sex");
        requireField(request.townId(), "townId");

        String name = NameValidator.normalizeAndValidate(request.name());

        Vocation vocation = Vocation.byId(request.vocation())
                .orElseThrow(() -> new InvalidVocationException(request.vocation()));
        Sex sex = Sex.byId(request.sex()).orElseThrow(() -> new InvalidSexException(request.sex()));
        AccountEntity account = accounts.findById(request.accountId())
                .orElseThrow(() -> new UnknownAccountException(request.accountId()));
        TownEntity town = towns.findById(request.townId())
                .orElseThrow(() -> new UnknownTownException(request.townId()));

        int level = vocation == Vocation.NONE ? noneStartLevel : defaultStartLevel;
        long experience = Experience.forLevel(level);
        int health = vocation.healthAtLevel(level);
        int mana = vocation.manaAtLevel(level);
        int capacity = vocation.capacityAtLevel(level);
        int lookType = vocation.lookType(sex);

        String nameKey = name.toLowerCase(Locale.ROOT);

        PlayerEntity player = new PlayerEntity(account.getId(), name, nameKey, vocation.id(), sex.id(),
                town.getId(), level, experience, health, health, mana, mana, capacity, lookType, OUTFIT_LOOK_HEAD,
                OUTFIT_LOOK_BODY, OUTFIT_LOOK_LEGS, OUTFIT_LOOK_FEET, town.getPosX(), town.getPosY(), town.getPosZ(),
                LocalDateTime.now());
        player = players.save(player);

        return new CharacterResponse(player.getId(), player.getAccountId(), player.getName(), vocation.id(),
                vocation.displayName(), sex.id(), town.getId(), town.getName(), level, experience, health, health,
                mana, mana, capacity, lookType, OUTFIT_LOOK_HEAD, OUTFIT_LOOK_BODY, OUTFIT_LOOK_LEGS,
                OUTFIT_LOOK_FEET, town.getPosX(), town.getPosY(), town.getPosZ());
    }

    private static void requireField(Object value, String field) {
        if (value == null) {
            throw new FieldRequiredException(field);
        }
    }
}
