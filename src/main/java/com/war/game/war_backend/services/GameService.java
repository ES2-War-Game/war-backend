package com.war.game.war_backend.services;

import com.war.game.war_backend.model.Game;
import com.war.game.war_backend.model.Player;
import com.war.game.war_backend.model.PlayerGame;
import com.war.game.war_backend.repository.GameRepository;
import com.war.game.war_backend.repository.PlayerGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Transactional
    public Game addPlayerToLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        // Verifica se o jogador já está no lobby
        Optional<PlayerGame> existingPlayerGame = playerGameRepository.findByGameAndPlayer(game, player);
        if (existingPlayerGame.isPresent()) {
            throw new RuntimeException("Jogador já está no lobby.");
        }

        // Cria a entidade PlayerGame para o novo jogador
        PlayerGame newPlayerGame = new PlayerGame();
        newPlayerGame.setGame(game);
        newPlayerGame.setPlayer(player);
        newPlayerGame.setIsOwner(false);
        newPlayerGame.setIsReady(false);

        playerGameRepository.save(newPlayerGame);

        return gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));
    }

    @Transactional
    public Game removePlayerFromLobby(Long lobbyId, Player player) {
        Game game = gameRepository.findById(lobbyId)
                .orElseThrow(() -> new RuntimeException("Lobby não encontrado."));

        // Encontra a entidade PlayerGame para remover
        PlayerGame playerGame = playerGameRepository.findByGameAndPlayer(game, player)
                .orElseThrow(() -> new RuntimeException("Jogador não está no lobby."));

        // Remove a entidade de relacionamento do banco de dados
        playerGameRepository.delete(playerGame);

        // Lógica para o dono: se o dono sair, o próximo vira o dono
        if (playerGame.getIsOwner()) {
            List<PlayerGame> remainingPlayers = playerGameRepository.findByGame(game);
            if (!remainingPlayers.isEmpty()) {
                PlayerGame newOwner = remainingPlayers.get(0);
                newOwner.setIsOwner(true);
                playerGameRepository.save(newOwner);
            } else {
                // Se não houver mais jogadores, o lobby é excluído
                gameRepository.delete(game);
                return null; // Retorna null para dizxer que o lobby foi excluído
            }
        }
        
        return gameRepository.findById(lobbyId).orElse(null);
    }
}