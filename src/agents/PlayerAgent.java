package agents;

import Messages.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.*;

import java.util.Random;

public class PlayerAgent extends Agent {
    private static final MessageTemplate reqtemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
    private static final MessageTemplate infotemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    private static final MessageTemplate restemplate = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);

    public long TimeforBattle = 1000;
    public String[] Actions = {"attaquer", "esquiver", "defendre", "lancer_un_sort", "utiliser_un_objet"};
    public boolean bataille = false;
    public Player player;
    Random rnd = new Random();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + "--> Installed");
        Characteristics characteristics = new Characteristics(
                (rnd.nextInt(10) * 10 + 1),
                (rnd.nextInt(10) * 10 + 1),
                20,
                (rnd.nextInt(10) * 3 + 1),
                (rnd.nextInt(10) * 3 + 1),
                0,
                0);
        this.player = new Player(this.getLocalName(), 0, 0, characteristics);
        //System.out.println(player);
        //Enregistrement via le DF
        DFTool.registerAgent(this, Constants.PLAYER_DF, getLocalName());

        addBehaviour(new PlayerAgentBehaviour(this));
        //addBehaviour(new SubscribeRankingAgent());
        //addBehaviour(new WaitForBattle(this, TimeforBattle));
    }

    @Override
    protected void takeDown() {
        System.out.println("---> " + getLocalName() + " : Good bye");

        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException e)
        {
            e.printStackTrace();
        }
    }

    private class WaitForBattle extends WakerBehaviour {
        public WaitForBattle(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onWake() {
            if (!bataille) {
                //System.out.println("Here battle");
                ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
                message.addReceiver(DFTool.findFirstAgent(getAgent(), Constants.MATCHMAKER_DF, Constants.MATCHMAKER_DF));
                message.setContent(player.serialize());
                message.setProtocol(Constants.PLAYER_DF);
                getAgent().send(message);
                //System.out.println(message);
                //send(Messages.Subscribe(ACLMessage.SUBSCRIBE,"MatchmakerAgent",getLocalName(),AID.ISLOCALNAME));
                addBehaviour(new WaitForArene());
            }
        }
    }


    private class WaitForArene extends Behaviour {

        @Override
        public void action() {
            ACLMessage message = receive(reqtemplate);
            if (message != null) {
                serialisation_des_statistiques_joueur car = serialisation_des_statistiques_joueur.read(message.getContent());
                car.caract(player.getCharacteristics());
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(car.toJSON());
                send(reply);
                addBehaviour(new WaitFortour());
                bataille = true;
            } else
                block();
        }

        public boolean done() {
            return bataille;
        }

        @Override
        public int onEnd() {
            return super.onEnd();
        }
    }


    private class WaitFortour extends Behaviour {

        @Override
        public void action() {
            ACLMessage message = receive(infotemplate);
            ACLMessage message_fin = receive(restemplate);
            if (message != null) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(Actions[(int) (rnd.nextDouble() * 5)]);
                send(reply);
            } else if (message_fin != null) {
                serialisation_des_statistiques_joueur car = serialisation_des_statistiques_joueur.read(message_fin.getContent());
                player.setCharacteristics(car.car);
                addBehaviour(new WaitForBattle(myAgent, 60000));
                bataille = false;
            } else
                block();
        }

        public boolean done() {
            return !bataille;
        }

        @Override
        public int onEnd() {
            return super.onEnd();
        }
    }

    public class SubscribeRankingAgent extends OneShotBehaviour {

        @Override
        public void action() {
            ACLMessage message = new ACLMessage(ACLMessage.SUBSCRIBE);
            AID receiver = DFTool.findFirstAgent(getAgent(), Constants.RANKING_DF, Constants.RANKING_DF);
            if (receiver!=null){
                message.addReceiver(receiver);
                message.setContent(player.serialize());
                message.setProtocol(Constants.PLAYER_DF);
                getAgent().send(message);
            }
        }
    }

    private class PlayerAgentBehaviour extends SequentialBehaviour {
        public PlayerAgentBehaviour(Agent a) {
            super(a);
            addSubBehaviour(new SubscribeRankingAgent());
            addSubBehaviour(new WaitForBattle(getAgent(),TimeforBattle));
        }
    }
}
