package pws.editor;

import assembly.Assembly;
import assembly.AssemblyInterface;
import assembly.GuardActionsPair;
import editor.StateMachineEditor;
import editor.StateMachinePanel;
import machinery.StateMachine;
import pws.PWSState;
import pws.PWSStateMachine;
import serializer.BinaryModelSerializer;
import utility.SVGExporter;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.*;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;

import pws.editor.PWSStateMachineEditor;
import pws.editor.PWSStateMachinePanel;
import javax.swing.JCheckBoxMenuItem;

public class PWSEditor extends JFrame {

    // private Assembly assembly;
    private PWSStateMachine pwsStateMachine;
    private StateMachineEditor baseEditor;  // Editor for the current state machine
    private PWSPanel assemblyPanel;         // Panel to manage the Assembly
    private JTabbedPane tabbedPane;         // Panel to switch between baseEditor and assemblyPanel
    private StateMachineEditor embeddedEditor = null; // single reusable embedded editor for assembly machines
    private JPanel machineEditorContainer; // promoted so removal callback can clear it
    private String embeddedMachineId = null;

    // The main PWSEditor window uses a fixed title, e.g. "PWSEditor"
    public PWSEditor(PWSStateMachine machine) {
        super("PWSEditor");
        // Use the specialized PWSStateMachine:
        if (machine instanceof PWSStateMachine) {
            this.pwsStateMachine = ((PWSStateMachine) machine).clone();
        } else {
            this.pwsStateMachine = new PWSStateMachine(machine.getName());
        }
        initComponents();
    }

