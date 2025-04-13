package sidim.doma.service

import sidim.doma.entity.User
import sidim.doma.plugin.Localization
import sidim.doma.repository.UserRepository

class UserService(private val userRepository: UserRepository) {
    fun getUserByChatId(chatId: String): User? = userRepository.findByChatId(chatId)

    fun getUserByChatId(chatId: Long): User? = userRepository.findByChatId(chatId.toString())

    fun getAllActiveUsers(): List<User> = userRepository.findAllByActive(true)

    fun getActiveUsersByAppId(appId: String): List<User> =
        userRepository.findByActiveAndAppidAndBanned(true, appId, false)

    fun registerOrUpdateUser(
        chatId: String,
        name: String?,
        steamId: String,
        locale: String
    ) {
        if (existsByChatId(chatId)) {
            updateUser(chatId, name, steamId, locale)
        } else {
            createUser(chatId, name, steamId, locale)
        }
    }

    private fun createUser(chatId: String, name: String?, steamId: String, locale: String): User? {
        val user = User(
            chatId,
            name ?: Localization.getText("users.default_name", locale),
            steamId.toLong(),
            locale,
            true
        )
        return userRepository.create(user)
    }

    private fun updateUser(chatId: String, name: String?, steamId: String, locale: String): User? {
        return if (existsByChatId(chatId)) {
            userRepository.update(chatId, name, steamId.toLong(), locale)
        } else {
            null
        }
    }

    fun updateActiveByChatId(isActive: Boolean, chatId: String): Int =
        userRepository.updateActiveByChatId(isActive, chatId)

    fun existsByChatId(chatId: String) = userRepository.existsByChatId(chatId)
}