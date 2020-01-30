package com.wcs.poker.repository;

import com.wcs.poker.entity.Game;
import com.wcs.poker.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    Optional<List<GamePlayer>> findAllByGameId(Long idGame);

    Optional<List<GamePlayer>> findAllByGameIdAndStep(Long idGame, int step);

    GamePlayer findByPlayerIdAndGameId(long idPlayer, long idGame);

}
