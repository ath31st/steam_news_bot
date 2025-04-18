package sidim.doma.application.game.mapper

import sidim.doma.domain.game.entity.Game
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDetailsDto
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDto
import sidim.doma.infrastructure.integration.steam.dto.SteamWishlistAppDto

fun SteamAppDetailsDto.toGame() = Game(
    appid = appid,
    name = name
)

fun SteamAppDto.toGame() = Game(
    appid = appid.toString(),
    name = name
)

fun SteamWishlistAppDto.toGame() = Game(
    appid = appid.toString(),
    name = null
)