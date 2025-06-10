package com.example.cabinet.agents;

import com.example.cabinet.gui.PharmacienGui;
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

import java.util.ArrayList;
import java.util.List;

public class PharmacienAgent extends GuiAgent {

    private transient PharmacienGui gui;
    private List<PrescriptionInfo> receivedPrescriptions = new ArrayList<>();

    public static final int CMD_PREPARE_MEDICATION = 1;
    public static final int CMD_NOTIFY_PATIENT_READY = 2;
    public static final String SERVICE_TYPE_PHARMACIE = "pharmacie";

    // Inner class to store prescription data
    public static class PrescriptionInfo {
        public AID patientAID;
        public String patientName; // Could be sent by patient or resolved
        public String diagnostic;
        public String prescription;
        public String status; // e.g., "Reçue", "En préparation", "Prête", "Délivrée"

        public PrescriptionInfo(AID patientAID, String patientName, String diagnostic, String prescription) {
            this.patientAID = patientAID;
            this.patientName = patientName; // Assuming patient sends their name along
            this.diagnostic = diagnostic;
            this.prescription = prescription;
            this.status = "Reçue";
        }

        @Override
        public String toString() {
            return "Patient: " + (patientName != null ? patientName : patientAID.getLocalName()) +
                   " - Statut: " + status;
        }
    }

    @Override
    protected void setup() {
        System.out.println("PharmacienAgent " + getAID().getName() + " is ready.");
        gui = new PharmacienGui(this);
        gui.setPharmacienInfo(getLocalName(), getAID().getName());
        gui.setVisible(true);

        // Register the pharmacy service with DF
        registerService();

        // Behaviour to receive prescriptions from patients
        addBehaviour(new ReceivePrescriptionBehaviour());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE_PHARMACIE);
        sd.setName(getLocalName() + "-PharmacieService"); // Unique service name
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + ": Registered service '" + SERVICE_TYPE_PHARMACIE + "' with DF.");
        } catch (FIPAException fe) {
            System.err.println(getLocalName() + ": Error registering service: " + fe.getMessage());
            fe.printStackTrace();
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent ge) {
        if (ge.getType() == CMD_PREPARE_MEDICATION) {
            PrescriptionInfo prescriptionInfo = (PrescriptionInfo) ge.getParameter(0);
            String newStatus = (String) ge.getParameter(1);
            updatePrescriptionStatus(prescriptionInfo, newStatus);
        } else if (ge.getType() == CMD_NOTIFY_PATIENT_READY) {
            PrescriptionInfo prescriptionInfo = (PrescriptionInfo) ge.getParameter(0);
            notifyPatientMedicationReady(prescriptionInfo);
        }
    }

    private void updatePrescriptionStatus(PrescriptionInfo pInfo, String newStatus) {
        pInfo.status = newStatus;
        gui.updatePrescriptionInList(pInfo);
        gui.displayMessage("Statut de la prescription pour " + pInfo.patientName + " mis à jour: " + newStatus, false);
        if ("Prête".equalsIgnoreCase(newStatus)) {
            // Optionally auto-notify or enable a button to notify
            // For now, let's assume GUI has a separate button for notification
        }
    }

    private void notifyPatientMedicationReady(PrescriptionInfo pInfo) {
        if (pInfo.patientAID == null) {
            gui.displayMessage("ERREUR: AID du patient inconnu pour " + pInfo.patientName, true);
            return;
        }
        ACLMessage informMsg = new ACLMessage(ACLMessage.INFORM);
        informMsg.addReceiver(pInfo.patientAID);
        informMsg.setContent("MEDICATION_READY: Pharmacie " + getLocalName() + ": Votre médication est prête.");
        send(informMsg);
        gui.displayMessage("Patient " + pInfo.patientName + " notifié que sa médication est prête.", false);
        updatePrescriptionStatus(pInfo, "Notifié (Prête)");
    }

    private class ReceivePrescriptionBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            // Expecting a message from PatientAgent with their prescription
            // Format: "SEND_PRESCRIPTION_TO_PHARMACIE:PatientName:XYZ;Diagnostic:ABC;Prescription:PQR"
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST), // Or INFORM, depends on PatientAgent's implementation
                new MessageTemplate(new MessageTemplate.MatchExpression() {
                    public boolean match(ACLMessage msgToMatch) {
                        if (msgToMatch.getContent() != null) {
                            return msgToMatch.getContent().startsWith("SEND_PRESCRIPTION_TO_PHARMACIE:");
                        }
                        return false;
                    }
                })
            );
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                AID patientAID = msg.getSender();
                String content = msg.getContent();
                System.out.println(getLocalName() + ": Received prescription from " + patientAID.getName() + ": " + content);

                try {
                    String data = content.substring("SEND_PRESCRIPTION_TO_PHARMACIE:".length());
                    String patientName = "Inconnu";
                    String diagnostic = "N/A";
                    String prescriptionText = "N/A";

                    String[] parts = data.split(";");
                    for (String part : parts) {
                        String[] keyValue = part.split(":", 2);
                        if (keyValue.length == 2) {
                            if ("PatientName".equalsIgnoreCase(keyValue[0])) {
                                patientName = keyValue[1];
                            } else if ("Diagnostic".equalsIgnoreCase(keyValue[0])) {
                                diagnostic = keyValue[1];
                            } else if ("Prescription".equalsIgnoreCase(keyValue[0])) {
                                prescriptionText = keyValue[1];
                            }
                        }
                    }
                    
                    PrescriptionInfo pInfo = new PrescriptionInfo(patientAID, patientName, diagnostic, prescriptionText);
                    receivedPrescriptions.add(pInfo);
                    gui.addPrescriptionToList(pInfo);
                    gui.displayMessage("Nouvelle prescription reçue de " + patientName, false);

                    // Optionally send an ACK to the patient
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("PRESCRIPTION_RECEIVED_ACK: Votre prescription a bien été reçue par la pharmacie " + getLocalName());
                    send(reply);

                } catch (Exception e) {
                    System.err.println(getLocalName() + ": Error parsing prescription message: " + e.getMessage());
                    gui.displayMessage("ERREUR: Format de message de prescription incorrect de " + patientAID.getLocalName(), true);
                    // Send error reply?
                }
            } else {
                block();
            }
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println(getLocalName() + ": Service '" + SERVICE_TYPE_PHARMACIE + "' deregistered from DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        if (gui != null) {
            gui.dispose();
        }
        System.out.println("PharmacienAgent " + getAID().getName() + " terminating.");
    }
}
