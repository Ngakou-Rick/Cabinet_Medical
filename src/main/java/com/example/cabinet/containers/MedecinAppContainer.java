package com.example.cabinet.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class MedecinAppContainer {

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.CONTAINER_NAME, "MedecinsContainer");

            AgentContainer medecinContainer = rt.createAgentContainer(profile);
            System.out.println("Medecins container created and connected to the main platform.");

            // Launch a couple of doctor agents
            // In a real scenario, names might come from config or args
            launchMedecinAgent("DrHouse", medecinContainer);
            launchMedecinAgent("DrGoodall", medecinContainer);

        } catch (Exception e) {
            System.err.println("Exception while starting medecin container or agents: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void launchMedecinAgent(String agentName, AgentContainer container) {
        try {
            AgentController medecinAgent = container.createNewAgent(
                    agentName, // Agent's local name
                    "com.example.cabinet.agents.MedecinAgent", // Agent's class
                    new Object[]{} // Arguments to the agent (if any)
            );
            medecinAgent.start();
            System.out.println("Agent " + agentName + " started in " + container.getContainerName());
        } catch (ControllerException e) {
            System.err.println("Error starting agent " + agentName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