    private void initComponents() {
        setJMenuBar(createMenuBar());

        // Left editor area (wrapped with a header)
        baseEditor = new PWSStateMachineEditor(pwsStateMachine, "PWSMachine");
        JPanel editorInner = new JPanel(new BorderLayout());
        editorInner.add(baseEditor.getContentPane(), BorderLayout.CENTER);

        JPanel leftWrapper = new JPanel(new BorderLayout());
        JLabel leftHeader = new JLabel("Controller", SwingConstants.CENTER);
        leftHeader.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        leftHeader.setFont(leftHeader.getFont().deriveFont(Font.BOLD));
        leftWrapper.add(leftHeader, BorderLayout.NORTH);
        leftWrapper.add(editorInner, BorderLayout.CENTER);

        // Right area: assembly list + embedded machine editor container (also with header)
        assemblyPanel = new PWSPanel(pwsStateMachine.getAssembly());

        JPanel rightTop = new JPanel(new BorderLayout());
        JLabel rightHeader = new JLabel("Assembly", SwingConstants.CENTER);
        rightHeader.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        rightHeader.setFont(rightHeader.getFont().deriveFont(Font.BOLD));
        rightTop.add(rightHeader, BorderLayout.NORTH);
        rightTop.add(assemblyPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(rightTop, BorderLayout.NORTH);

        machineEditorContainer = new JPanel(new BorderLayout());
        // Placeholder label until a machine is selected
        JLabel placeholder = new JLabel("Select an assembly machine to edit", SwingConstants.CENTER);
        machineEditorContainer.add(placeholder, BorderLayout.CENTER);

        rightPanel.add(machineEditorContainer, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrapper, rightPanel);
        split.setResizeWeight(0.7);
        getContentPane().add(split, BorderLayout.CENTER);

        // Wire selection from the assembly panel to show the selected machine in the embedded editor
        assemblyPanel.setMachineSelectionListener(new pws.editor.PWSPanel.MachineSelectionListener() {
            @Override
            public void machineSelected(String id) {
                StateMachine machine = pwsStateMachine.getAssembly().getStateMachines().get(id);
                if (machine != null) {
                    SwingUtilities.invokeLater(() -> {
                        machineEditorContainer.removeAll();
                        try {
                            String title = id + " : " + (machine.getName() != null ? machine.getName() : "");
                            if (embeddedEditor == null) {
                                embeddedEditor = new StateMachineEditor(machine, pwsStateMachine.getAssembly(), title);
                            } else {
                                embeddedEditor.bindStateMachine(machine);
                            }

                            // remember which id is currently embedded
                            embeddedMachineId = id;

                            JMenuBar mb = embeddedEditor.getJMenuBar();
                            StateMachinePanel smPanel = embeddedEditor.getStateMachinePanel();

                            JPanel wrapper = new JPanel(new BorderLayout());
                            JPanel topArea = new JPanel(new BorderLayout());
                            if (mb != null) topArea.add(mb, BorderLayout.NORTH);
                            JLabel header = new JLabel(title, SwingConstants.CENTER);
                            header.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                            header.setFont(header.getFont().deriveFont(Font.BOLD));
                            topArea.add(header, BorderLayout.SOUTH);

                            wrapper.add(topArea, BorderLayout.NORTH);
                            wrapper.add(smPanel, BorderLayout.CENTER);

                            machineEditorContainer.add(wrapper, BorderLayout.CENTER);
                            machineEditorContainer.revalidate();
                            machineEditorContainer.repaint();
                        } catch (Exception ex) {
                            machineEditorContainer.removeAll();
                            JPanel wrapper = new JPanel(new BorderLayout());
                            String title = id + " : " + (machine.getName() != null ? machine.getName() : "");
                            JLabel header = new JLabel(title, SwingConstants.CENTER);
                            header.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                            header.setFont(header.getFont().deriveFont(Font.BOLD));
                            wrapper.add(header, BorderLayout.NORTH);
                            StateMachinePanel smPanel = new StateMachinePanel(machine);
                            wrapper.add(smPanel, BorderLayout.CENTER);
                            machineEditorContainer.add(wrapper, BorderLayout.CENTER);
                            machineEditorContainer.revalidate();
                            machineEditorContainer.repaint();
                        }
                    });
                }
            }

            @Override
            public void machineRemoved(String id) {
                // If the removed machine is currently embedded, clear the right editor area
                if (id != null && id.equals(embeddedMachineId)) {
                    SwingUtilities.invokeLater(() -> {
                        if (machineEditorContainer != null) {
                            machineEditorContainer.removeAll();
                            JLabel placeholder = new JLabel("Select an assembly machine to edit", SwingConstants.CENTER);
                            machineEditorContainer.add(placeholder, BorderLayout.CENTER);
                            machineEditorContainer.revalidate();
                            machineEditorContainer.repaint();
                        }
                        embeddedMachineId = null;
                    });
                }
            }
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");

//        // Save model item (existing)
//        JMenuItem saveItem = new JMenuItem("Save");
//        saveItem.addActionListener(e -> {
//            JFileChooser fileChooser = new JFileChooser();
//            int option = fileChooser.showSaveDialog(PWSEditor.this);
//            if (option == JFileChooser.APPROVE_OPTION) {
//                String filename = fileChooser.getSelectedFile().getAbsolutePath();
//                try {
//                    BinaryModelSerializer.saveModel(pwsStateMachine, filename);
//                    JOptionPane.showMessageDialog(PWSEditor.this, "Model saved successfully.");
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(PWSEditor.this, "Error saving: " + ex.getMessage());
//                }
//            }
//        });
//        fileMenu.add(saveItem);
//
//        // Load model item (existing)
//        JMenuItem loadItem = new JMenuItem("Load");
//        loadItem.addActionListener(e -> {
//            JFileChooser fileChooser = new JFileChooser();
//            int option = fileChooser.showOpenDialog(PWSEditor.this);
//            if (option == JFileChooser.APPROVE_OPTION) {
//                String filename = fileChooser.getSelectedFile().getAbsolutePath();
//                try {
//                    Object loadedModel = BinaryModelSerializer.loadModel(filename);
//                    if (loadedModel instanceof PWSStateMachine) {
//                        pwsStateMachine = (PWSStateMachine) loadedModel;
//                        baseEditor.dispose(); // Close the previous editor if needed
//                        baseEditor = new PWSStateMachineEditor(pwsStateMachine, "PWSMachine");
//
//                        JPanel editorPanel = new JPanel(new BorderLayout());
//                        editorPanel.add(baseEditor.getContentPane(), BorderLayout.CENTER);
//                        tabbedPane.setComponentAt(0, editorPanel);
//
//                        assemblyPanel = new PWSPanel(pwsStateMachine.getAssembly());
//                        tabbedPane.setComponentAt(1, assemblyPanel);
//
//                        revalidate();
//                        repaint();
//                        JOptionPane.showMessageDialog(PWSEditor.this, "Model loaded successfully.");
//                    } else {
//                        JOptionPane.showMessageDialog(PWSEditor.this, "The selected file does not contain a valid model.");
//                    }
//                } catch (IOException | ClassNotFoundException ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(PWSEditor.this, "Error loading: " + ex.getMessage());
//                }
//            }
//        });
//        fileMenu.add(loadItem);

        // --- New Composite Save/Load for Model + Layout in a Single File ---

        // Save All (model and layout)
        JMenuItem saveAllItem = new JMenuItem("Save All");
        saveAllItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(PWSEditor.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    // Write the model first
                    oos.writeObject(pwsStateMachine);
                    // Write the layout data from the state machine panel.
                    // Note: Ensure that PWSStateMachinePanel has the method saveAnnotationsToStream.
                    ((PWSStateMachinePanel) baseEditor.getStateMachinePanel()).saveAnnotationsToStream(oos);
                    oos.flush();
                    JOptionPane.showMessageDialog(PWSEditor.this, "Model and layout saved successfully.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PWSEditor.this, "Error saving: " + ex.getMessage());
                }
            }
        });
        fileMenu.add(saveAllItem);

        // Load All (model and layout)
        JMenuItem loadAllItem = new JMenuItem("Load All");
        loadAllItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(PWSEditor.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    Object obj = ois.readObject();
                    if (obj instanceof PWSStateMachine) {
                        pwsStateMachine = (PWSStateMachine) obj;
                        baseEditor.dispose();
                        baseEditor = new PWSStateMachineEditor(pwsStateMachine, "PWSMachine");
                        JPanel editorPanel = new JPanel(new BorderLayout());
                        editorPanel.add(baseEditor.getContentPane(), BorderLayout.CENTER);
                        tabbedPane.setComponentAt(0, editorPanel);
                        assemblyPanel = new PWSPanel(pwsStateMachine.getAssembly());
                        tabbedPane.setComponentAt(1, assemblyPanel);
                        // Now load the layout data.
                        ((PWSStateMachinePanel) baseEditor.getStateMachinePanel()).loadAnnotationsFromStream(ois);
                        revalidate();
                        repaint();
                        JOptionPane.showMessageDialog(PWSEditor.this, "Model and layout loaded successfully.");
                    } else {
                        JOptionPane.showMessageDialog(PWSEditor.this, "The selected file does not contain valid data.");
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PWSEditor.this, "Error loading: " + ex.getMessage());
                }
            }
        });
        fileMenu.add(loadAllItem);

        // New: Export as SVG menu item.
        JMenuItem exportSVGItem = new JMenuItem("Export as SVG");
        exportSVGItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("SVG File", "svg"));

            if (fileChooser.showSaveDialog(PWSEditor.this)
                    == JFileChooser.APPROVE_OPTION) {

                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".svg")) {
                    file = new File(file.getAbsolutePath() + ".svg");
                }

                // 👇 QUI LA DIFFERENZA IMPORTANTE
                // Non esportiamo più l'intero editorPanel (che include il bottone),
                // ma solo il pannello della macchina a stati dal baseEditor.
                StateMachinePanel panel =
                        ((PWSStateMachineEditor) baseEditor).getStateMachinePanel();

                SVGExporter.exportPanelToSVGFile(panel, file);

                JOptionPane.showMessageDialog(PWSEditor.this,
                    "SVG file saved successfully.");
            }
        });
        fileMenu.add(exportSVGItem);

        // Exit item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // --- Edit Menu (existing items) ---
        JMenu editMenu = new JMenu("Edit");

        JMenuItem addStateItem = new JMenuItem("Add State");
        addStateItem.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(PWSEditor.this, "Enter state name:");
            if (name != null && !name.trim().isEmpty()) {
                // create state at a default top-left, then align its CENTER to the grid if enabled
                Point defaultTopLeft = new Point(50, 50);
                PWSState newState = new PWSState(name, defaultTopLeft, pwsStateMachine.getAssembly());
                pwsStateMachine.addState(newState);

                // Try to align center to grid using the active panel's grid settings
                try {
                    PWSStateMachinePanel panel = (PWSStateMachinePanel) ((PWSStateMachineEditor) baseEditor).getStateMachinePanel();
                    if (panel.isSnapToGrid()) {
                        int grid = panel.getGridSize();
                        int diameter = 50; // must match StateMachinePanel.DIAMETER
                        int radius = diameter / 2;
                        Point center = new Point(defaultTopLeft.x + radius, defaultTopLeft.y + radius);
                        int snappedCenterX = Math.round(center.x / (float) grid) * grid;
                        int snappedCenterY = Math.round(center.y / (float) grid) * grid;
                        Point newTopLeft = new Point(snappedCenterX - radius, snappedCenterY - radius);
                        newState.setPosition(newTopLeft);
                    }
                } catch (ClassCastException ex) {
                    // If casting fails, ignore snapping and leave default position.
                }

                baseEditor.getStateMachinePanel().repaint();
            }
        });
        editMenu.add(addStateItem);

        JMenuItem addInitialTransitionItem = new JMenuItem("Add initial transition");
        addInitialTransitionItem.addActionListener(e ->
                baseEditor.getStateMachinePanel().enableInitialTransitionMode());
        editMenu.add(addInitialTransitionItem);

        editMenu.addSeparator();

