package utility;

import machinery.TransitionInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DraggableTriggerLabel extends JLabel {
    private Point initialClick;
    private TransitionInterface associatedTransition;

    // Constructor that associates this label with a transition.
    public DraggableTriggerLabel(String text, TransitionInterface associatedTransition) {
        // Use HTML to style as bold and underlined.
        super("<html><b><u>" + text + "</u></b></html>");
        setOpaque(false);  // Transparent background
        this.associatedTransition = associatedTransition;
        initDrag();
    }

    // Convenience constructor if no transition is provided.
    public DraggableTriggerLabel(String text) {
        this(text, null);
    }

    private void initDrag() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                // When clicking on the label, ensure the containing StateMachinePanel gets focus
                java.awt.Component smPanel = javax.swing.SwingUtilities.getAncestorOfClass(editor.StateMachinePanel.class, DraggableTriggerLabel.this);
                if (smPanel != null) {
                    smPanel.requestFocusInWindow();
                } else {
                    // fallback: request focus on this label's parent
                    if (getParent() != null) getParent().requestFocusInWindow();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double-click (left button) edits the trigger event text
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && associatedTransition != null) {
                    String current = associatedTransition.getTriggerEvent();
                    String input = JOptionPane.showInputDialog(DraggableTriggerLabel.this, "Edit trigger event:", current);
                    if (input != null) {
                        associatedTransition.setTriggerEvent(input);
                        setText("<html><b><u>" + input + "</u></b></html>");
                        revalidate();
                        repaint();
                    }
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getX();
                int thisY = getY();
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                int newX = thisX + xMoved;
                int newY = thisY + yMoved;
                setLocation(newX, newY);
                // Update the associated transition's trigger offset, if available.
                if (associatedTransition != null) {
                    associatedTransition.setTriggerOffset(new Point(newX, newY));
                }
            }
        });
    }
}