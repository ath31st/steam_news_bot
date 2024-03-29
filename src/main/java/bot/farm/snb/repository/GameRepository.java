package bot.farm.snb.repository;

import bot.farm.snb.entity.Game;
import java.util.List;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Game entities.
 */
@Repository
public interface GameRepository extends CrudRepository<Game, String> {
  long countByStates_User_ActiveTrue();

  List<Game> findByStates_User_ChatIdAndStates_IsBannedTrue(String chatId);

  List<Game> findByStates_User_ActiveTrue();

  Set<Game> findByStates_User_ActiveTrueAndStates_IsBannedFalse();

  boolean existsByAppid(String appid);

}
