package daylog.ui;

import daylog.entry.EntryRecorder;
import daylog.task.TaskCompletion;
import daylog.task.TaskHistory;
import daylog.task.TaskName;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the window where the user writes an entry: a one line task (with a drop-down of earlier tasks) and a four line text area.
 * Enter saves, Shift+Enter adds a line break. It is built once and only hidden/shown afterwards, which makes opening it instant.
 * It contains no business logic: saving is delegated to EntryRecorder and the suggestions come from TaskCompletion.
 */
public final class EntryDialog {

    private static final Logger log = LoggerFactory.getLogger(EntryDialog.class);
    private static final int MAX_SUGGESTIONS = 8;

    private final TaskHistory taskHistory;
    private final EntryRecorder entryRecorder;
    private final JDialog dialog = new JDialog((java.awt.Frame) null, "Daylog", false);
    private final JTextField taskField = new JTextField(40);
    private final JTextArea textArea = new JTextArea(4, 40);
    private final DefaultListModel<String> suggestionModel = new DefaultListModel<>();
    private final JList<String> suggestionList = new JList<>(suggestionModel);
    private final JPopupMenu suggestionPopup = new JPopupMenu();
    private boolean changingTaskFromCode = false;

    /** Builds all components right away; must be called on the Swing event thread. */
    public EntryDialog(TaskHistory taskHistory, EntryRecorder entryRecorder) {
        log.trace("EntryDialog(taskHistory, entryRecorder): building the dialog once");
        this.taskHistory = taskHistory;
        this.entryRecorder = entryRecorder;
        buildLayout();
        buildSuggestionPopup();
        installTaskFieldBehavior();
        installDialogShortcuts();
    }

    /** The parent to use for other windows (the file explorer) opened on top of this dialog. */
    public JDialog window() {
        return dialog;
    }

    /**
     * Shows the dialog with the last task prefilled and an empty text, focus on the text area because the text changes every time
     * and the task rarely does. If the dialog is already open we only bring it to the front, so nothing the user typed is lost.
     */
    public void show() {
        log.trace("show(): showing the dialog");
        if (dialog.isVisible()) {
            dialog.toFront();
            textArea.requestFocusInWindow();
            return;
        }
        setTaskFromCode(taskHistory.last().map(TaskName::value).orElse(""));
        textArea.setText("");
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.toFront();
        textArea.requestFocusInWindow();
    }

