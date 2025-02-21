package sidim.doma.service

import sidim.doma.entity.User
import sidim.doma.repository.UserRepository

class UserService(private val userRepository: UserRepository) {
    fun getUserByChatId(chatId: String): User? = userRepository.findByChatId(chatId)

    fun getAllUsers(): List<User> = userRepository.findAll()

    fun getAllActiveUsers(): List<User> = userRepository.findAllByActive(true)

    fun getActiveUsersByAppId(appId: String): List<User> =
        userRepository.findByActiveAndAppidAndBanned(true, appId, false)

    fun createUser(chatId: String, name: String?, steamId: String, locale: String) {
        val user = User(chatId, name ?: "", steamId.toLong(), locale, true)
        userRepository.create(user)
    }

    fun updateUser(chatId: String, name: String?, steamId: String, locale: String) {
        if (existsByChatId(chatId)) {
            userRepository.update(chatId, name, steamId.toLong(), locale)
        }
    }

    fun updateActiveByChatId(isActive: Boolean, chatId: String) =
        userRepository.updateActiveByChatId(isActive, chatId)

    private fun existsByChatId(chatId: String) = userRepository.existsByChatId(chatId)
}