package com.example.cabinet.agents;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import com.example.cabinet.gui.PatientGui;

public class PatientAgent extends GuiAgent {

    private transient PatientGui gui;
    private AID secretaireAID; // To be discovered or set
    private AID medecinAID; // Assigned by secretaire
    private String consultationDetails; // e.g., "Dr. Smith, Room 3, 10:00 AM"

    public static final int CMD_REQUEST_CONSULTATION = 1;
    public static final int CMD_SEND_SYMPTOMS = 2;
    public static final int CMD_SEND_PRESCRIPTION_TO_PHARMACIE = 3;
    public static final int CMD_PAY_FOR_MEDICATION = 4;

    public static final String SERVICE_TYPE_PHARMACIE = "pharmacie";

    @Override
    protected void setup() {
        System.out.println("PatientAgent " + getAID().getName() + " is ready.");

        // Initialize GUI
        gui = new PatientGui(this);
        gui.setVisible(true);

        // For simplicity, let's assume SecretaireAgent is named "secretaire"
        // In a real system, use DFService for discovery
        secretaireAID = new AID("secretaire", AID.ISLOCALNAME);

        // Add behaviour to listen for messages from Secretaire (consultation assignment)
        addBehaviour(new ReceiveConsultationAssignmentBehaviour());

        // Add behaviour to listen for messages from Medecin (diagnosis/prescription)
        addBehaviour(new ReceiveDiagnosisBehaviour());

        // Add behaviour to periodically search for pharmacies
        addBehaviour(new TickerBehaviour(this, 10000) { // search every 10 seconds
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType(SERVICE_TYPE_PHARMACIE);
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    AID[] pharmacies = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        pharmacies[i] = result[i].getName();
                    }
                    if (gui != null) {
                        gui.updatePharmacieList(pharmacies);
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        // Behaviour to listen for notifications from the pharmacy
        addBehaviour(new ReceivePharmacyNotificationBehaviour());
    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {
        int command = ge.getType();
        switch (command) {
            case CMD_REQUEST_CONSULTATION:
                String patientInfo = (String) ge.getParameter(0);
                requestConsultation(patientInfo);
                break;
            case CMD_SEND_SYMPTOMS:
                if (medecinAID != null) {
                    String symptoms = (String) ge.getParameter(0);
                    sendSymptomsToMedecin(symptoms);
                } else {
                    gui.displayMessage("Erreur: Aucun médecin n'est encore assigné.", true);
                }
                break;
            case CMD_SEND_PRESCRIPTION_TO_PHARMACIE:
                String pharmacieName = (String) ge.getParameter(0);
                String diagnostic = (String) ge.getParameter(1);
                String prescription = (String) ge.getParameter(2);
                sendPrescriptionToPharmacie(pharmacieName, diagnostic, prescription);
                break;
            case CMD_PAY_FOR_MEDICATION:
                String pharmacieNameToPay = (String) ge.getParameter(0);
                confirmPaymentToPharmacie(pharmacieNameToPay);
                break;
        }
    }

    private void requestConsultation(String patientInfo) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                System.out.println(getLocalName() + ": Sending consultation request to " + secretaireAID.getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(secretaireAID);
                msg.setContent("CONSULTATION_REQUEST:" + patientInfo);
                // Set a conversation ID to track this request if needed
                // msg.setConversationId("consultation-request-" + System.currentTimeMillis());
                send(msg);
                gui.displayMessage("Demande de consultation envoyée à la secrétaire.", false);
            }
        });
    }