    /** This arranges the task row, the scrollable text area and the buttons row. */
    private void buildLayout() {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScroll = new JScrollPane(textArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel taskRow = new JPanel(new BorderLayout(6, 0));
        taskRow.add(new JLabel("Task"), BorderLayout.WEST);
        taskRow.add(taskField, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save (Enter)");
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

    /** The drop-down is a popup holding a list. Neither takes keyboard focus, so the user keeps typing in the task field. */
    private void buildSuggestionPopup() {
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false);
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int clickedIndex = suggestionList.locationToIndex(event.getPoint());
                if (clickedIndex >= 0) {
                    chooseSuggestion(suggestionModel.get(clickedIndex));
                }
            }
        });
        suggestionPopup.setFocusable(false);
        suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        suggestionPopup.setLayout(new BorderLayout());
        suggestionPopup.add(suggestionList, BorderLayout.CENTER);
    }

    /**
     * Wires the task field: typing refreshes the drop-down; Down/Up move in it (Down also opens it); Enter chooses the highlighted
     * suggestion or, when none is highlighted, jumps to the text area; Escape closes the drop-down first and the dialog second.
     */
    private void installTaskFieldBehavior() {
        taskField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { taskTextChanged(); }
            @Override public void removeUpdate(DocumentEvent event) { taskTextChanged(); }
            @Override public void changedUpdate(DocumentEvent event) { }
        });
        taskField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                suggestionPopup.setVisible(false);
            }
        });
        bindTaskKey(KeyEvent.VK_DOWN, "suggestionDown", () -> moveSuggestionSelection(1));
        bindTaskKey(KeyEvent.VK_UP, "suggestionUp", () -> moveSuggestionSelection(-1));
        bindTaskKey(KeyEvent.VK_ENTER, "taskEnter", this::enterPressedInTask);
        bindTaskKey(KeyEvent.VK_ESCAPE, "taskEscape", this::escapePressedInTask);
    }

    /** Registers a key on the task field only (it wins over the dialog-wide shortcuts). */
    private void bindTaskKey(int keyCode, String actionName, Runnable action) {
        taskField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        taskField.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    /**
     * Enter in the text area saves and Shift+Enter inserts a line break (the text area's own "insert-break" action).
     * Ctrl+Enter also saves, and Escape closes, from anywhere in the dialog.
     */
    private void installDialogShortcuts() {
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "save");
        textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "insert-break");
        textArea.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                save();
            }
        });
        JComponent root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "save");
        root.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                save();
            }
        });
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        root.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dialog.setVisible(false);
            }
        });
    }

    /** Sets the task text without opening the drop-down (that is only for what the user types). */
    private void setTaskFromCode(String task) {
        changingTaskFromCode = true;
        taskField.setText(task);
        changingTaskFromCode = false;
        suggestionPopup.setVisible(false);
    }

    /** Called on every change of the task text; later, on the event thread, the drop-down is refreshed. */
    private void taskTextChanged() {
        if (changingTaskFromCode) {
            return;
        }
        SwingUtilities.invokeLater(this::refreshSuggestions);
    }

    /** Fills the drop-down with the matching tasks and shows it under the field, or hides it when nothing matches. */
    private void refreshSuggestions() {
        log.trace("refreshSuggestions(): computing suggestions for '{}'", taskField.getText());
        List<TaskName> suggestions = TaskCompletion.suggestionsFor(taskField.getText(), taskHistory.all(), MAX_SUGGESTIONS);
        if (suggestions.isEmpty() || !taskField.isShowing()) {
            suggestionPopup.setVisible(false);
            return;
        }
        suggestionModel.clear();
        suggestions.forEach(task -> suggestionModel.addElement(task.value()));
        suggestionList.clearSelection();
        suggestionList.setVisibleRowCount(suggestions.size());
        suggestionPopup.setPopupSize(taskField.getWidth(), suggestionList.getPreferredScrollableViewportSize().height + 2);
        suggestionPopup.show(taskField, 0, taskField.getHeight());
    }

    /** Down/Up: opens the drop-down if it is closed (Down), otherwise moves the highlight, wrapping around. */
    private void moveSuggestionSelection(int step) {
        if (!suggestionPopup.isVisible()) {
            if (step > 0) {
                refreshSuggestions();
            }
            return;
        }
        int count = suggestionModel.size();
        int nextIndex = Math.floorMod(suggestionList.getSelectedIndex() + step, count);
        suggestionList.setSelectedIndex(nextIndex);
    }

    /** Enter in the task field: take the highlighted suggestion if there is one, otherwise save the entry. */
    private void enterPressedInTask() {
        if (suggestionPopup.isVisible() && suggestionList.getSelectedIndex() >= 0) {
            chooseSuggestion(suggestionList.getSelectedValue());
            return;
        }
        save();
    }

    /** Escape in the task field closes the drop-down if open, otherwise the dialog. */
    private void escapePressedInTask() {
        if (suggestionPopup.isVisible()) {
            suggestionPopup.setVisible(false);
            return;
        }
        dialog.setVisible(false);
    }

    /** Puts the chosen suggestion in the task field, closes the drop-down and moves on to the text area. */
    private void chooseSuggestion(String task) {
        log.debug("Suggestion chosen: {}", task);
        setTaskFromCode(task);
        textArea.requestFocusInWindow();
    }

    /**
     * Validates the task, records the entry and closes the dialog. A blank task turns the field red and keeps the dialog open;
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
        suggestionPopup.setVisible(false);
        boolean saved = entryRecorder.record(task, textArea.getText());
        if (saved) {
            dialog.setVisible(false);
        }
    }
}
