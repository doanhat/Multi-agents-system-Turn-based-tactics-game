package main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import tools.Constants;
import tools.Player;
import tools.RankingList;
import tools.RegisterModel;

import java.util.*;

public class MainTest {
    public static void main(String[] args) throws JsonProcessingException {
        List<Player> playerList = Arrays.asList(
                new Player(new AID(),6,13,3),
                new Player(new AID(),3,20,9),
                new Player(new AID(),9,11,7),
                new Player(new AID(),9,11,7)

        );
        RankingList rankingList = new RankingList(playerList);
        Map<String,List<Player>> map = rankingList.getLevelRanking();
        System.out.println(new ObjectMapper().writeValueAsString(map));


    }
}
