package sidim.doma.domain.user.repository

import sidim.doma.domain.user.entity.User

interface UserRepository {
    fun existsByChatId(chatId: String): Boolean
    fun findByChatId(chatId: String): User?
    fun findAll(): List<User>
    fun findAllByActive(isActive: Boolean): List<User>
    fun findByActiveAndAppidAndBanned(
        isActive: Boolean,
        appId: String,
        isBanned: Boolean
    ): List<User>

    fun create(user: User): User?
    fun update(chatId: String, name: String?, steamId: Long, locale: String): User?
    fun updateActiveByChatId(isActive: Boolean, chatId: String): Int
    fun countUsers(): Long
}