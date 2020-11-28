package tools;

import jade.core.AID;

/**
 * Modèle pour un message d'abonnement des agents
 */
public class RegisterModel extends Model {
    private AID aid;
    private String agentType;
    //public RegisterModel() { }

    public RegisterModel(AID aid, String agentType) {
        this.aid = aid;
        this.agentType = agentType;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public AID getAid() {
        return aid;
    }

    public void setAid(AID aid) {
        this.aid = aid;
    }
}
