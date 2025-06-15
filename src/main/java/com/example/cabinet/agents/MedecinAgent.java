package com.example.cabinet.agents;

import com.example.cabinet.gui.MedecinGui;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MedecinAgent extends GuiAgent {

    private transient MedecinGui gui;
    private AID secretaireAID; // To be discovered or set
    private boolean isAvailable = true;
    private PatientData currentPatient;

    public static final int CMD_SEND_DIAGNOSIS = 1;
    public static final int CMD_BECOME_AVAILABLE = 2;
    public static final String SERVICE_TYPE_MEDECIN = "medecine-generale";
    public static final String SECRETAIRE_SERVICE_TYPE = "gestion-cabinet";

    // Inner class to store patient data received from Secretaire
    public static class PatientData {
        public AID aid;
        public String nom;
        public String prenom;
        public String age;
        public String sexe;
        public String poids;
        public String taille;
        public String temperature;
        public String tension;
        public String symptomes;

        public PatientData(AID aid, String rawContentFromSecretaire) {
            this.aid = aid;
            parseInfo(rawContentFromSecretaire);
        }

        private void parseInfo(String content) {
            // Expected from Secretaire: "Nom:X;Prenom:Y;Age:Z...;Symptomes:A,B,C"
            // Or if symptoms come later: "Nom:X;Prenom:Y;Age:Z..."
            String[] parts = content.split(";");
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
                        case "Symptomes": this.symptomes = keyValue[1]; break; // If symptoms are part of initial assignment
                    }
                }
            }
        }

        @Override
        public String toString() {
            return nom + " " + prenom + " (Age: " + age + ")";
        }
    }

    @Override
    protected void setup() {
        System.out.println("MedecinAgent " + getAID().getName() + " is ready.");
        gui = new MedecinGui(this);
        gui.setMedecinInfo(getLocalName(), getAID().getName());
        gui.setVisible(true);
        gui.setAvailability(isAvailable, null);

        // Register the medical service with DF
        registerService();

        // Discover Secretaire Agent (can be done once or periodically)
        discoverSecretaire();
        // Inform secretaire of initial availability
        informSecretaireAvailability();

        // Behaviour to receive patient assignments from Secretaire
        addBehaviour(new ReceivePatientAssignmentBehaviour());
        // Behaviour to receive symptoms from Patient (if sent separately after assignment)
        addBehaviour(new ReceiveSymptomsBehaviour());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE_MEDECIN);
        sd.setName(getLocalName() + "-Service"); // Unique service name
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + ": Registered service '" + SERVICE_TYPE_MEDECIN + "' with DF.");
        } catch (FIPAException fe) {
            System.err.println(getLocalName() + ": Error registering service: " + fe.getMessage());
            fe.printStackTrace();
        }
    }

    private void discoverSecretaire() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SECRETAIRE_SERVICE_TYPE);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                secretaireAID = result[0].getName();
                System.out.println(getLocalName() + ": Found secretaire agent: " + secretaireAID.getName());
            } else {
                System.out.println(getLocalName() + ": Secretaire agent not found yet.");
                // Optionally, retry later or handle error
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void informSecretaireAvailability() {
        if (secretaireAID == null) {
            discoverSecretaire(); // Try to find it again if not found initially
            if (secretaireAID == null) {
                System.out.println(getLocalName() + ": Cannot inform secretaire, AID unknown.");
                return;
            }
        }
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.addReceiver(secretaireAID);
        String status = isAvailable ? "Disponible" : "Occupe:" + (currentPatient != null ? currentPatient.nom + " " + currentPatient.prenom : "Inconnu");
        inform.setContent("AVAILABILITY_UPDATE:" + status);
        send(inform);
        System.out.println(getLocalName() + ": Informed secretaire of availability: " + status);
    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {
        switch (ge.getType()) {
            case CMD_SEND_DIAGNOSIS:
                if (currentPatient != null) {
                    String diagnostic = (String) ge.getParameter(0);
                    String prescription = (String) ge.getParameter(1);
                    String recommendations = (String) ge.getParameter(2);
                    sendDiagnosisToPatient(currentPatient.aid, diagnostic, prescription, recommendations);
                    gui.clearDiagnosisForm();
                } else {
                    gui.displayMessage("Aucun patient n'est actuellement sélectionné pour envoyer un diagnostic.", true);
                }
                break;
            case CMD_BECOME_AVAILABLE:
                becomeAvailable();
                break;
        }
    }

    private void sendDiagnosisToPatient(AID patientAid, String diagnostic, String prescription, String recommendations) {
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(patientAid);
        reply.setContent(String.format("DIAGNOSIS_PRESCRIPTION_RECOMMENDATIONS:Diagnostic:%s;Ordonnance:%s;Recommandations:%s", diagnostic, prescription, recommendations));
        send(reply);
        gui.displayMessage("Diagnostic envoyé à " + patientAid.getLocalName(), false);
        gui.clearPatientInfo(); // Clear patient info from GUI after sending
        // currentPatient = null; // Keep current patient until explicitly made available
    }

    private void becomeAvailable() {
        isAvailable = true;
        currentPatient = null;
        gui.setAvailability(true, null);
        gui.clearPatientInfo();
        gui.clearDiagnosisForm();
        gui.displayMessage("Vous êtes maintenant disponible.", false);
        informSecretaireAvailability();
    }

    private class ReceivePatientAssignmentBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            // Message from Secretaire: REQUEST or INFORM based on SecretaireAgent's implementation
            // SecretaireAgent sends REQUEST with PATIENT_ASSIGNED:PatientAID:patientAID;Nom:X;Prenom:Y...
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                System.out.println(getLocalName() + ": Received patient assignment: " + content + " from " + msg.getSender().getName());

                if (content != null && content.startsWith("PATIENT_ASSIGNED:")) {
                    isAvailable = false;
                    String patientDataContent = content.substring("PATIENT_ASSIGNED:".length());
                    String patientAIDString = patientDataContent.substring(patientDataContent.indexOf("PatientAID:") + "PatientAID:".length()).split(";")[0];
                    AID patientAID = new AID(patientAIDString, AID.ISGUID);
                    // The rest of the string is the patient info
                    String infoPart = patientDataContent.substring(patientDataContent.indexOf(";") + 1);

                    currentPatient = new PatientData(patientAID, infoPart);
                    gui.displayPatientInfo(currentPatient);
                    gui.setAvailability(false, currentPatient.nom + " " + currentPatient.prenom);
                    gui.displayMessage("Patient " + currentPatient.nom + " " + currentPatient.prenom + " assigné.", false);
                    informSecretaireAvailability(); // Inform secretary now busy

                    // PatientAgent might send symptoms in a separate message directly to MedecinAgent
                    // Or symptoms might be included in the assignment from SecretaireAgent
                    // If symptoms are included in 'infoPart' and parsed by PatientData, they will be in currentPatient.symptomes
                    if (currentPatient.symptomes != null && !currentPatient.symptomes.isEmpty()) {
                        gui.displaySymptoms(currentPatient.symptomes);
                    }

                } else {
                    System.out.println(getLocalName() + ": Received unknown REQUEST: " + content);
                }
            } else {
                block();
            }
        }
    }

    // This behaviour is for when PatientAgent sends symptoms directly to MedecinAgent
    // after being assigned by SecretaireAgent.
    private class ReceiveSymptomsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                new MessageTemplate(new MessageTemplate.MatchExpression() {
                    public boolean match(ACLMessage msgToMatch) {
                        if (msgToMatch.getContent() != null) {
                            return msgToMatch.getContent().startsWith("SYMPTOMS:");
                        }
                        return false;
                    }
                })
            );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AID patientAID = msg.getSender();
                System.out.println(getLocalName() + ": Received symptoms from " + patientAID.getName() + ": " + msg.getContent());

                if (currentPatient != null && currentPatient.aid.getName().equals(patientAID.getName())) {
                    String symptoms = msg.getContent().substring("SYMPTOMS:".length());
                    currentPatient.symptomes = symptoms; // Update symptoms for current patient
                    gui.displaySymptoms(symptoms);
                    gui.displayMessage("Symptômes reçus de " + currentPatient.nom, false);
                } else {
                    System.out.println(getLocalName() + ": Received symptoms from an unexpected patient " + patientAID.getName() + " or no patient currently assigned.");
                    if (currentPatient != null) {
                        System.out.println(getLocalName() + ": Expected patient AID name: " + currentPatient.aid.getName() + ", Received from AID name: " + patientAID.getName());
                        System.out.println(getLocalName() + ": Expected patient AID (full): " + currentPatient.aid.toString() + ", Received from AID (full): " + patientAID.toString());
                    } else {
                        System.out.println(getLocalName() + ": currentPatient is null.");
                    }
                    // Optionally, queue it or send an error
                }
            } else {
                block();
            }
        }
    }

    @Override
    protected void takeDown() {
        // Deregister from DF
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + ": Service '" + SERVICE_TYPE_MEDECIN + "' deregistered from DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        if (gui != null) {
            gui.dispose();
        }
        System.out.println("MedecinAgent " + getAID().getName() + " terminating.");
    }
}