    private void sendSymptomsToMedecin(String symptoms) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                if (medecinAID == null) {
                    System.err.println(getLocalName() + ": Cannot send symptoms, MedecinAID is null.");
                    gui.displayMessage("Erreur: Impossible d'envoyer les symptômes, médecin non défini.", true);
                    return;
                }
                System.out.println(getLocalName() + ": Sending symptoms to " + medecinAID.getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(medecinAID);
                msg.setContent("SYMPTOMS:" + symptoms);
                // msg.setConversationId("symptoms-patient-" + getAID().getLocalName()); // Could be useful
                send(msg);
                gui.displayMessage("Symptômes envoyés au Dr. " + medecinAID.getLocalName(), false);
            }
        });
    }

    private class ReceiveConsultationAssignmentBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            // Optionally, filter by conversation ID or ontology if Secretaire uses them
            // MessageTemplate mt = MessageTemplate.and(
            //     MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            //     MessageTemplate.MatchSender(secretaireAID) // Ensure it's from the known secretary
            // );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                System.out.println(getLocalName() + ": Received message from " + msg.getSender().getName() + ": " + content);

                if (content != null && content.startsWith("ASSIGNMENT_DETAILS:")) {
                    consultationDetails = content.substring("ASSIGNMENT_DETAILS:".length());
                    // Extract MedecinAID from the message content
                    // Example format: "MedecinAID:doctor1@Platform;Lieu:Cab1;Heure:10h00"
                    try {
                        String[] parts = consultationDetails.split(";");
                        String medecinAIDString = "";
                        for (String part : parts) {
                            if (part.startsWith("MedecinAID:")) {
                                medecinAIDString = part.substring("MedecinAID:".length());
                                break;
                            }
                        }
                        if (!medecinAIDString.isEmpty()) {
                            medecinAID = new AID(medecinAIDString, AID.ISGUID);
                            medecinAID.addUserDefinedSlot("slot_address", msg.getSender().getAddressesArray()[0]); // Copy address

                            System.out.println(getLocalName() + ": Assigned to doctor: " + medecinAID.getName());
                            gui.displayConsultationDetails(consultationDetails, medecinAID.getLocalName());
                            gui.displayMessage("Consultation assignée: " + consultationDetails, false);
                        } else {
                             System.err.println(getLocalName() + ": Could not parse MedecinAID from assignment: " + consultationDetails);
                             gui.displayMessage("Erreur: Informations du médecin non trouvées dans l'assignation.", true);
                        }
                    } catch (Exception e) {
                        System.err.println(getLocalName() + ": Error parsing MedecinAID from assignment: " + e.getMessage());
                        gui.displayMessage("Erreur lors du traitement de l'assignation.", true);
                    }
                } else {
                    System.out.println(getLocalName() + ": Received other INFORM message: " + content);
                }
            } else {
                block();
            }
        }
    }

    private class ReceiveDiagnosisBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (medecinAID == null) {
                block();
                return; // Don't process until a doctor is assigned
            }

            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchSender(medecinAID) // Only messages from the assigned doctor
            );

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                System.out.println(getLocalName() + ": Received message from " + msg.getSender().getName() + ": " + content);

                if (content != null && content.startsWith("DIAGNOSIS_PRESCRIPTION_RECOMMENDATIONS:")) {
                    String diagnosisInfo = content.substring("DIAGNOSIS_PRESCRIPTION_RECOMMENDATIONS:".length());
                    // diagnosisInfo format: "Diagnostic:Flu;Ordonnance:Pills;Recommandations:Rest"
                    String diagnostic = "Non spécifié";
                    String ordonnance = "Non spécifié";
                    String recommendations = "Non spécifié";

                    String[] parts = diagnosisInfo.split(";");
                    for (String part : parts) {
                        String[] keyValue = part.split(":", 2);
                        if (keyValue.length == 2) {
                            switch (keyValue[0]) {
                                case "Diagnostic":
                                    diagnostic = keyValue[1];
                                    break;
                                case "Ordonnance":
                                    ordonnance = keyValue[1];
                                    break;
                                case "Recommandations":
                                    recommendations = keyValue[1];
                                    break;
                            }
                        }
                    }
                    gui.displayDiagnosisAndPrescription(diagnostic, ordonnance, recommendations);
                    gui.displayMessage("Diagnostic, ordonnance et recommandations reçus du médecin.", false);
                } else if (content != null && content.startsWith("ASSIGNMENT_DETAILS:")) {
                    // This is handled by ReceiveConsultationAssignmentBehaviour, but good to be aware
                    // Potentially, could use different message ontologies or conversation IDs to differentiate
                     System.out.println(getLocalName() + ": Received assignment, but should be handled by another behavior.");
                } else {
                    System.out.println(getLocalName() + ": Received other INFORM from doctor (or other): " + content);
                }
            } else {
                block();
            }
        }
    }

    private void sendPrescriptionToPharmacie(String pharmacieName, String diagnostic, String prescription) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                AID pharmacieAID = new AID(pharmacieName, AID.ISLOCALNAME);
                System.out.println(getLocalName() + ": Sending prescription to " + pharmacieAID.getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(pharmacieAID);
                String patientName = getLocalName();
                String content = String.format("SEND_PRESCRIPTION_TO_PHARMACIE:PatientName:%s;Diagnostic:%s;Prescription:%s",
                    patientName, diagnostic, prescription);
                msg.setContent(content);
                send(msg);
            }
        });
    }

    private void confirmPaymentToPharmacie(String pharmacieName) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                AID pharmacieAID = new AID(pharmacieName, AID.ISLOCALNAME);
                System.out.println(getLocalName() + ": Sending payment confirmation to " + pharmacieAID.getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
                msg.addReceiver(pharmacieAID);
                msg.setContent("PAYMENT_CONFIRMED:" + getLocalName());
                send(msg);
            }
        });
    }

    private class ReceivePharmacyNotificationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = new MessageTemplate(msg ->
                msg.getPerformative() == ACLMessage.INFORM &&
                msg.getContent() != null &&
                (msg.getContent().startsWith("MEDICATION_READY:") || msg.getContent().startsWith("PRESCRIPTION_RECEIVED_ACK:"))
            );

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String content = msg.getContent();
                AID sender = msg.getSender();
                System.out.println(getLocalName() + ": Received notification from " + sender.getLocalName() + ": " + content);

                if (content.startsWith("MEDICATION_READY:")) {
                    String pharmacieName = content.substring("MEDICATION_READY:".length());
                    if (gui != null) {
                        gui.displayPharmacyNotification("Votre médication est prête à la pharmacie " + pharmacieName + ".", pharmacieName);
                    }
                } else if (content.startsWith("PRESCRIPTION_RECEIVED_ACK:")) {
                     if (gui != null) {
                        gui.displayPharmacyNotification(content.substring("PRESCRIPTION_RECEIVED_ACK:".length()), null);
                    }
                }
            } else {
                block();
            }
        }
    }

    @Override
    protected void takeDown() {
        if (gui != null) {
            gui.dispose();
        }
        System.out.println("PatientAgent " + getAID().getName() + " terminating.");
    }
}
