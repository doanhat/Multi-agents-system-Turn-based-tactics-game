package main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jade.core.AID;
import tools.*;

import java.util.*;

public class MainTest {
    public static void main(String[] args) throws JsonProcessingException {
        List<Player> playerList = Arrays.asList(
                //new Player(new AID("1",true),6,13,3,new Characteristics(), attack, experience),
                //new Player(new AID("2",true),3,20,9,new Characteristics(), attack, experience),
                //new Player(new AID("3",true),9,11,7,new Characteristics(), attack, experience),
                //new Player(new AID("4",true),9,11,7,new Characteristics(), attack, experience)

        );
        RankingList rankingList = new RankingList(playerList);
        Map<String,List<Player>> map = rankingList.getLevelRanking();
        System.out.println(new ObjectMapper().writeValueAsString(map));
        System.out.println(rankingList.serialize());
        System.out.println(Model.deserialize(rankingList.serialize(),RankingList.class));

    }
}
