package com.example.cabinet.gui;

import com.example.cabinet.agents.PharmacienAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PharmacienGui extends JFrame {

    private PharmacienAgent myAgent;

    private JLabel pharmacienInfoLabel;
    private DefaultListModel<PharmacienAgent.PrescriptionInfo> prescriptionListModel;
    private JList<PharmacienAgent.PrescriptionInfo> prescriptionList;

    private JTextArea diagnosticDetailArea;
    private JTextArea prescriptionDetailArea;
    private JLabel patientNameDetailLabel;
    private JLabel currentStatusDetailLabel;

    private JComboBox<String> statusComboBox;
    private JButton updateStatusButton;
    private JButton notifyPatientButton;
    private JTextArea statusMessageArea;

    public PharmacienGui(PharmacienAgent agent) {
        this.myAgent = agent;
        setTitle("Interface Pharmacien - " + myAgent.getLocalName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GuiUtils.applyNimbusLookAndFeel();

        initComponents();
        pack();
        setMinimumSize(new Dimension(750, 600));
        setLocationRelativeTo(null); // Center on screen
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        // Top Panel: Pharmacien Info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        pharmacienInfoLabel = GuiUtils.styleLabel(new JLabel("Pharmacien: "), false);
        topPanel.add(pharmacienInfoLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Panel: Prescription List and Details
        JSplitPane centerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPanel.setResizeWeight(0.4); // Give more space to details initially
        centerSplitPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        centerSplitPanel.setBorder(null);

        // Prescription List Panel
        JPanel listPanel = new JPanel(new BorderLayout(5,5));
        listPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        listPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Prescriptions Reçues", TitledBorder.LEFT, TitledBorder.TOP, GuiUtils.FONT_SUBTITLE, GuiUtils.COLOR_ACCENT));
        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        GuiUtils.styleJList(prescriptionList);
        prescriptionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prescriptionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedPrescriptionDetails(prescriptionList.getSelectedValue());
            }
        });
        listPanel.add(new JScrollPane(prescriptionList), BorderLayout.CENTER);
        centerSplitPanel.setLeftComponent(listPanel);

        // Prescription Details Panel
        JPanel detailsPanel = new JPanel(new BorderLayout(5,10));
        detailsPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Détails de la Prescription Sélectionnée", TitledBorder.LEFT, TitledBorder.TOP, GuiUtils.FONT_SUBTITLE, GuiUtils.COLOR_ACCENT));
        
        JPanel patientStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        patientStatusPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        patientNameDetailLabel = GuiUtils.styleLabel(new JLabel("Patient: N/A"), false);
        currentStatusDetailLabel = GuiUtils.styleLabel(new JLabel("Statut Actuel: N/A"), true);
        patientStatusPanel.add(patientNameDetailLabel);
        patientStatusPanel.add(currentStatusDetailLabel);
        detailsPanel.add(patientStatusPanel, BorderLayout.NORTH);

        JPanel textAreasPanel = new JPanel(new GridLayout(2,1,5,5));
        textAreasPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        diagnosticDetailArea = GuiUtils.styleTextArea(new JTextArea(5, 25), false);
        diagnosticDetailArea.setEditable(false);
        JScrollPane diagScrollPane = new JScrollPane(diagnosticDetailArea);
        diagScrollPane.setBorder(BorderFactory.createTitledBorder("Diagnostic Associé:"));
        textAreasPanel.add(diagScrollPane);

        prescriptionDetailArea = GuiUtils.styleTextArea(new JTextArea(8, 25), false);
        prescriptionDetailArea.setEditable(false);
        JScrollPane prescScrollPane = new JScrollPane(prescriptionDetailArea);
        prescScrollPane.setBorder(BorderFactory.createTitledBorder("Contenu de la Prescription:"));
        textAreasPanel.add(prescScrollPane);
        detailsPanel.add(textAreasPanel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,5));
        actionsPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        statusComboBox = new JComboBox<>(new String[]{"Reçue", "En préparation", "Prête", "Délivrée", "Annulée"});
        GuiUtils.styleComboBox(statusComboBox);
        updateStatusButton = GuiUtils.styleButton(new JButton("Mettre à Jour Statut"));
        updateStatusButton.addActionListener(this::performUpdateStatus);
        notifyPatientButton = GuiUtils.styleButton(new JButton("Notifier Patient (Prête)"));
        notifyPatientButton.addActionListener(this::performNotifyPatient);
        notifyPatientButton.setEnabled(false); // Enabled when status is 'Prête'

        actionsPanel.add(GuiUtils.styleLabel(new JLabel("Nouveau Statut:"), false));
        actionsPanel.add(statusComboBox);
        actionsPanel.add(updateStatusButton);
        actionsPanel.add(notifyPatientButton);
        detailsPanel.add(actionsPanel, BorderLayout.SOUTH);

        centerSplitPanel.setRightComponent(detailsPanel);
        mainPanel.add(centerSplitPanel, BorderLayout.CENTER);

        // Bottom Panel: Status Messages
        statusMessageArea = GuiUtils.styleTextArea(new JTextArea(3, 20), false);
        statusMessageArea.setEditable(false);
        mainPanel.add(new JScrollPane(statusMessageArea), BorderLayout.SOUTH);

        add(mainPanel);
        clearDetailsPanel(); // Initially no selection
    }

    private void displaySelectedPrescriptionDetails(PharmacienAgent.PrescriptionInfo pInfo) {
        SwingUtilities.invokeLater(() -> {
            if (pInfo != null) {
                patientNameDetailLabel.setText("Patient: " + (pInfo.patientName != null ? pInfo.patientName : pInfo.patientAID.getLocalName()));
                diagnosticDetailArea.setText(pInfo.diagnostic != null ? pInfo.diagnostic : "N/A");
                prescriptionDetailArea.setText(pInfo.prescription != null ? pInfo.prescription : "N/A");
                currentStatusDetailLabel.setText("Statut Actuel: " + pInfo.status);
                statusComboBox.setSelectedItem(pInfo.status);
                updateStatusButton.setEnabled(true);
                notifyPatientButton.setEnabled("Prête".equalsIgnoreCase(pInfo.status) || "Notifié (Prête)".equalsIgnoreCase(pInfo.status));
            } else {
                clearDetailsPanel();
            }
        });
    }

    private void clearDetailsPanel() {
        patientNameDetailLabel.setText("Patient: N/A");
        diagnosticDetailArea.setText("");
        prescriptionDetailArea.setText("");
        currentStatusDetailLabel.setText("Statut Actuel: N/A");
        statusComboBox.setSelectedIndex(0);
        updateStatusButton.setEnabled(false);
        notifyPatientButton.setEnabled(false);
    }

    private void performUpdateStatus(ActionEvent e) {
        PharmacienAgent.PrescriptionInfo selectedPrescription = prescriptionList.getSelectedValue();
        if (selectedPrescription == null) {
            displayMessage("ERREUR: Veuillez sélectionner une prescription à mettre à jour.", true);
            return;
        }
        String newStatus = (String) statusComboBox.getSelectedItem();
        GuiEvent ge = new GuiEvent(this, PharmacienAgent.CMD_PREPARE_MEDICATION);
        ge.addParameter(selectedPrescription);
        ge.addParameter(newStatus);
        myAgent.postGuiEvent(ge);
    }

    private void performNotifyPatient(ActionEvent e) {
        PharmacienAgent.PrescriptionInfo selectedPrescription = prescriptionList.getSelectedValue();
        if (selectedPrescription == null) {
            displayMessage("ERREUR: Veuillez sélectionner une prescription pour notifier le patient.", true);
            return;
        }
        if (!("Prête".equalsIgnoreCase(selectedPrescription.status) || "Notifié (Prête)".equalsIgnoreCase(selectedPrescription.status))) {
            displayMessage("INFO: La médication doit être marquée comme 'Prête' avant de notifier.", false);
            return;
        }
        GuiEvent ge = new GuiEvent(this, PharmacienAgent.CMD_NOTIFY_PATIENT_READY);
        ge.addParameter(selectedPrescription);
        myAgent.postGuiEvent(ge);
    }

    public void setPharmacienInfo(String localName, String agentName) {
        SwingUtilities.invokeLater(() -> {
            pharmacienInfoLabel.setText("Pharmacien: " + localName + " (" + agentName + ")");
        });
    }

    public void addPrescriptionToList(PharmacienAgent.PrescriptionInfo pInfo) {
        SwingUtilities.invokeLater(() -> {
            prescriptionListModel.addElement(pInfo);
        });
    }

    public void updatePrescriptionInList(PharmacienAgent.PrescriptionInfo pInfo) {
        SwingUtilities.invokeLater(() -> {
            int index = prescriptionListModel.indexOf(pInfo);
            if (index != -1) {
                prescriptionListModel.setElementAt(pInfo, index); // Re-set to trigger update if toString changed
                 // If the currently selected item is the one being updated, refresh details view
                if (prescriptionList.getSelectedValue() == pInfo) {
                    displaySelectedPrescriptionDetails(pInfo);
                }
            } else {
                // Should not happen if pInfo came from the list
            }
            prescriptionList.repaint();
        });
    }

    public void displayMessage(String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            statusMessageArea.append((isError ? "[ERREUR] " : "[INFO] ") + message + "\n");
            statusMessageArea.setForeground(isError ? Color.RED : GuiUtils.COLOR_TEXT_NORMAL);
            statusMessageArea.setCaretPosition(statusMessageArea.getDocument().getLength());
        });
    }
}
