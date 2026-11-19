package daylog.ui;

import daylog.entry.EntryRecorder;
import daylog.task.TaskCompletion;
import daylog.task.TaskHistory;
import daylog.task.TaskName;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the window where the user writes an entry: a one line task (with autocomplete) and a four line text area.
 * It is built once and only hidden/shown afterwards, which is what makes opening it instant.
 * It contains no business logic, saving is delegated to EntryRecorder.
 */
public final class EntryDialog {

    private static final Logger log = LoggerFactory.getLogger(EntryDialog.class);
    private static final KeyStroke CTRL_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
    private static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

    private final TaskHistory taskHistory;
    private final EntryRecorder entryRecorder;
    private final JDialog dialog = new JDialog((java.awt.Frame) null, "Daylog", false);
    private final JTextField taskField = new JTextField(40);
    private final JTextArea textArea = new JTextArea(4, 40);

    /** Builds all components right away; must be called on the Swing event thread. */
    public EntryDialog(TaskHistory taskHistory, EntryRecorder entryRecorder) {
        log.trace("EntryDialog(taskHistory, entryRecorder): building the dialog once");
        this.taskHistory = taskHistory;
        this.entryRecorder = entryRecorder;
        installAutocomplete();
        buildLayout();
        installShortcuts();
    }

    /** The parent to use for other windows (the file explorer) opened on top of this dialog. */
    public JDialog window() {
        return dialog;
    }

    /**
     * Shows the dialog: the task is prefilled with the last task and selected (typing replaces it, Tab keeps it),
     * the text is cleared, and the window is brought to the front.
     */
    public void show() {
        log.trace("show(): prefilling the last task and showing the dialog");
        Optional<TaskName> lastTask = taskHistory.last();
        taskField.setText(lastTask.map(TaskName::value).orElse(""));
        taskField.selectAll();
        textArea.setText("");
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.toFront();
        taskField.requestFocusInWindow();
    }

    /** This arranges the task row, the scrollable text area and the buttons. */
    private void buildLayout() {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(textArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel taskRow = new JPanel(new BorderLayout(6, 0));
        taskRow.add(new JLabel("Task"), BorderLayout.WEST);
        taskRow.add(taskField, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save (Ctrl+Enter)");
        saveButton.addActionListener(event -> save());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(event -> dialog.setVisible(false));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        content.add(taskRow, BorderLayout.NORTH);
        content.add(textScroll, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setAlwaysOnTop(true);
        dialog.pack();
    }

    /** Ctrl+Enter saves and Escape closes, from anywhere in the dialog. */
    private void installShortcuts() {
        JComponent root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(CTRL_ENTER, "save");
        root.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                save();
            }
        });
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE, "close");
        root.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                dialog.setVisible(false);
            }
        });
    }

    /**
     * Validates the task, records the entry and closes the dialog. An invalid (blank) task turns the field red and keeps the dialog open;
     * so does a failed save (no log file chosen), so the user does not lose what was written.
     */
    private void save() {
        log.trace("save(): user asked to save");
        TaskName task;
        try {
            task = new TaskName(taskField.getText());
        } catch (IllegalArgumentException invalidTask) {
            log.debug("Task rejected: {}", invalidTask.getMessage());
            taskField.setBackground(new Color(255, 200, 200));
            taskField.requestFocusInWindow();
            return;
        }
        taskField.setBackground(UIManagerDefaults.textFieldBackground());
        boolean saved = entryRecorder.record(task, textArea.getText());
        if (saved) {
            dialog.setVisible(false);
        }
    }

    /**
     * This installs the inline autocomplete: after the user types at the end of the task field we append the rest of the most recent
     * matching task and select it, so more typing overwrites it and Tab/End keeps it. Removing text never triggers a completion.
     */
    private void installAutocomplete() {
        ((AbstractDocument) taskField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                throws BadLocationException {
                super.replace(bypass, offset, length, text, attributes);
                if (text == null || text.isEmpty()) {
                    return;
                }
                String typed = bypass.getDocument().getText(0, bypass.getDocument().getLength());
                if (offset + text.length() != typed.length()) {
                    return;
                }
                Optional<TaskName> suggestion = TaskCompletion.suggestionFor(typed, taskHistory.all());
                if (suggestion.isEmpty()) {
                    return;
                }
                String remainder = suggestion.get().value().substring(typed.length());
                super.insertString(bypass, typed.length(), remainder, attributes);
                taskField.setCaretPosition(typed.length() + remainder.length());
                taskField.moveCaretPosition(typed.length());
            }
        });
    }
}
