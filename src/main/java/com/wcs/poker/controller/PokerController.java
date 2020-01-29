package com.wcs.poker.controller;

import com.wcs.poker.entity.Game;
import com.wcs.poker.entity.GamePlayer;
import com.wcs.poker.repository.CardRepository;
import com.wcs.poker.repository.GamePlayerRepository;
import com.wcs.poker.repository.GameRepository;
import com.wcs.poker.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

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
    public String index(){

        return "index";
    }

    @GetMapping("/newGame")
    public String newGame(HttpSession sessionUser, Model model){
        //TODO récupérer la session du joueur connecté
        Game game = new Game();
        model.addAttribute("game", game);
        //TODO créer un nouveau jeu

        //TODO : instancier 4 joueurs en plus de l'utilisateur
        //TODO : attribuer la petite blinde et la grande blind au joueur 1 et 2 puis 2 et 3 puis 3 et 4 ...
        //TODO : distribuer 2 cartes à chaque joueur

    }
}
