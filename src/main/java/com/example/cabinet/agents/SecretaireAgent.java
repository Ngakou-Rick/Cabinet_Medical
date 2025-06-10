package com.example.cabinet.agents;

import com.example.cabinet.gui.SecretaireGui;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecretaireAgent extends GuiAgent {

    private transient SecretaireGui gui;
    private List<PatientInfo> patientsEnAttente = new ArrayList<>();
    // médecin AID -> MedecinDispo
    private Map<AID, MedecinDispo> medecinsDisponibles = new HashMap<>(); 

    public static final int CMD_ASSIGN_CONSULTATION = 1;
    public static final String SERVICE_TYPE_MEDECIN = "medecine-generale";

    // Inner class to store patient data
    public static class PatientInfo {
        public AID aid;
        public String nom;
        public String prenom;
        public String age;
        public String sexe;
        public String poids;
        public String taille;
        public String temperature;
        public String tension;
        public String rawMessageContent; // Original request content

        public PatientInfo(AID aid, String rawMessageContent) {
            this.aid = aid;
            this.rawMessageContent = rawMessageContent;
            parseInfo(rawMessageContent);
        }

        private void parseInfo(String content) {
            // Format: "CONSULTATION_REQUEST:Nom:X;Prenom:Y;Age:Z..."
            String data = content.substring("CONSULTATION_REQUEST:".length());
            String[] parts = data.split(";");
            for (String part : parts) {
                String[] keyValue = part.split(":", 2);
                if (keyValue.length == 2) {
                    switch (keyValue[0]) {
                        case "Nom": this.nom = keyValue[1]; break;
                        case "Prenom": this.prenom = keyValue[1]; break;
                        case "Age": this.age = keyValue[1]; break;
                        case "Sexe": this.sexe = keyValue[1]; break;
                        case "Poids": this.poids = keyValue[1]; break;
                        case "Taille": this.taille = keyValue[1]; break;
                        case "Temperature": this.temperature = keyValue[1]; break;
                        case "Tension": this.tension = keyValue[1]; break;
                    }
                }
            }
        }
        @Override
        public String toString() { return nom + " " + prenom; }
    }

    // Inner class for doctor availability
    public static class MedecinDispo {
        public AID aid;
        public String nom; // Doctor's name (from service registration or message)
        public boolean estDisponible;
        public String patientActuel; // Name of patient if busy

        public MedecinDispo(AID aid, String nom, boolean estDisponible) {
            this.aid = aid;
            this.nom = nom;
            this.estDisponible = estDisponible;
            this.patientActuel = "";
        }
        @Override
        public String toString() { return nom + (estDisponible ? " (Disponible)" : " (Occupé avec " + patientActuel + ")"); }
    }

    @Override
    protected void setup() {
        System.out.println("SecretaireAgent " + getAID().getName() + " is ready.");
        gui = new SecretaireGui(this);
        gui.setSecretaireInfo(getLocalName(), getAID().getName()); // Display secretary info
        gui.setVisible(true);

        // Register the secretary service (optional, but good practice)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("gestion-cabinet");
        sd.setName("Service de Secretariat Medical");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + ": Service 'gestion-cabinet' registered with DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Behaviour to receive consultation requests from patients
        addBehaviour(new ReceiveConsultationRequestBehaviour());
        // Behaviour to receive availability updates from doctors
        addBehaviour(new ReceiveMedecinAvailabilityBehaviour());
        // Behaviour to periodically search for doctors (optional, could be on demand)
        // addBehaviour(new TickerBehaviour(this, 15000) { // every 15 seconds
        //     protected void onTick() {
        //         searchForMedecins();
        //     }
        // });
        searchForMedecins(); // Initial search
    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {
        if (ge.getType() == CMD_ASSIGN_CONSULTATION) {
            PatientInfo patient = (PatientInfo) ge.getParameter(0);
            MedecinDispo medecin = (MedecinDispo) ge.getParameter(1);
            String heure = (String) ge.getParameter(2);
            String lieu = (String) ge.getParameter(3);
            assignConsultation(patient, medecin, heure, lieu);
        }
    }

    private void assignConsultation(PatientInfo patient, MedecinDispo medecin, String heure, String lieu) {
        System.out.println(getLocalName() + ": Assigning " + patient.prenom + " " + patient.nom + " to Dr. " + medecin.nom);

        // 1. Inform Patient
        ACLMessage informPatient = new ACLMessage(ACLMessage.INFORM);
        informPatient.addReceiver(patient.aid);
        String patientMsgContent = String.format("ASSIGNMENT_DETAILS:MedecinAID:%s;NomMedecin:%s;Lieu:%s;Heure:%s",
                                             medecin.aid.getName(), medecin.nom, lieu, heure);
        informPatient.setContent(patientMsgContent);
        send(informPatient);

        // 2. Inform Doctor
        ACLMessage informMedecin = new ACLMessage(ACLMessage.REQUEST); // Or INFORM based on MedecinAgent's expectation
        informMedecin.addReceiver(medecin.aid);
        // Send patient's original info string + AID
        String medecinMsgContent = String.format("PATIENT_ASSIGNED:PatientAID:%s;%s",
                                             patient.aid.getName(), patient.rawMessageContent.substring("CONSULTATION_REQUEST:".length()));
        informMedecin.setContent(medecinMsgContent);
        send(informMedecin);

        // Update local lists and GUI
        patientsEnAttente.remove(patient);
        medecin.estDisponible = false;
        medecin.patientActuel = patient.nom + " " + patient.prenom;
        gui.removePatientFromQueue(patient);
        gui.updateMedecinStatus(medecin);
        gui.displayMessage("Consultation assignée: " + patient.nom + " avec Dr. " + medecin.nom, false);
    }

    private class ReceiveConsultationRequestBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            // Optionally, filter by ontology or conversation ID if patients use them
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                AID patientAID = msg.getSender();
                System.out.println(getLocalName() + ": Received consultation request from " + patientAID.getName() + ": " + content);

                if (content != null && content.startsWith("CONSULTATION_REQUEST:")) {
                    PatientInfo patientInfo = new PatientInfo(patientAID, content);
                    patientsEnAttente.add(patientInfo);
                    gui.addPatientToQueue(patientInfo);
                    gui.displayMessage("Nouvelle demande de " + patientInfo.nom + " " + patientInfo.prenom, false);
                } else {
                    // Handle other types of REQUEST messages if necessary
                    System.out.println(getLocalName() + ": Received unknown REQUEST: " + content);
                }
            } else {
                block();
            }
        }
    }

    private class ReceiveMedecinAvailabilityBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            // Filter for availability messages, e.g., by ontology or specific content
            // For now, we assume any INFORM from a known doctor might be availability
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AID medecinAID = msg.getSender();
                String content = msg.getContent();
                System.out.println(getLocalName() + ": Received INFORM from " + medecinAID.getName() + ": " + content);

                if (medecinsDisponibles.containsKey(medecinAID)) {
                    if (content != null && content.startsWith("AVAILABILITY_UPDATE:")) {
                        // Format: "AVAILABILITY_UPDATE:Disponible" or "AVAILABILITY_UPDATE:Occupe:PatientX"
                        String statusPart = content.substring("AVAILABILITY_UPDATE:".length());
                        MedecinDispo medecin = medecinsDisponibles.get(medecinAID);
                        if (statusPart.equalsIgnoreCase("Disponible")) {
                            medecin.estDisponible = true;
                            medecin.patientActuel = "";
                        } else if (statusPart.startsWith("Occupe:")) {
                            medecin.estDisponible = false;
                            medecin.patientActuel = statusPart.substring("Occupe:".length());
                        }
                        gui.updateMedecinStatus(medecin);
                        gui.displayMessage("Disponibilité Dr. " + medecin.nom + " mise à jour.", false);
                    }
                } else {
                    // Potentially a new doctor if not discovered by DF yet, or other INFORM
                    // If it's a new doctor announcing themselves, could add them here
                     System.out.println(getLocalName() + ": Received INFORM from unknown sender or non-availability msg: " + medecinAID.getName());
                }
            } else {
                block();
            }
        }
    }

    private void searchForMedecins() {
        System.out.println(getLocalName() + ": Searching for doctor services...");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE_MEDECIN); // Doctors should register with this type
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            // gui.clearMedecinsTable(); // Clear old entries before adding new ones
            // medecinsDisponibles.clear(); // Or update existing ones

            System.out.println(getLocalName() + ": Found " + result.length + " doctors.");
            for (DFAgentDescription dfad : result) {
                AID medecinAID = dfad.getName();
                // Assuming doctor's name is part of the service name or AID local name
                String medecinNom = medecinAID.getLocalName(); // Default to local name
                // Iterate through services to find a more descriptive name if provided
                // jade.domain.FIPAAgentManagement.ServiceDescription[] services = dfad.getAllServices();
                // if (services.hasMoreElements()) { medecinNom = ((ServiceDescription)services.nextElement()).getName(); }

                if (!medecinsDisponibles.containsKey(medecinAID)) {
                    MedecinDispo newMedecin = new MedecinDispo(medecinAID, medecinNom, true); // Assume available initially
                    medecinsDisponibles.put(medecinAID, newMedecin);
                    gui.addMedecinToList(newMedecin);
                    System.out.println(getLocalName() + ": Added new doctor to list: " + medecinNom);
                } else {
                    // Optionally update existing doctor's info if it can change in DF
                    // MedecinDispo existingMedecin = medecinsDisponibles.get(medecinAID);
                    // existingMedecin.nom = medecinNom; // Update name if necessary
                    // gui.updateMedecinStatus(existingMedecin);
                     System.out.println(getLocalName() + ": Doctor " + medecinNom + " already in list.");
                }
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        // Deregister from DF
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + ": Service 'gestion-cabinet' deregistered from DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        if (gui != null) {
            gui.dispose();
        }
        System.out.println("SecretaireAgent " + getAID().getName() + " terminating.");
    }
}
