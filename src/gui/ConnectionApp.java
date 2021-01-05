package gui;
import main.GameBoot;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import static tools.Constants.CONNECTION_DF;


public class ConnectionApp extends Application {
	public ContainerController containerController;
	public static String SECONDARY_PROPERTIES_FILE = "properties/second.properties";
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("UIGame.fxml"));
        Pane root = (Pane) loader.load();
        // Contrôleur de l'application
        ConnectionController app_controller = loader.getController();
        createAgentContainer(app_controller);
        stage.setTitle("Game Matchmaker");
        Scene scene = new Scene(root,500,300);
        stage.setScene(scene);
        stage.setOnCloseRequest(evt -> stopAgentContainer());
        stage.show();	
	}
	/**
	 * Crée le container secondaire et l'agent connexion
	 * l'agent connexion doit connaître le contrôleur de l'application
	 */
	public void createAgentContainer(ConnectionController controller) {
		Runtime rt = Runtime.instance();
		ProfileImpl p = null;
		try {
			p = new ProfileImpl(SECONDARY_PROPERTIES_FILE);
			ContainerController cc = rt.createAgentContainer(p);
			GameBoot g = new GameBoot();
			g.startWithProfile();
			cc.createNewAgent(CONNECTION_DF,"gui.ConnectionAgent", new Object[]{controller}).start();
			
			containerController = cc;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	// Quand on ferme l'application on détruit le nouveau container
	// et le SMA reste en place
	public void stopAgentContainer() {
		try {
			containerController.kill();
			Thread.sleep(500);
			System.exit(0);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		launch(args);
	}

}


