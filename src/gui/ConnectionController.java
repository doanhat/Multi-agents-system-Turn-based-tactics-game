package gui;

import tools.Constants;
import tools.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;



public class ConnectionController {
	String receiverArenaName;
	String receiverPlayerName;
	ConnectionAgent agent;
	@FXML
	TextArea classement; 
	@FXML
	Button showranking;
	@FXML
	ComboBox<String> arenalist;
	@FXML
	ComboBox<String> playerlist;
	@FXML
	TextArea groupa; 
	@FXML
	TextArea groupb; 
	@FXML
	TextArea arenashow; 
	@FXML
	TextArea playeractionshow; 
	String playerAction = "";
	public void setAgent(ConnectionAgent agent) {
		
		this.agent = agent;
		//initialize();
		//playerAction = "";
		System.out.println("ConnexionJadeController --> ConnexionJadeGuiAgent connected ");
	
	
	}
	public void initialize() {
		try {
		List<String> l = Arrays.asList("Mise a jour");
		
		arenalist.setItems(FXCollections.observableArrayList(l));
		playerlist.setItems(FXCollections.observableArrayList(l));
		}catch(Exception e){}
	}
	
	/*public void DR() {
		agent.messageAnswerProperty().addListener((obsv, oldv, newv) -> displayRanking(newv));
	}*/
	public void displayRanking() {
		classement.setText("Update the ranking list");
		
		if (this.agent.PlayerList!=null&&!this.agent.PlayerList.isEmpty()) {
	   String text="";
		for(Player S: this.agent.PlayerList)
		{
			text = text  + S.getAgentName() + " Level: " + S.getLevel(S)+ "\n";
			
			
		}
		classement.setText(text);
		
		
	}
	

}
	public void setArenaList() {
		//Platform.runLater(() ->{
		try {
		//List<String> l1 = this.agent.arena;
		if(this.agent.arena.size()!=2) {
			arenalist.setItems(FXCollections.observableArrayList("Mise a jour"));}
			else {
				arenalist.setItems(FXCollections.observableArrayList(this.agent.arena));
		//if( arenalist.getSelectionModel().getSelectedItem()!= "Mise a jour" ) {
				receiverArenaName = arenalist.getSelectionModel().getSelectedItem();
		if(this.agent.newarenaAgentMap.get(receiverArenaName)!=null/*||this.agent.newarenaAgentMap.get(receiverArenaName).size()!=10*/) {
			
		
		List<String> l2 = this.agent.newarenaAgentMap.get(receiverArenaName);
		
		playerlist.setItems(FXCollections.observableArrayList(l2));
			
		//if(receiverArenaName!= null&& receiverArenaName!= "Mise a jour") {
			String text1="";
			String text2="";
		for(int i = 0;i<Constants.NBR_PLAYER_PER_TEAM;i++ )
		{
			text1 = text1  + this.agent.newarenaAgentMap.get(receiverArenaName).get(i)+"\n";
			text2 = text2  + this.agent.newarenaAgentMap.get(receiverArenaName).get(i+Constants.NBR_PLAYER_PER_TEAM)+"\n";
			
		}
		groupa.setText(text1);
		
		
		groupb.setText(text2);
	    if(this.agent.arenaActionMap!=null&&this.agent.arenaActionMap.get(receiverArenaName)!= null) {
			arenashow.setText(receiverArenaName + "\n"+this.agent.arenaActionMap.get(receiverArenaName));
	    }
		
		}
			}
	}catch(Exception e){}
		//});
		
	}
	public void setPlayer() {
		//Platform.runLater(() ->{
		try {
		receiverPlayerName = playerlist.getSelectionModel().getSelectedItem();
		if(receiverPlayerName!= "Mise a jour") {
		
		
		
		 List<String> items2= Stream.of(this.agent.arenaActionMap.get(receiverArenaName).split("\n"))
        	     .map(String::trim)
        	     .collect(Collectors.toList());
		 for(String S : items2) {
			 if (S.contains(receiverPlayerName+" ")) {
				 playerAction = playerAction + S+"\n";
			 }
			 
		 }
		
		 playeractionshow.setText(playerAction);
		 playerAction ="";
			
		List<String> l2 = this.agent.newarenaAgentMap.get(receiverArenaName);
		playerlist.setItems(FXCollections.observableArrayList(l2));
			
		String text1="";
		String text2="";
			for(int i = 0;i<Constants.NBR_PLAYER_PER_TEAM;i++ )
			{
				text1 = text1  + this.agent.newarenaAgentMap.get(receiverArenaName).get(i)+"\n";
				text2 = text2  + this.agent.newarenaAgentMap.get(receiverArenaName).get(i+Constants.NBR_PLAYER_PER_TEAM)+"\n";
				
			}
		groupa.setText(text1);
        groupb.setText(text2);
        arenashow.setText(receiverArenaName + "\n"+this.agent.arenaActionMap.get(receiverArenaName));
        }
		}catch(Exception e){}
		//});
	}
		
	
}
