package com.example.cabinet.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class SecretaireAppContainer {

    public static final String SECRETAIRE_AGENT_NAME = "secretaire"; // Well-known name

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "SecretaireContainer");

            AgentContainer secretaireContainer = rt.createAgentContainer(profile);
            System.out.println("Secretaire container created and connected to the main platform.");

            launchSecretaireAgent(secretaireContainer);

        } catch (Exception e) {
            System.err.println("Exception while starting secretaire container or agent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void launchSecretaireAgent(AgentContainer container) {
        try {
            AgentController secretaireAgent = container.createNewAgent(
                    SECRETAIRE_AGENT_NAME, // Agent's local name
                    "com.example.cabinet.agents.SecretaireAgent", // Agent's class
                    new Object[]{} // Arguments to the agent (if any)
            );
            secretaireAgent.start();
            System.out.println("Agent " + SECRETAIRE_AGENT_NAME + " started in " + container.getContainerName());
        } catch (ControllerException e) {
            System.err.println("Error starting agent " + SECRETAIRE_AGENT_NAME + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
