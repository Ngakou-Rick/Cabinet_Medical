package com.example.cabinet.gui;

import com.example.cabinet.agents.PatientAgent;
import jade.gui.GuiEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import jade.core.AID;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class PatientGui extends JFrame {

    private PatientAgent myAgent;

    // Tab 1: Consultation Request
    private JTextField nomField, prenomField, ageField, poidsField, tailleField, temperatureField, tensionField;
    private JComboBox<String> sexeComboBox;
    private JButton requestConsultationButton;
    private JTable assignedConsultationsTable;
    private DefaultTableModel assignedConsultationsTableModel;

    // Tab 2: Symptoms & Treatment
    private JCheckBox fievreCheckBox, touxCheckBox, fatigueCheckBox, mauxDeTeteCheckBox, nauseesCheckBox;
    private JTextField autresSymptomesField;
    private JButton sendSymptomsButton;
    private JTextArea recommendationsArea, ordonnanceArea, diagnosticArea;
    private JLabel medecinAssigneLabel;

    // Pharmacy related UI elements
    private JComboBox<String> pharmacieComboBox;
    private JButton sendToPharmacieButton;
    private JTextArea pharmacieStatusArea;

    private JTabbedPane tabbedPane;

    public PatientGui(PatientAgent agent) {
        super("Interface Patient: " + agent.getLocalName());
        this.myAgent = agent;

        GuiUtils.applyNimbusLookAndFeel();
        initComponents();
        pack();
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose on close to trigger agent's takeDown
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                myAgent.doDelete(); // Tell the agent to terminate
            }
        });
    }

    private void initComponents() {
        getContentPane().setBackground(GuiUtils.COLOR_BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(GuiUtils.FONT_PRIMARY);

        // Tab 1: Demander une Consultation
        JPanel consultationPanel = createConsultationRequestPanel();
        tabbedPane.addTab("Demander une Consultation", null, consultationPanel, "Soumettre une demande de consultation");

        // Tab 2: Mes Symptômes & Traitement
        JPanel symptomsPanel = createSymptomsTreatmentPanel();
        tabbedPane.addTab("Mes Symptômes & Traitement", null, symptomsPanel, "Envoyer les symptômes et voir le traitement");

        add(tabbedPane, BorderLayout.CENTER);

        // Initially, disable symptoms tab/button until a doctor is assigned
        sendSymptomsButton.setEnabled(false);
        tabbedPane.setEnabledAt(1, false); // Disable symptoms tab initially
        sendToPharmacieButton.setEnabled(false); // Also disable the pharmacy button
    }

    private JPanel createConsultationRequestPanel() {
        JPanel mainPanel = GuiUtils.createMainPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = GuiUtils.createCardPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Informations Personnelles");
        GuiUtils.styleTitleLabel(titleLabel);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        // Nom
        addFormField(formPanel, gbc, "Nom:", nomField = new JTextField(20), 0, 1);
        // Prénom
        addFormField(formPanel, gbc, "Prénom:", prenomField = new JTextField(20), 2, 1);
        // Âge
        addFormField(formPanel, gbc, "Âge:", ageField = new JTextField(5), 0, 2);
        // Sexe
        sexeComboBox = new JComboBox<>(new String[]{"Masculin", "Féminin", "Autre"});
        sexeComboBox.setFont(GuiUtils.FONT_TEXT_FIELD);
        addFormField(formPanel, gbc, "Sexe:", sexeComboBox, 2, 2);
        // Poids
        addFormField(formPanel, gbc, "Poids (kg):", poidsField = new JTextField(5), 0, 3);
        // Taille
        addFormField(formPanel, gbc, "Taille (cm):", tailleField = new JTextField(5), 2, 3);
        // Température
        addFormField(formPanel, gbc, "Température (°C):", temperatureField = new JTextField(5), 0, 4);
        // Tension
        addFormField(formPanel, gbc, "Tension (mmHg):", tensionField = new JTextField(7), 2, 4);

        requestConsultationButton = new JButton("Demander Consultation");
        GuiUtils.styleButton(requestConsultationButton);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(requestConsultationButton, gbc);

        requestConsultationButton.addActionListener(_ -> {
            // Basic validation (can be enhanced)
            if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom et le prénom sont requis.", "Erreur de Saisie", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String patientInfo =
                "Nom:" + nomField.getText().trim() +
                ";Prenom:" + prenomField.getText().trim() +
                ";Age:" + ageField.getText().trim() +
                ";Sexe:" + sexeComboBox.getSelectedItem().toString() +
                ";Poids:" + poidsField.getText().trim() +
                ";Taille:" + tailleField.getText().trim() +
                ";Temperature:" + temperatureField.getText().trim() +
                ";Tension:" + tensionField.getText().trim();

            GuiEvent ge = new GuiEvent(this, PatientAgent.CMD_REQUEST_CONSULTATION);
            ge.addParameter(patientInfo);
            myAgent.postGuiEvent(ge);
        });

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Assigned Consultations Table
        JPanel tablePanel = GuiUtils.createCardPanel();
        tablePanel.setLayout(new BorderLayout(5,5));
        JLabel assignedTitle = new JLabel("Mes Consultations Assignées");
        GuiUtils.styleSubtitleLabel(assignedTitle);
        assignedTitle.setHorizontalAlignment(SwingConstants.CENTER);
        tablePanel.add(assignedTitle, BorderLayout.NORTH);

        String[] columnNames = {"Médecin", "ID Médecin", "Lieu", "Heure"};
        assignedConsultationsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        assignedConsultationsTable = new JTable(assignedConsultationsTableModel);
        JScrollPane scrollPane = new JScrollPane(assignedConsultationsTable);
        GuiUtils.styleTable(assignedConsultationsTable, scrollPane);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent component, int x, int y) {
        JLabel label = new JLabel(labelText);
        GuiUtils.styleRegularLabel(label);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = x + 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        if (component instanceof JTextField) {
            GuiUtils.styleTextField((JTextField) component);
        } else if (component instanceof JComboBox) {
            component.setFont(GuiUtils.FONT_TEXT_FIELD);
        }
        panel.add(component, gbc);
    }

    private JPanel createSymptomsTreatmentPanel() {
        JPanel mainPanel = GuiUtils.createMainPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Symptoms Input Panel
        JPanel symptomsInputPanel = GuiUtils.createCardPanel();
        symptomsInputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel symptomsTitle = new JLabel("Description des Symptômes");
        GuiUtils.styleTitleLabel(symptomsTitle);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(5,5,15,5);
        symptomsInputPanel.add(symptomsTitle, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(5,5,5,5);

        medecinAssigneLabel = new JLabel("Médecin assigné: Aucun pour le moment");
        GuiUtils.styleRegularLabel(medecinAssigneLabel);
        medecinAssigneLabel.setFont(GuiUtils.FONT_BOLD);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        symptomsInputPanel.add(medecinAssigneLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;

        JPanel checkboxesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxesPanel.setBackground(GuiUtils.COLOR_PANEL_BACKGROUND);
        fievreCheckBox = new JCheckBox("Fièvre"); styleSymptomCheckbox(fievreCheckBox);
        touxCheckBox = new JCheckBox("Toux"); styleSymptomCheckbox(touxCheckBox);
        fatigueCheckBox = new JCheckBox("Fatigue"); styleSymptomCheckbox(fatigueCheckBox);
        mauxDeTeteCheckBox = new JCheckBox("Maux de tête"); styleSymptomCheckbox(mauxDeTeteCheckBox);
        nauseesCheckBox = new JCheckBox("Nausées"); styleSymptomCheckbox(nauseesCheckBox);
        checkboxesPanel.add(fievreCheckBox); checkboxesPanel.add(touxCheckBox); checkboxesPanel.add(fatigueCheckBox);
        checkboxesPanel.add(mauxDeTeteCheckBox); checkboxesPanel.add(nauseesCheckBox);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        symptomsInputPanel.add(checkboxesPanel, gbc);

        addFormField(symptomsInputPanel, gbc, "Autres symptômes:", autresSymptomesField = new JTextField(30), 0, 3);
        ((GridBagConstraints)gbc.clone()).gridwidth = 2; // Span across for the text field

        sendSymptomsButton = new JButton("Envoyer Symptômes");
        GuiUtils.styleButton(sendSymptomsButton);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 5, 5, 5);
        symptomsInputPanel.add(sendSymptomsButton, gbc);

        sendSymptomsButton.addActionListener(e -> {
            List<String> symptomsList = new ArrayList<>();
            if (fievreCheckBox.isSelected()) symptomsList.add("Fièvre");
            if (touxCheckBox.isSelected()) symptomsList.add("Toux");
            if (fatigueCheckBox.isSelected()) symptomsList.add("Fatigue");
            if (mauxDeTeteCheckBox.isSelected()) symptomsList.add("Maux de tête");
            if (nauseesCheckBox.isSelected()) symptomsList.add("Nausées");

            String autres = autresSymptomesField.getText().trim();
            if (!autres.isEmpty()) {
                symptomsList.add("Autres: " + autres);
            }
            if (symptomsList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez décrire au moins un symptôme.", "Symptômes Requis", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String symptomsString = String.join(", ", symptomsList);

            GuiEvent ge = new GuiEvent(this, PatientAgent.CMD_SEND_SYMPTOMS);
            ge.addParameter(symptomsString);
            myAgent.postGuiEvent(ge);
        });

        mainPanel.add(symptomsInputPanel, BorderLayout.NORTH);

        // Treatment Display Panel
        JPanel treatmentPanel = GuiUtils.createCardPanel();
        treatmentPanel.setLayout(new GridLayout(0, 1, 10, 10)); // Single column, multiple rows

        diagnosticArea = createReadOnlyTextArea("Diagnostic:");
        recommendationsArea = createReadOnlyTextArea("Recommandations:");
        ordonnanceArea = createReadOnlyTextArea("Ordonnance:");

        treatmentPanel.add(createLabeledPanel("Diagnostic du Médecin", diagnosticArea));
        treatmentPanel.add(createLabeledPanel("Recommandations", recommendationsArea));
        treatmentPanel.add(createLabeledPanel("Ordonnance", ordonnanceArea));

        // Pharmacy Interaction Panel
        JPanel pharmacieInteractionPanel = new JPanel(new BorderLayout(5, 5));
        pharmacieInteractionPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        pharmacieInteractionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Interactions Pharmacie", TitledBorder.LEFT, TitledBorder.TOP, GuiUtils.FONT_SUBTITLE, GuiUtils.COLOR_ACCENT));

        JPanel pharmacieSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pharmacieSelectionPanel.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        pharmacieComboBox = new JComboBox<>();
        GuiUtils.styleComboBox(pharmacieComboBox);
        pharmacieComboBox.setToolTipText("Sélectionnez une pharmacie");
        sendToPharmacieButton = GuiUtils.styleButton(new JButton("Envoyer Prescription à la Pharmacie"));
        sendToPharmacieButton.addActionListener(this::performSendToPharmacie);
        pharmacieSelectionPanel.add(GuiUtils.styleLabel(new JLabel("Pharmacie:"), false));
        pharmacieSelectionPanel.add(pharmacieComboBox);
        pharmacieSelectionPanel.add(sendToPharmacieButton);
        pharmacieInteractionPanel.add(pharmacieSelectionPanel, BorderLayout.NORTH);

        pharmacieStatusArea = GuiUtils.styleTextArea(new JTextArea(3, 20), false);
        pharmacieStatusArea.setEditable(false);
        pharmacieInteractionPanel.add(new JScrollPane(pharmacieStatusArea), BorderLayout.CENTER);
        pharmacieStatusArea.setToolTipText("Statut des interactions avec la pharmacie");

        // Add pharmacy panel below diagnosis/prescription
        JPanel southPanelForSymptomsTab = new JPanel(new BorderLayout(5,5));
        southPanelForSymptomsTab.setBackground(GuiUtils.COLOR_BACKGROUND_LIGHT);
        southPanelForSymptomsTab.add(treatmentPanel, BorderLayout.CENTER); // Existing panel
        southPanelForSymptomsTab.add(pharmacieInteractionPanel, BorderLayout.SOUTH); // New pharmacy panel

        mainPanel.add(southPanelForSymptomsTab, BorderLayout.CENTER);
        return mainPanel;
    }

    private void styleSymptomCheckbox(JCheckBox checkBox) {
        checkBox.setFont(GuiUtils.FONT_PRIMARY);
        checkBox.setBackground(GuiUtils.COLOR_PANEL_BACKGROUND);
        checkBox.setForeground(GuiUtils.COLOR_TEXT_PRIMARY);
    }

    private JTextArea createReadOnlyTextArea(String title) {
        JTextArea textArea = new JTextArea(5, 30);
        GuiUtils.styleTextArea(textArea);
        textArea.setEditable(false);
        textArea.setBackground(GuiUtils.COLOR_BACKGROUND); // Slightly different background for read-only
        return textArea;
    }

    private JPanel createLabeledPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5,2));
        panel.setBackground(GuiUtils.COLOR_PANEL_BACKGROUND);
        JLabel label = new JLabel(labelText);
        GuiUtils.styleRegularLabel(label);
        label.setFont(GuiUtils.FONT_BOLD);
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(component), BorderLayout.CENTER);
        return panel;
    }

    // --- Methods to be called by Agent --- 

    public void displayConsultationDetails(String details, String medecinName) {
        SwingUtilities.invokeLater(() -> {
            // Example details: "MedecinAID:doc1@Platform;Lieu:Cab1;Heure:10h00"
            String medecinId = "N/A";
            String lieu = "N/A";
            String heure = "N/A";

            String[] parts = details.split(";");
            for (String part : parts) {
                if (part.startsWith("MedecinAID:")) {
                    medecinId = part.substring("MedecinAID:".length()).split("@")[0]; // Get local name part
                } else if (part.startsWith("Lieu:")) {
                    lieu = part.substring("Lieu:".length());
                } else if (part.startsWith("Heure:")) {
                    heure = part.substring("Heure:".length());
                }
            }
            assignedConsultationsTableModel.addRow(new Object[]{medecinName, medecinId, lieu, heure});
            sendSymptomsButton.setEnabled(true);
            tabbedPane.setEnabledAt(1, true); // Enable symptoms tab
            medecinAssigneLabel.setText("Médecin assigné: Dr. " + medecinName);
            tabbedPane.setSelectedIndex(1); // Switch to symptoms tab
            JOptionPane.showMessageDialog(this, "Consultation assignée avec Dr. " + medecinName + " à " + heure + " (" + lieu + ")", "Consultation Confirmée", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void displayDiagnosisAndPrescription(String diagnostic, String prescription, String recommendations) {
        SwingUtilities.invokeLater(() -> {
            diagnosticArea.setText(diagnostic != null ? diagnostic : "N/A");
            ordonnanceArea.setText(prescription != null ? prescription : "N/A");
            recommendationsArea.setText(recommendations != null ? recommendations : "N/A");
            sendToPharmacieButton.setEnabled(true); // Enable pharmacy interaction now that prescription is available
            JOptionPane.showMessageDialog(this, "Diagnostic, prescription et recommandations reçus.", "Traitement Reçu", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void displayMessage(String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, isError ? "Erreur" : "Information",
                    isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void performSendToPharmacie(ActionEvent e) {
        // TO DO: Implement sending prescription to pharmacy
    }

    public void updatePharmacieList(AID[] pharmacies) {
        SwingUtilities.invokeLater(() -> {
            pharmacieComboBox.removeAllItems();
            for (AID pharmacy : pharmacies) {
                pharmacieComboBox.addItem(pharmacy.getLocalName());
            }
        });
    }
}
