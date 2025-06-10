package com.example.cabinet.gui;

import com.example.cabinet.agents.SecretaireAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SecretaireGui extends JFrame {

    private SecretaireAgent myAgent;

    private JLabel secretaireInfoLabel;
    private DefaultListModel<SecretaireAgent.PatientInfo> patientQueueModel;
    private JList<SecretaireAgent.PatientInfo> patientQueueList;
    private DefaultListModel<SecretaireAgent.MedecinDispo> medecinListModel;
    private JList<SecretaireAgent.MedecinDispo> medecinList;

    private JSpinner timeSpinner;
    private JTextField lieuTextField;
    private JButton assignButton;
    private JTextArea statusArea;

    public SecretaireGui(SecretaireAgent agent) {
        this.myAgent = agent;
        setTitle("Interface Secrétaire - " + myAgent.getLocalName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GuiUtils.applyNimbusLookAndFeel();

        initComponents();
        pack();
        setLocationRelativeTo(null); // Center on screen
        // setMinimumSize(new Dimension(700, 500));
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        // Top Panel for Secretary Info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        secretaireInfoLabel = GuiUtils.styleLabel(new JLabel("Secrétaire: "), false);
        topPanel.add(secretaireInfoLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Panel for Lists
        JPanel listPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        listPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        // Patient Queue Panel
        JPanel patientPanel = new JPanel(new BorderLayout(5,5));
        patientPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        JLabel patientTitleLabel = new JLabel("Patients en Attente");
        GuiUtils.styleTitleLabel(patientTitleLabel);
        patientPanel.add(patientTitleLabel, BorderLayout.NORTH);
        patientQueueModel = new DefaultListModel<>();
        patientQueueList = new JList<>(patientQueueModel);
        GuiUtils.styleJList(patientQueueList);
        patientPanel.add(new JScrollPane(patientQueueList), BorderLayout.CENTER);
        listPanel.add(patientPanel);

        // Medecin List Panel
        JPanel medecinPanel = new JPanel(new BorderLayout(5,5));
        medecinPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        JLabel medecinTitleLabel = new JLabel("Médecins Disponibles");
        GuiUtils.styleTitleLabel(medecinTitleLabel);
        medecinPanel.add(medecinTitleLabel, BorderLayout.NORTH);
        medecinListModel = new DefaultListModel<>();
        medecinList = new JList<>(medecinListModel);
        GuiUtils.styleJList(medecinList);
        medecinPanel.add(new JScrollPane(medecinList), BorderLayout.CENTER);
        listPanel.add(medecinPanel);

        mainPanel.add(listPanel, BorderLayout.CENTER);

        // Bottom Panel for Assignment and Status
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(GuiUtils.COLOR_BACKGROUND);

        JPanel assignmentFormPanel = new JPanel(new GridBagLayout());
        assignmentFormPanel.setBackground(GuiUtils.COLOR_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        assignmentFormPanel.add(GuiUtils.styleLabel(new JLabel("Heure (HH:mm):"), false), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new java.util.Date()); // Set current time initially
        GuiUtils.styleSpinner(timeSpinner);
        assignmentFormPanel.add(timeSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        assignmentFormPanel.add(GuiUtils.styleLabel(new JLabel("Lieu:"), false), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        lieuTextField = new JTextField(15);
        GuiUtils.styleTextField(lieuTextField, 15);
        lieuTextField.setText("Salle de consultation 1");
        assignmentFormPanel.add(lieuTextField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        assignButton = GuiUtils.styleButton(new JButton("Assigner Consultation"));
        assignButton.addActionListener(this::performAssignConsultation);
        assignmentFormPanel.add(assignButton, gbc);

        bottomPanel.add(assignmentFormPanel, BorderLayout.NORTH);

        statusArea = GuiUtils.styleTextArea(new JTextArea(5, 30), false);
        statusArea.setEditable(false);
        bottomPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void performAssignConsultation(ActionEvent e) {
        SecretaireAgent.PatientInfo selectedPatient = patientQueueList.getSelectedValue();
        SecretaireAgent.MedecinDispo selectedMedecin = medecinList.getSelectedValue();

        if (selectedPatient == null) {
            displayMessage("ERREUR: Veuillez sélectionner un patient.", true);
            return;
        }
        if (selectedMedecin == null) {
            displayMessage("ERREUR: Veuillez sélectionner un médecin.", true);
            return;
        }
        if (!selectedMedecin.estDisponible) {
            displayMessage("ERREUR: Dr. " + selectedMedecin.nom + " n'est pas disponible.", true);
            return;
        }

        java.util.Date selectedTime = (java.util.Date) timeSpinner.getValue();
        LocalTime localTime = selectedTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        String heure = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        String lieu = lieuTextField.getText().trim();

        if (lieu.isEmpty()) {
            displayMessage("ERREUR: Veuillez spécifier un lieu.", true);
            return;
        }

        GuiEvent ge = new GuiEvent(this, SecretaireAgent.CMD_ASSIGN_CONSULTATION);
        ge.addParameter(selectedPatient);
        ge.addParameter(selectedMedecin);
        ge.addParameter(heure);
        ge.addParameter(lieu);
        myAgent.postGuiEvent(ge);
    }

    public void setSecretaireInfo(String localName, String agentName) {
        SwingUtilities.invokeLater(() -> {
            secretaireInfoLabel.setText("Secrétaire: " + localName + " (" + agentName + ")");
        });
    }

    public void addPatientToQueue(SecretaireAgent.PatientInfo patientInfo) {
        SwingUtilities.invokeLater(() -> {
            patientQueueModel.addElement(patientInfo);
        });
    }

    public void removePatientFromQueue(SecretaireAgent.PatientInfo patientInfo) {
        SwingUtilities.invokeLater(() -> {
            patientQueueModel.removeElement(patientInfo);
        });
    }

    public void addMedecinToList(SecretaireAgent.MedecinDispo medecinDispo) {
        SwingUtilities.invokeLater(() -> {
            medecinListModel.addElement(medecinDispo);
        });
    }

    public void updateMedecinStatus(SecretaireAgent.MedecinDispo medecinDispo) {
        SwingUtilities.invokeLater(() -> {
            // For JList, we might need to find and replace or rely on its toString for updates
            // A more robust way would be to remove and re-add, or use a custom ListCellRenderer
            // For simplicity, let's assume the list will refresh if the object's string representation changes
            // This often requires a repaint or a more direct model update if the object instance is the same.
            int index = medecinListModel.indexOf(medecinDispo);
            if (index != -1) {
                medecinListModel.setElementAt(medecinDispo, index); // Re-set to trigger update if toString changed
            } else {
                 // If not found, perhaps it's a new one or was removed then re-added
                 // For now, we assume it's already in the list if we're updating status
            }
            medecinList.repaint(); // Force repaint to reflect changes
        });
    }

    public void displayMessage(String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append((isError ? "[ERREUR] " : "[INFO] ") + message + "\n");
            if (isError) {
                statusArea.setForeground(Color.RED);
            } else {
                statusArea.setForeground(GuiUtils.COLOR_TEXT_NORMAL);
            }
            // Auto-scroll to the bottom
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }

    public void clearMedecinsTable() { // Renamed from clearMedecinsList for consistency if it were a table
        SwingUtilities.invokeLater(() -> {
            medecinListModel.clear();
        });
    }
}
