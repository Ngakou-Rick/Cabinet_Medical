package com.example.cabinet.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class PatientAppContainer {

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile profile = new ProfileImpl();
            // Connect to the main container
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099"); // Default JADE port, same as MainContainerLaunch
            profile.setParameter(Profile.CONTAINER_NAME, "PatientsContainer"); // Optional: give a name to this container

            AgentContainer patientContainer = rt.createAgentContainer(profile);
            System.out.println("Patient container created and connected to the main platform.");

            // Example: Launch a few patient agents
            // In a real application, you might get patient names from args or a config file
            launchPatientAgent("Patient1", patientContainer);
            launchPatientAgent("Patient2", patientContainer);

        } catch (Exception e) {
            System.err.println("Exception while starting patient container or agents: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void launchPatientAgent(String agentName, AgentContainer container) {
        try {
            AgentController patientAgent = container.createNewAgent(
                    agentName, // Agent's local name
                    "com.example.cabinet.agents.PatientAgent", // Agent's class
                    new Object[]{} // Arguments to the agent (if any)
            );
            patientAgent.start();
            System.out.println(agentName + " started.");
        } catch (StaleProxyException e) {
            System.err.println("Error starting agent " + agentName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
