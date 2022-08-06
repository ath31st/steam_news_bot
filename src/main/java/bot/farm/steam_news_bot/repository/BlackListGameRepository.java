package bot.farm.steam_news_bot.repository;

import bot.farm.steam_news_bot.entity.BlackListGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlackListGameRepository extends JpaRepository<BlackListGame, Long> {
    Optional<BlackListGame> findByChatIdAndName(String chatId, String name);

    boolean existsByChatIdAndName(String chatId, String name);

    boolean existsByChatIdAndAppid(String chatId, String appid);

    @Transactional
    @Modifying
    @Query("delete from BlackListGame b where b.chatId = ?1 and b.name = ?2")
    void deleteByChatIdAndName(String chatId, String name);

    List<BlackListGame> findByChatId(String chatId);

    @Transactional
    @Modifying
    @Query("delete from BlackListGame b where b.chatId = ?1")
    void deleteByChatId(String chatId);


}
