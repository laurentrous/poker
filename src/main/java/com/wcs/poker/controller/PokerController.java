package com.wcs.poker.controller;

import com.wcs.poker.entity.Card;
import com.wcs.poker.entity.Game;
import com.wcs.poker.entity.GamePlayer;
import com.wcs.poker.entity.Player;
import com.wcs.poker.repository.CardRepository;
import com.wcs.poker.repository.GamePlayerRepository;
import com.wcs.poker.repository.GameRepository;
import com.wcs.poker.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
public class PokerController {

    @Autowired
    public CardRepository cardRepository;

    @Autowired
    public GameRepository gameRepository;

    @Autowired
    public GamePlayerRepository gamePlayerRepository;

    @Autowired
    public PlayerRepository playerRepository;

    public static int pot = 0;

    @GetMapping("/")
    public String index(Model model) {
        List<Game> games = gameRepository.findTop1ByOrderByIdDesc();
        if (games.size() > 0) {
            Game actualGame = games.get(0);
            model.addAttribute("actualGame", actualGame);
            List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameIdOrderByPlayerPosition(actualGame.getId()).get();
            if (gamePlayers.size() > 0) {
                GamePlayer firstGamePlayer = gamePlayers.get(0);
                model.addAttribute("firstGamePlayer", firstGamePlayer);
            }
        }
        return "index";
    }


    @GetMapping("/newGame")
    public String newGame(Model model) {
        //TODO remettre le jeu de carte à zéro : pulled = false
        this.initialiseCardGame();
        Game game = new Game();
        model.addAttribute("game", game);
        List<Player> players = playerRepository.findAll();
        model.addAttribute("players", players);
        return "newGame";
    }

    @PostMapping("/newGame")
    public String createNewGame(Model model,
                                @RequestParam int player1,
                                @RequestParam int player2,
                                @RequestParam int player3,
                                @RequestParam int player4,
                                @RequestParam int player5,
                                @ModelAttribute Game newGame) {
        //TODO créer un nouveau jeu
        Game saveNewGame = gameRepository.save(newGame);
        pot = 0;
        //TODO instancier les joueurs
        List<Player> players = new ArrayList<>();
        if (player1 > 0) {
            Long player1Long = (long) player1;
            players.add(playerRepository.findById(player1Long).get());
        }
        if (player2 > 0) {
            Long player2Long = (long) player2;
            players.add(playerRepository.findById(player2Long).get());
        }
        if (player3 > 0) {
            Long player3Long = (long) player3;
            players.add(playerRepository.findById(player3Long).get());
        }
        if (player4 > 0) {
            Long player4Long = (long) player4;
            players.add(playerRepository.findById(player4Long).get());
        }
        if (player5 > 0) {
            Long player5Long = (long) player5;
            players.add(playerRepository.findById(player5Long).get());
        }

        List<GamePlayer> gamePlayers = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            List<Card> cards = new ArrayList<>();
            while (cards.size() < 2) {
                cards.add(this.pullACard());
            }
            player.setCards(cards);
            GamePlayer gamePlayer = new GamePlayer();
            gamePlayer.setGame(saveNewGame);
            gamePlayer.setPlayer(player);
            gamePlayer.setGain(player.getWallet());
            gamePlayer.setPlayerPosition(i + 1);
            if (i == players.size() - 1) {
                gamePlayer.setTurn("BB");
                gamePlayer.setGain(gamePlayer.getGain() - saveNewGame.getBigBlind());
                pot += saveNewGame.getBigBlind();
            } else if (i == players.size() - 2) {
                gamePlayer.setTurn("SB");
                gamePlayer.setGain(gamePlayer.getGain() - saveNewGame.getSmallBlind());
                pot += saveNewGame.getSmallBlind();
            } else {
                gamePlayer.setTurn("");
            }
            gamePlayerRepository.save(gamePlayer);
        }

        List<Card> gameCards = new ArrayList<>();
        while (gameCards.size() < 5) {
            gameCards.add(this.pullACard());
        }
        saveNewGame.setCards(gameCards);
        gameRepository.save(saveNewGame);
        gamePlayers = gamePlayerRepository.findAllByGameId(saveNewGame.getId()).get();

