package pws.editor;

import assembly.Assembly;
import assembly.AssemblyInterface;
import editor.StateMachineEditor;
import machinery.StateMachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class PWSPanel extends JPanel {

    private Assembly assembly;
    private DefaultListModel<String> listModel;
    private JList<String> machineList;

    public PWSPanel(Assembly assembly) {
        this.assembly = assembly;
        setLayout(new BorderLayout());
        listModel = new DefaultListModel<>();
        machineList = new JList<>(listModel);
        refreshList();
        JScrollPane scrollPane = new JScrollPane(machineList);
        add(scrollPane, BorderLayout.CENTER);

        // Listener per il doppio click: notifica il listener di selezione (se presente),
        // altrimenti apre un nuovo editor in finestra (comportamento legacy).
        machineList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // doppio click
                    String selected = machineList.getSelectedValue();
                    if (selected != null) {
                        // Assumiamo il formato "id - Nome"
                        String[] parts = selected.split(" - ");
                        if (parts.length >= 2) {
                            String id = parts[0];
                            String machineName = parts[1];
                            StateMachine machine = assembly.getStateMachines().get(id);
                            if (machine != null) {
                                if (selectionListener != null) {
                                    selectionListener.machineSelected(id);
                                } else {
                                    SwingUtilities.invokeLater(() -> {
                                        // Fallback: apri editor in finestra separata
                                        StateMachineEditor editor = new StateMachineEditor(machine, assembly, id + " : " + machineName);
                                        editor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                        editor.setSize(800, 600);
                                        editor.setLocationRelativeTo(null);
                                        editor.setVisible(true);
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });

        // Pannello di pulsanti per aggiungere, modificare o rimuovere macchine
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton removeButton = new JButton("Remove");

        addButton.addActionListener(e -> onAdd());
        editButton.addActionListener(e -> onEdit());
        removeButton.addActionListener(e -> onRemove());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Callback interface to notify when a machine is selected (double-clicked)
    public interface MachineSelectionListener {
        void machineSelected(String id);
    }

    private MachineSelectionListener selectionListener = null;

    public void setMachineSelectionListener(MachineSelectionListener l) {
        this.selectionListener = l;
    }

    public void refreshList() {
        listModel.clear();
        for (String id : assembly.getStateMachines().keySet()) {
            StateMachine machine = assembly.getStateMachines().get(id);
            listModel.addElement(id + " - " + machine.getName());
        }
    }

    private void onAdd() {
        String id = JOptionPane.showInputDialog(this, "Enter a unique identifier:");
        if (id == null || id.trim().isEmpty()) return;
        String name = JOptionPane.showInputDialog(this, "Enter the machine name:");
        if (name == null || name.trim().isEmpty()) return;

        // Controlla se esiste già una macchina con lo stesso nome
        StateMachine existingMachine = null;
        for (Map.Entry<String, StateMachine> entry : assembly.getStateMachines().entrySet()) {
            if (entry.getValue().getName().equals(name)) {
                existingMachine = entry.getValue();
                break;
            }
        }

        if (existingMachine != null) {
            // Se esiste, aggiungiamo un nuovo mapping con lo stesso oggetto
            assembly.addStateMachine(id, existingMachine);
        } else {
            // Altrimenti, creiamo una nuova macchina
            StateMachine newMachine = new StateMachine(name);
            assembly.addStateMachine(id, newMachine);
        }
        refreshList();
    }

    private void onEdit() {
        String selected = machineList.getSelectedValue();
        if (selected == null) return;
        String id = selected.split(" - ")[0];
        AssemblyMachineEditDialog dlg = new AssemblyMachineEditDialog(SwingUtilities.getWindowAncestor(this), assembly, id);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            refreshList();
        }
    }

    private void onRemove() {
        String selected = machineList.getSelectedValue();
        if (selected == null) return;
        String id = selected.split(" - ")[0];
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove the machine with identifier " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Rimuove solo il mapping corrispondente all'id selezionato
            assembly.getStateMachines().remove(id);
            refreshList();
        }
    }
}
