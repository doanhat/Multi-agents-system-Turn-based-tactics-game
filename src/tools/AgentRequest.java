package tools;

public class AgentRequest extends Model{
    private String agentName;
    private String requestName;

    public AgentRequest() {
    }

    public AgentRequest(String agentName, String requestName) {
        this.agentName = agentName;
        this.requestName = requestName;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }
}
