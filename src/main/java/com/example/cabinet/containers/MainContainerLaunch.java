package com.example.cabinet.containers;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainContainerLaunch {

    public static void main(String[] args) {
        try {
            // Get a hold on the JADE runtime
            Runtime rt = Runtime.instance();

            // Create a default profile
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true"); // Start the RMA GUI
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099"); // Default JADE port
            profile.setParameter(Profile.PLATFORM_ID, "CabinetMedicalPlatform");

            // Create a main container
            AgentContainer mainContainer = rt.createMainContainer(profile);
            System.out.println("Main container created and RMA GUI started.");
            System.out.println("Platform Name: " + mainContainer.getPlatformName());

            // You can start other agents here if needed, or use separate container launchers
            // For example, to start the RMA agent explicitly if not started by GUI=true:
            // AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
            // rma.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
