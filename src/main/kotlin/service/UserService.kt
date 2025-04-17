package sidim.doma.service

import sidim.doma.entity.User
import sidim.doma.repository.UserRepository

class UserService(private val userRepository: UserRepository) {
    fun getUserByChatId(chatId: String): User? = userRepository.findByChatId(chatId)

    fun getUserByChatId(chatId: Long): User? = userRepository.findByChatId(chatId.toString())

    fun getAllActiveUsers(): List<User> = userRepository.findAllByActive(true)

    fun getActiveUsersByAppId(appId: String): List<User> =
        userRepository.findByActiveAndAppidAndBanned(true, appId, false)

    fun registerOrUpdateUser(
        chatId: String,
        name: String,
        steamId: Long,
        locale: String
    ): User? {
        require(chatId.isNotBlank()) { "chatId must not be blank" }
        require(locale.isNotBlank()) { "locale must not be blank" }

        return if (existsByChatId(chatId)) {
            updateUser(chatId, name, steamId, locale)
        } else {
            createUser(chatId, name, steamId, locale)
        }
    }

    private fun createUser(chatId: String, name: String, steamId: Long, locale: String): User? {
        val user = User(chatId, name, steamId, locale, true)

        return userRepository.create(user)
    }

    private fun updateUser(chatId: String, name: String, steamId: Long, locale: String): User? {
        return userRepository.update(chatId, name, steamId, locale)
    }

    fun updateActiveByChatId(isActive: Boolean, chatId: String): Int =
        userRepository.updateActiveByChatId(isActive, chatId)

    fun existsByChatId(chatId: String) = userRepository.existsByChatId(chatId)

    fun countUsers(): Long = userRepository.countUsers()

    fun countActiveUsers(): Long = userRepository.findAllByActive(true).size.toLong()
}