        GamePlayer gamePlayerWhoPlays = gamePlayers.get(0);
        model.addAttribute("gamePlayers", gamePlayers);
        return "redirect:/game/" + saveNewGame.getId() + "/1/" + gamePlayerWhoPlays.getId();
    }

    @GetMapping("/newDistribution/{idGame}")
    public String newDistribution(Model model,
                                  @PathVariable("idGame") int idGame){

        Long idGameLong = (long) idGame;
        Game game = gameRepository.findById(idGameLong).get();
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameIdOrderByPlayerPosition(idGameLong).get();
        GamePlayer gamePlayerWhoPlays = new GamePlayer();
        for (GamePlayer gamePlayer : gamePlayers) {
            Player player = gamePlayer.getPlayer();
            List<Card> cards = new ArrayList<>();
            while (cards.size() < 2) {
                cards.add(this.pullACard());
            }
            player.setCards(cards);
            gamePlayerRepository.save(gamePlayer);
            if(gamePlayer.getPlayerPosition()==1){
                gamePlayerWhoPlays = gamePlayer;
            }
        }

        List<Card> gameCards = new ArrayList<>();
        while (gameCards.size() < 5) {
            gameCards.add(this.pullACard());
        }
        game.setCards(gameCards);
        gameRepository.save(game);

        model.addAttribute("gamePlayers", gamePlayers);
        return "redirect:/game/" + game.getId() + "/1/" + gamePlayerWhoPlays.getId();
    }

    @GetMapping("/game/{idGame}/{step}/{idGamePlayer}")
    public String gameStep1(Model model,
                            @PathVariable("idGame") int idGame,
                            @PathVariable("step") int step,
                            @PathVariable("idGamePlayer") int idGamePlayer) {
        Long idGameLong = (long) idGame;
        Long idGamePlayerLong = (long) idGamePlayer;
        Game game = gameRepository.findById(idGameLong).get();
        model.addAttribute("game", game);
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(idGameLong).get();
        model.addAttribute("gamePlayers", gamePlayers);
        GamePlayer actualGamePlayer = gamePlayerRepository.findById(idGamePlayerLong).get();
        List<Card> gameCards = game.getCards();
        Player actualPlayer = actualGamePlayer.getPlayer();
        model.addAttribute("actualPlayer", actualPlayer);
        model.addAttribute("step", step);
        if (step >= 2) {
            model.addAttribute("carte1", gameCards.get(0));
            model.addAttribute("carte2", gameCards.get(1));
            model.addAttribute("carte3", gameCards.get(2));
        }
        if (step >= 3) {
            model.addAttribute("carte4", gameCards.get(3));
        }
        if (step >= 4) {
            model.addAttribute("carte5", gameCards.get(4));
        }
        model.addAttribute("pot", pot);
        return "gameStep";
    }

    @PostMapping("/game/{idGame}")
    public String gameStep1PlayerDecision(Model model,
                                          @PathVariable("idGame") int idGame,
                                          @RequestParam("step") int step,
                                          @RequestParam("idPlayer") int idPlayer,
                                          @RequestParam("decision") int decision) {
        Long idGameLong = (long) idGame;
        Game game = gameRepository.findById(idGameLong).get();
        model.addAttribute("game", game);
        Long idActualPlayer = (long) idPlayer;
        GamePlayer actualGamePlayer = gamePlayerRepository.findByPlayerIdAndGameId(idActualPlayer, idGameLong);
        actualGamePlayer.setPlayerDecision(decision);
        if (decision == 2) {
            if (step == 1 && actualGamePlayer.getTurn().equals("SB")) {
                actualGamePlayer.setGain(actualGamePlayer.getGain() - (game.getBigBlind() - game.getSmallBlind()));
                pot += game.getBigBlind() - game.getSmallBlind();
            } else if (step == 1 && actualGamePlayer.getTurn().equals("BB")) {
                pot += 0;
            } else {
                actualGamePlayer.setGain(actualGamePlayer.getGain() - game.getBigBlind());
                pot += game.getBigBlind();
            }
        }
        actualGamePlayer.setStep(step);
        gamePlayerRepository.save(actualGamePlayer);
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(idGameLong).get();
        int position = gamePlayers.indexOf(actualGamePlayer);
        GamePlayer nextGamePlayer = this.whoIsPlayingThisStep2(game, position);
        if (actualGamePlayer.getTurn().equals("BB")) {
            step++;
            nextGamePlayer = gamePlayers.get(0);
            if (step == 5) {
                return "redirect:/conclusion/" + game.getId();
            }
        }
        model.addAttribute("step", step);
        return "redirect:/game/" + game.getId() + "/" + step + "/" + nextGamePlayer.getId();

    }

    @GetMapping("/conclusion/{idGame}")
    public String conclusion(Model model,
                             @PathVariable("idGame") int idGame) {
        Long idGameLong = (long) idGame;
        Game game = gameRepository.findById(idGameLong).get();
        model.addAttribute("game", game);
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(game.getId()).get();
        model.addAttribute("gamePlayers", gamePlayers);
        List<Card> gameCards = game.getCards();
        model.addAttribute("gameCards", gameCards);
        model.addAttribute("pot", pot);
        return "conclusion";
    }

    @PostMapping("/conclusion/{idGame}")
    public String conclusionSave(Model model,
                                 @PathVariable("idGame") int idGame,
                                 @RequestParam("idGamePlayerWinner") Long idGamePlayerWinner) {
        Long idGameLong = (long) idGame;
        GamePlayer winner = gamePlayerRepository.findById(idGamePlayerWinner).get();
        winner.setGain(winner.getGain() + pot);
        Game game = gameRepository.findById(idGameLong).get();
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(game.getId()).get();
        for (GamePlayer gamePlayer : gamePlayers) {
            gamePlayer.setStep(0);
            gamePlayer.setPlayerDecision(0);
            Player player = gamePlayer.getPlayer();
            player.setWallet(gamePlayer.getGain());
            if (gamePlayer.getPlayerPosition() < gamePlayers.size()) {
                gamePlayer.setPlayerPosition(gamePlayer.getPlayerPosition() + 1);
            } else {
                gamePlayer.setPlayerPosition(1);
            }
            gamePlayerRepository.save(gamePlayer);
        }
        gamePlayers = gamePlayerRepository.findAllByGameId(game.getId()).get();
        for (GamePlayer gamePlayer : gamePlayers) {
            if (gamePlayer.getPlayerPosition() == gamePlayers.size()) {
                gamePlayer.setTurn("BB");
            } else if (gamePlayer.getPlayerPosition() == gamePlayers.size() - 1) {
                gamePlayer.setTurn("SB");
            } else {
                gamePlayer.setTurn("");
            }
            gamePlayerRepository.save(gamePlayer);
        }
        return "redirect:/newDistribution/" + game.getId();
    }

    public Card pullACard() {
        Random random = new Random();
        int nb;
        nb = random.nextInt(53);
        Long nombre = 1L + nb;
        for (int i = 1; i <= 52; i++) {
            Card card;
            Optional<Card> optionalCard = cardRepository.findById(nombre);
            if (optionalCard.isPresent()) {
                card = optionalCard.get();
                if (!card.isPulled()) {
                    card.setPulled(true);
                    cardRepository.save(card);
                    return card;
                } else {
                    if (nombre < 51) {
                        nombre++;
                    } else {
                        nombre = 1L;
                    }
                }
            }
        }
        return null;
    }

    public void initialiseCardGame() {
        for (Long i = 1L; i <= 52; i++) {
            Optional<Card> optionalCard = cardRepository.findById(i);
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                card.setPulled(false);
                cardRepository.save(card);
            }
        }
    }

    public GamePlayer whoIsPlayingThisStep(Game game, int actualPosition) {
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(game.getId()).get();
        int position = actualPosition + 1;
        if (position == gamePlayers.size()) {
            position = 0;
        }
        return gamePlayers.get(position);
    }

    public GamePlayer whoIsPlayingThisStep2(Game game, int actualPosition) {
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(game.getId()).get();
        int position = actualPosition + 1;
        while(position != actualPosition) {
            if (position == gamePlayers.size()) {
                position = 0;
            }
            GamePlayer nextGamePlayer = gamePlayers.get(position);
            if (nextGamePlayer.getPlayerDecision() != 1) {
                return gamePlayers.get(position);
            } else {
                position++;
            }
        }
        return gamePlayers.get(position);
    }

    public boolean checkStep(Game game, int step) {
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameIdAndStep(game.getId(), step).get();
        int a = 1;
        int decision = 2;
        for (GamePlayer gamePlayer : gamePlayers) {
            int playerDecision = gamePlayer.getPlayerDecision();
            if (playerDecision != decision) {
                return false;
            }
        }
        return true;
    }

}
