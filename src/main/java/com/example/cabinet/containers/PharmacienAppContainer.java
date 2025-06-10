package com.example.cabinet.containers;

import com.example.cabinet.agents.PharmacienAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class PharmacienAppContainer {

    public static final String MAIN_CONTAINER_HOST = "localhost";
    public static final String MAIN_CONTAINER_PORT = "1099";
    public static final String PHARMACIENS_CONTAINER_NAME = "PharmaciensContainer";

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, MAIN_CONTAINER_HOST);
        p.setParameter(Profile.MAIN_PORT, MAIN_CONTAINER_PORT);
        p.setParameter(Profile.CONTAINER_NAME, PHARMACIENS_CONTAINER_NAME);

        AgentContainer pharmacienContainer = rt.createAgentContainer(p);
        if (pharmacienContainer != null) {
            System.out.println("Launching " + PHARMACIENS_CONTAINER_NAME + " on platform " + MAIN_CONTAINER_HOST + ":" + MAIN_CONTAINER_PORT);
            try {
                // Launch a PharmacienAgent
                launchPharmacienAgent(pharmacienContainer, "PharmaciePrincipale");
                // You can launch more pharmacists here if needed
                // launchPharmacienAgent(pharmacienContainer, "PharmacieDeGarde");

            } catch (StaleProxyException e) {
                System.err.println("Error launching pharmacien agents: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to create pharmacien agent container.");
        }
    }

    private static void launchPharmacienAgent(AgentContainer container, String agentName) throws StaleProxyException {
        AgentController agentController = container.createNewAgent(
                agentName, // Agent's local name
                PharmacienAgent.class.getName(), // Agent's class name
                new Object[]{} // Arguments to the agent (if any)
        );
        agentController.start();
        System.out.println("PharmacienAgent " + agentName + " launched.");
    }
}
