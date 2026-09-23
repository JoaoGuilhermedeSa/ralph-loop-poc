package ots.charcreate.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {

    long countByAccountId(Long accountId);
}
