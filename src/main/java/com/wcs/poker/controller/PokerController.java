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


    @GetMapping("/")
    public String index(Model model) {
        List<Game> games = gameRepository.findTop1ByOrderByIdDesc();
        if (games.size() > 0) {
            Game actualGame = games.get(0);
            List<GamePlayer> gamePlayers = gamePlayerRepository.findAll();
            model.addAttribute("actualGame", actualGame);
            if (gamePlayers.size() > 0) {
                GamePlayer firstGamePlayer = gamePlayers.get(0);
                model.addAttribute("firstGamePlayer", firstGamePlayer);
            }
        }
        return "index";
    }

    @GetMapping("/newGame")
    public String newGame(HttpSession sessionUser, Model model) {
        //TODO récupérer la session du joueur connecté
        //TODO remettre le jeu de carte à zéro : pulled = false
        this.initialiseCardGame();
        Game game = new Game();
        model.addAttribute("game", game);
        return "newGame";

    }

    @PostMapping("/newGame")
    public String createNewGame(HttpSession sessionUser, Model model, @RequestParam int nbPlayer, @ModelAttribute Game newGame) {
        //TODO créer un nouveau jeu
        Game saveNewGame = gameRepository.save(newGame);
        //TODO instancier les joueurs
        List<GamePlayer> gamePlayers = new ArrayList<>();
        for (Long i = 1L; i <= nbPlayer; i++) {
            Optional<Player> optionalPlayer = playerRepository.findById(i);
            if (optionalPlayer.isPresent()) {
                Player player = optionalPlayer.get();
                List<Card> cards = new ArrayList<>();
                cards.add(this.pullACard());
                cards.add(this.pullACard());
                player.setCards(cards);
                GamePlayer gamePlayer = new GamePlayer();
                gamePlayer.setGame(saveNewGame);
                gamePlayer.setPlayer(player);
                if (i == nbPlayer) {
                    gamePlayer.setTurn("BB");
                } else if (i == nbPlayer - 1) {
                    gamePlayer.setTurn("SB");
                } else {
                    gamePlayer.setTurn("");
                }
                gamePlayer.setGain(player.getWallet());
                gamePlayerRepository.save(gamePlayer);
            }
        }
        List<Card> gameCards = new ArrayList<>();
        for (int a = 0; a < 5; a++) {
            gameCards.add(this.pullACard());
        }
        saveNewGame.setCards(gameCards);
        gameRepository.save(saveNewGame);
        gamePlayers = gamePlayerRepository.findAllByGameId(saveNewGame.getId()).get();
        GamePlayer gamePlayerWhoPlays = gamePlayers.get(0);
        model.addAttribute("gamePlayers", gamePlayers);
        return "redirect:/game/" + saveNewGame.getId() + "/1/" + gamePlayerWhoPlays.getId();
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
        actualGamePlayer.setPlayerDecisison(decision);
        actualGamePlayer.setStep(step);
        gamePlayerRepository.save(actualGamePlayer);
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameId(idGameLong).get();
        int position = gamePlayers.indexOf(actualGamePlayer);
        GamePlayer nextGamePlayer = this.whoIsPlayingThisStep(game, position);
        //TODO ICI VERIFER SI ON PEUT PASSER A LETAPE SUIVANTE
        if (step > 50) {
            step++;
            if (step > 5) {
                //todo faire une redirection vers la fin de cette distribution
            }
            model.addAttribute("step", step);
            return "redirect:/game/" + game.getId() + "/" + step + "/" + gamePlayers.get(0).getId();
        }
        model.addAttribute("step", step);
        return "redirect:/game/" + game.getId() + "/" + step + "/" + nextGamePlayer.getId();

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
        int a = 0;
        GamePlayer gamePlayer = gamePlayers.get(position);
        while (a < gamePlayers.size() + 1) {
            if (gamePlayer.getPlayerDecisison() > 1) {
                return gamePlayer;
            }
            position++;
            if (position == gamePlayers.size()) {
                position = 0;
            }
            gamePlayer = gamePlayers.get(position);
            a++;
        }

        return gamePlayers.get(position);
    }

    public boolean checkStep(Game game, int step) {
        List<GamePlayer> gamePlayers = gamePlayerRepository.findAllByGameIdAndStep(game.getId(), step).get();
        int a = 1;
        int decision = 2;
        for (GamePlayer gamePlayer : gamePlayers) {
            int playerDecision = gamePlayer.getPlayerDecisison();
            if (playerDecision != decision) {
                return false;
            }
        }
        return true;
    }

}