//        JMenuItem addTransitionItem = new JMenuItem("Aggiungi Transizione");
//        addTransitionItem.addActionListener(e -> {
//            String sourceName = JOptionPane.showInputDialog(PWSEditor.this, "Inserisci il nome dello stato sorgente:");
//            String targetName = JOptionPane.showInputDialog(PWSEditor.this, "Inserisci il nome dello stato target:");
//            if (sourceName != null && targetName != null) {
//                machinery.StateInterface source = findStateByName(sourceName);
//                machinery.StateInterface target = findStateByName(targetName);
//                if (source != null && target != null) {
//                    String trigger = JOptionPane.showInputDialog(PWSEditor.this, "Inserisci il trigger event (lascia vuoto per interna):");
//                    boolean autonomous = (trigger == null || trigger.trim().isEmpty());
//                    pws.PWSTransition newTransition = new pws.PWSTransition(source, target, autonomous, trigger);
//                    GuardActionsPair gap = ((Assembly) pwsStateMachine.getAssembly()).askForGuardAndActions();
//                    if (gap != null) {
//                        newTransition.setGuardProposition(gap.getGuard());
//                        for (assembly.Action act : gap.getActions()) {
//                            newTransition.addAction(act);
//                        }
//                    }
//                    pwsStateMachine.addTransition(newTransition);
//                    baseEditor.getStateMachinePanel().repaint();
//                } else {
//                    JOptionPane.showMessageDialog(PWSEditor.this, "Stato sorgente o target non trovato.");
//                }
//            }
//        });
//        editMenu.add(addTransitionItem);

        JMenuItem linkModeItem = new JMenuItem("Create transition (link mode)");
        linkModeItem.addActionListener(e -> baseEditor.getStateMachinePanel().enableLinkMode());
        editMenu.add(linkModeItem);

        JCheckBoxMenuItem editModeItem = new JCheckBoxMenuItem("Edit mode", true);
        editModeItem.addActionListener(e -> baseEditor.getStateMachinePanel().setShowControlHandles(editModeItem.isSelected()));
        editMenu.add(editModeItem);

        menuBar.add(editMenu);

        // --- View menu: toggle state annotations ---
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem showStateAnn = new JCheckBoxMenuItem("Show State Annotations", false);
        showStateAnn.addActionListener(e -> {
            boolean show = showStateAnn.isSelected();
            // Retrieve the PWSStateMachinePanel and toggle annotations
            PWSStateMachinePanel panel =
                (PWSStateMachinePanel)((PWSStateMachineEditor) baseEditor).getStateMachinePanel();
            panel.setShowStateAnnotations(show);
            panel.repaint();
        });
        viewMenu.add(showStateAnn);

        JCheckBoxMenuItem showGridItem = new JCheckBoxMenuItem("Show grid", true);
        showGridItem.addActionListener(e -> {
            PWSStateMachinePanel panel =
                (PWSStateMachinePanel)((PWSStateMachineEditor) baseEditor).getStateMachinePanel();
            panel.setShowGrid(showGridItem.isSelected());
            panel.repaint();
        });
        viewMenu.add(showGridItem);

        JCheckBoxMenuItem snapToGridItem = new JCheckBoxMenuItem("Snap to grid", true);
        snapToGridItem.addActionListener(e -> {
            PWSStateMachinePanel panel =
                (PWSStateMachinePanel)((PWSStateMachineEditor) baseEditor).getStateMachinePanel();
            panel.setSnapToGrid(snapToGridItem.isSelected());
        });
        viewMenu.add(snapToGridItem);

        JMenuItem gridSizeItem = new JMenuItem("Set grid size...");
        gridSizeItem.addActionListener(e -> {
            PWSStateMachinePanel panel =
                (PWSStateMachinePanel)((PWSStateMachineEditor) baseEditor).getStateMachinePanel();
            String input = JOptionPane.showInputDialog(this, "Grid size (pixels):", panel.getGridSize());
            if (input != null) {
                try {
                    int size = Integer.parseInt(input.trim());
                    if (size > 0) {
                        panel.setGridSize(size);
                        panel.repaint();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        viewMenu.add(gridSizeItem);

        // LTL Formula editor for the current assembly
        JMenuItem ltlEditorItem = new JMenuItem("LTL Editor...");
        ltlEditorItem.addActionListener(e -> {
            pws.editor.LTLFormulaEditorDialog dlg = new pws.editor.LTLFormulaEditorDialog(PWSEditor.this, pwsStateMachine.getAssembly());
            dlg.setVisible(true);
        });
        // Disabled by default (grayed out)
        ltlEditorItem.setEnabled(false);
        viewMenu.add(ltlEditorItem);

        menuBar.add(viewMenu);
        return menuBar;
    }

    private machinery.StateInterface findStateByName(String name) {
        for (machinery.StateInterface s : pwsStateMachine.getStates()) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        // Simplify logs: only show the message text
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        Logger rootLogger = Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage() + System.lineSeparator();
                }
            });
        }
        PWSStateMachine pwsStateMachine = new PWSStateMachine("Whole");

        // Here I create a state machine for adding to the assembly with id "m1"
        StateMachine stateMachine1 = new StateMachine("M1");
        pwsStateMachine.getAssembly().addStateMachine("m1", stateMachine1);
        SwingUtilities.invokeLater(() -> {
            PWSEditor editor = new PWSEditor(pwsStateMachine);
            editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            editor.setSize(1000, 600);
            editor.setLocationRelativeTo(null);
            editor.setVisible(true);
        });
    }
}