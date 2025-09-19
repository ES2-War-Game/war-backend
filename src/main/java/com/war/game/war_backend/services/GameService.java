package com.war.game.war_backend.services;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerGameRepository playerGameRepository;

    public Game createNewLobby(String lobbyName, Player creator) {
        Game newGame = new Game();
        newGame.setName(lobbyName);
        newGame.setStatus("Lobby"); // O status inicial é "Lobby"
        newGame.setCreatedAt(LocalDateTime.now());

        gameRepository.save(newGame);

        // Cria a entidade PlayerGame para o criador
        PlayerGame creatorPlayerGame = new PlayerGame();
        creatorPlayerGame.setGame(newGame);
        creatorPlayerGame.setPlayer(creator);
        creatorPlayerGame.setIsOwner(true);
        creatorPlayerGame.setIsReady(false);

        playerGameRepository.save(creatorPlayerGame);

        return newGame;
    }

    public List<Game> findAllLobbies() {
        return gameRepository.findByStatus("Lobby");
    }
}