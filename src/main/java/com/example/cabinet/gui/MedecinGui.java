package com.example.cabinet.gui;

import com.example.cabinet.agents.MedecinAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MedecinGui extends JFrame {

    private MedecinAgent myAgent;

    private JLabel medecinInfoLabel;
    private JLabel availabilityStatusLabel;

    private JPanel patientInfoPanel;
    private JLabel patientNameLabel;
    private JLabel patientAgeLabel;
    private JLabel patientSexeLabel;
    private JLabel patientPoidsLabel;
    private JLabel patientTailleLabel;
    private JLabel patientTemperatureLabel;
    private JLabel patientTensionLabel;
    private JTextArea symptomsArea;

    private JTextArea diagnosticArea;
    private JTextArea prescriptionArea;
    private JButton sendDiagnosisButton;
    private JButton becomeAvailableButton;
    private JTextArea statusMessageArea;

    public MedecinGui(MedecinAgent agent) {
        this.myAgent = agent;
        setTitle("Interface Médecin - " + myAgent.getLocalName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GuiUtils.applyNimbusLookAndFeel();

        initComponents();
        pack();
        setMinimumSize(new Dimension(600, 700));
        setLocationRelativeTo(null); // Center on screen
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        // Top Panel: Medecin Info & Availability
        JPanel topPanel = new JPanel(new BorderLayout(5,5));
        topPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        medecinInfoLabel = GuiUtils.styleLabel(new JLabel("Médecin: "), false);
        availabilityStatusLabel = GuiUtils.styleLabel(new JLabel("Statut: Indisponible"), true);
        availabilityStatusLabel.setFont(GuiUtils.FONT_BOLD);
        topPanel.add(medecinInfoLabel, BorderLayout.WEST);
        topPanel.add(availabilityStatusLabel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Panel: Patient Info & Diagnosis
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        // Patient Details Panel
        patientInfoPanel = new JPanel(new BorderLayout(5,5));
        patientInfoPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        patientInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Informations du Patient Actuel", TitledBorder.LEFT, TitledBorder.TOP, GuiUtils.FONT_SUBTITLE, GuiUtils.COLOR_ACCENT));
        ((TitledBorder) patientInfoPanel.getBorder()).setTitleColor(GuiUtils.COLOR_ACCENT);

        JPanel detailsGridPanel = new JPanel(new GridBagLayout());
        detailsGridPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        patientNameLabel = GuiUtils.styleLabel(new JLabel("Nom: N/A"), false);
        patientAgeLabel = GuiUtils.styleLabel(new JLabel("Age: N/A"), false);
        patientSexeLabel = GuiUtils.styleLabel(new JLabel("Sexe: N/A"), false);
        patientPoidsLabel = GuiUtils.styleLabel(new JLabel("Poids: N/A"), false);
        patientTailleLabel = GuiUtils.styleLabel(new JLabel("Taille: N/A"), false);
        patientTemperatureLabel = GuiUtils.styleLabel(new JLabel("Température: N/A"), false);
        patientTensionLabel = GuiUtils.styleLabel(new JLabel("Tension: N/A"), false);

        gbc.gridx = 0; gbc.gridy = 0; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Nom Complet:"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientNameLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 1; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Âge:"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientAgeLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 2; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Sexe:"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientSexeLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 3; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Poids (kg):"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientPoidsLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 4; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Taille (cm):"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientTailleLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 5; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Température (°C):"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientTemperatureLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 6; detailsGridPanel.add(GuiUtils.styleLabel(new JLabel("Tension (mmHg):"), true), gbc);
        gbc.gridx = 1; detailsGridPanel.add(patientTensionLabel, gbc);

        patientInfoPanel.add(detailsGridPanel, BorderLayout.NORTH);

        symptomsArea = GuiUtils.styleTextArea(new JTextArea(5, 20), false);
        symptomsArea.setEditable(false);
        JScrollPane symptomsScrollPane = new JScrollPane(symptomsArea);
        symptomsScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Symptômes Déclarés", TitledBorder.LEFT, TitledBorder.TOP, GuiUtils.FONT_NORMAL, GuiUtils.COLOR_TEXT_NORMAL));
        patientInfoPanel.add(symptomsScrollPane, BorderLayout.CENTER);
        centerPanel.add(patientInfoPanel);

        // Diagnosis and Prescription Panel
        JPanel diagnosisPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        diagnosisPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        diagnosisPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Diagnostic et Prescription", TitledBorder.LEFT, TitledBorder.TOP, GuiUtils.FONT_SUBTITLE, GuiUtils.COLOR_ACCENT));
        ((TitledBorder) diagnosisPanel.getBorder()).setTitleColor(GuiUtils.COLOR_ACCENT);

        diagnosticArea = GuiUtils.styleTextArea(new JTextArea(5, 20), true);
        JScrollPane diagnosticScrollPane = new JScrollPane(diagnosticArea);
        diagnosticScrollPane.setBorder(BorderFactory.createTitledBorder("Diagnostic:"));
        diagnosisPanel.add(diagnosticScrollPane);

        prescriptionArea = GuiUtils.styleTextArea(new JTextArea(5, 20), true);
        JScrollPane prescriptionScrollPane = new JScrollPane(prescriptionArea);
        prescriptionScrollPane.setBorder(BorderFactory.createTitledBorder("Prescription:"));
        diagnosisPanel.add(prescriptionScrollPane);
        centerPanel.add(diagnosisPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel: Actions and Status Messages
        JPanel bottomPanel = new JPanel(new BorderLayout(10,10));
        bottomPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonsPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        sendDiagnosisButton = GuiUtils.styleButton(new JButton("Envoyer Diagnostic et Prescription"));
        sendDiagnosisButton.addActionListener(this::performSendDiagnosis);
        becomeAvailableButton = GuiUtils.styleButton(new JButton("Devenir Disponible"));
        becomeAvailableButton.addActionListener(this::performBecomeAvailable);
        buttonsPanel.add(sendDiagnosisButton);
        buttonsPanel.add(becomeAvailableButton);
        bottomPanel.add(buttonsPanel, BorderLayout.NORTH);

        statusMessageArea = GuiUtils.styleTextArea(new JTextArea(3, 20), false);
        statusMessageArea.setEditable(false);
        bottomPanel.add(new JScrollPane(statusMessageArea), BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void performSendDiagnosis(ActionEvent e) {
        String diagnostic = diagnosticArea.getText().trim();
        String prescription = prescriptionArea.getText().trim();

        if (diagnostic.isEmpty() || prescription.isEmpty()) {
            displayMessage("ERREUR: Le diagnostic et la prescription ne peuvent pas être vides.", true);
            return;
        }

        GuiEvent ge = new GuiEvent(this, MedecinAgent.CMD_SEND_DIAGNOSIS);
        ge.addParameter(diagnostic);
        ge.addParameter(prescription);
        myAgent.postGuiEvent(ge);
    }

    private void performBecomeAvailable(ActionEvent e) {
        GuiEvent ge = new GuiEvent(this, MedecinAgent.CMD_BECOME_AVAILABLE);
        myAgent.postGuiEvent(ge);
    }

    public void setMedecinInfo(String localName, String agentName) {
        SwingUtilities.invokeLater(() -> {
            medecinInfoLabel.setText("Médecin: " + localName + " (" + agentName + ")");
        });
    }

    public void setAvailability(boolean isAvailable, String patientName) {
        SwingUtilities.invokeLater(() -> {
            if (isAvailable) {
                availabilityStatusLabel.setText("Statut: Disponible");
                availabilityStatusLabel.setForeground(new Color(0, 128, 0)); // Green
                sendDiagnosisButton.setEnabled(false);
            } else {
                availabilityStatusLabel.setText("Statut: Occupé avec " + (patientName != null ? patientName : "Patient"));
                availabilityStatusLabel.setForeground(Color.RED);
                sendDiagnosisButton.setEnabled(true);
            }
        });
    }

    public void displayPatientInfo(MedecinAgent.PatientData patient) {
        SwingUtilities.invokeLater(() -> {
            patientNameLabel.setText(patient.prenom + " " + patient.nom);
            patientAgeLabel.setText(patient.age != null ? patient.age : "N/A");
            patientSexeLabel.setText(patient.sexe != null ? patient.sexe : "N/A");
            patientPoidsLabel.setText(patient.poids != null ? patient.poids : "N/A");
            patientTailleLabel.setText(patient.taille != null ? patient.taille : "N/A");
            patientTemperatureLabel.setText(patient.temperature != null ? patient.temperature : "N/A");
            patientTensionLabel.setText(patient.tension != null ? patient.tension : "N/A");
            sendDiagnosisButton.setEnabled(true);
        });
    }

    public void displaySymptoms(String symptoms) {
        SwingUtilities.invokeLater(() -> {
            symptomsArea.setText(symptoms != null ? symptoms : "Aucun symptôme supplémentaire reçu.");
        });
    }

    public void clearPatientInfo() {
        SwingUtilities.invokeLater(() -> {
            patientNameLabel.setText("Nom: N/A");
            patientAgeLabel.setText("Age: N/A");
            patientSexeLabel.setText("Sexe: N/A");
            patientPoidsLabel.setText("Poids: N/A");
            patientTailleLabel.setText("Taille: N/A");
            patientTemperatureLabel.setText("Température: N/A");
            patientTensionLabel.setText("Tension: N/A");
            symptomsArea.setText("");
            sendDiagnosisButton.setEnabled(false);
        });
    }

    public void clearDiagnosisForm() {
        SwingUtilities.invokeLater(() -> {
            diagnosticArea.setText("");
            prescriptionArea.setText("");
        });
    }

    public void displayMessage(String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            statusMessageArea.append((isError ? "[ERREUR] " : "[INFO] ") + message + "\n");
            if (isError) {
                statusMessageArea.setForeground(Color.RED);
            } else {
                statusMessageArea.setForeground(GuiUtils.COLOR_TEXT_NORMAL);
            }
            statusMessageArea.setCaretPosition(statusMessageArea.getDocument().getLength());
        });
    }
}
