package com.example.cairnclone.game_old

fun Game.spawnShaman(team: Team, pos: Pos): Game {
    val action = possibleSpawnAction(pos) ?: return this

    val shamanAtPos = this.shamans.firstOrNull { it.pos == pos }
    return when (team) {
        shamanAtPos?.team -> this
        else -> this.copy(
            shamans = if (shamanAtPos != null) this.shamans - shamanAtPos else this.shamans + Shaman(
                team = team,
                pos = pos
            ),
            actions = actions.flip(action)
        )
    }
}

fun Game.endTurn(): Game {
    return this.copy(
        activeTeam = when (activeTeam) {
            Team.Forest -> Team.Sea
            Team.Sea -> Team.Forest
        }
    )
}

fun Game.banishShaman(shaman: Shaman): Game {
    return this.copy(shamans = this.shamans - shaman)
}

fun Game.moveWithAction(shaman: Shaman, pos: Pos): Game {
    val action =
        this.possibleMoves(shaman).entries.firstOrNull { (_, positions) -> positions.contains(pos) }
            ?.component1() ?: return this

    val posTaken = this.shamans.any { it.pos == pos }
    return when {
        posTaken -> this
        else -> this.copy(
            shamans = this.shamans - shaman + shaman.copy(pos = pos),
            actions = actions.flip(action)
        )
    }
}

fun Game.transformShamans(s1: Shaman, s2: Shaman, enemyShaman: Shaman): Game {
    val transformation = possibleTransformation(s1, s2, enemyShaman) ?: return this
    return banishShaman(enemyShaman).copy(transformation = transformation.flip())
}

fun Game.spawnMonolith(power: MonolithPower, team: Team, pos: Pos): Game {
    if(!possibleToSpawnMonolith(power, pos)) return this

    return score(team).copy(
        monoliths = monoliths + Monolith(pos = pos, power = power),
        nextMonoliths = nextMonoliths.shift(power)
    )
}

fun Game.score(team: Team): Game {
    return if(team == Team.Forest) copy(forestPoints = forestPoints + 1) else copy(seaPoints = seaPoints + 1)
}

fun List<MonolithPower>.shift(power: MonolithPower): List<MonolithPower> {
    return this - power + power
}

fun Action.flip(): Action {
    return when (this) {
        Action.MoveShamanDiagonally -> Action.MoveShamanOrthogonally
        Action.MoveShamanOrthogonally -> Action.MoveShamanDiagonally
        Action.SpawnShamanOnBlack -> Action.SpawnShamanOnWhite
        Action.SpawnShamanOnWhite -> Action.SpawnShamanOnBlack
    }
}

fun List<Action>.flip(action: Action): List<Action> =
    this.map { if (it == action) action.flip() else it }


fun Transformation.flip(): Transformation {
    return when(this) {
        Transformation.Outnumbered -> Transformation.Surrounded
        Transformation.Surrounded -> Transformation.Outnumbered
    }
}