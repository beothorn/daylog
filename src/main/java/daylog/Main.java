package daylog;

import daylog.appdir.AppDirectory;
import daylog.entry.EntryRecorder;
import daylog.settings.Settings;
import daylog.task.TaskHistory;
import daylog.ui.EntryDialog;
import daylog.ui.LogFileChooser;
import daylog.ui.TrayApp;
import java.nio.file.Path;
import java.time.Clock;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the entry point. It only creates the objects and connects them, there is no logic here. */
public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    /** Starts daylog: prepares the app directory, then builds the dialog and the tray icon on the Swing thread. */
    public static void main(String[] arguments) {
        log.trace("main(arguments): starting daylog");
        Path appDirectory = AppDirectory.forThisMachine().createdIfMissing();
        Settings settings = new Settings(appDirectory.resolve("settings.properties"));
        TaskHistory taskHistory = new TaskHistory(appDirectory.resolve("tasks.txt"));
        SwingUtilities.invokeLater(() -> startUserInterface(settings, taskHistory));
    }

    /** Runs on the Swing thread: the dialog needs the recorder, and the recorder needs the dialog as parent of the file explorer. */
    private static void startUserInterface(Settings settings, TaskHistory taskHistory) {
        log.trace("startUserInterface(settings, taskHistory): building the user interface");
        useSystemLookAndFeel();
        EntryDialog[] dialogHolder = new EntryDialog[1];
        EntryRecorder recorder = new EntryRecorder(settings, taskHistory, Clock.systemDefaultZone(),
            () -> LogFileChooser.choose(dialogHolder[0].window(), settings.logFile()));
        dialogHolder[0] = new EntryDialog(taskHistory, recorder);
        boolean installed = new TrayApp(dialogHolder[0], settings).install();
        if (!installed) {
            System.err.println("daylog: no system tray available, exiting");
            System.exit(1);
        }
    }

    /**
     * Native look and feel on Windows only. On Linux the system one is GTK, which measured about 450 ms slower to start
     * (1.0 s against 0.6 s), and speed matters more than looks here. Failing is harmless, the default one is used.
     */
    private static void useSystemLookAndFeel() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            log.debug("Not Windows, keeping the default look and feel for a faster start");
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.warn("Could not use the system look and feel, using the default one: {}", e.toString());
        }
    }
}
