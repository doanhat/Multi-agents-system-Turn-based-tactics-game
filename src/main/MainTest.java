package main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import tools.*;

import java.util.*;

public class MainTest {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        List<Player> playerList = Arrays.asList(
                new Player("p1",6,13,new Characteristics()),
                new Player("p2",3,20,new Characteristics()),
                new Player("p3",9,11,new Characteristics()),
                new Player("p4",9,11,new Characteristics())
        );
        System.out.println(om.writeValueAsString(playerList.get(0)));
        RankingList rankingList = new RankingList(playerList);
        Map<String,List<Player>> map = rankingList.getLevelRanking();
        String playerListStr = om.writeValueAsString(playerList);
        List<Player> playerListDeserialized = Model.deserializeToList(playerListStr,Player.class);
        System.out.println(playerListDeserialized);
    }
}
