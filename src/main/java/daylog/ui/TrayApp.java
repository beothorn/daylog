package daylog.ui;

import daylog.settings.Settings;
import daylog.version.AppVersion;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class puts the daylog icon in the system tray: left click opens the entry dialog, right click shows the menu
 * (Log now, Choose log file, the version, Quit). It only wires UI events to the dialog, the settings and the chooser.
 */
public final class TrayApp {

    private static final Logger log = LoggerFactory.getLogger(TrayApp.class);

    private final EntryDialog entryDialog;
    private final Settings settings;

    /** The dialog is shown on click and the settings are updated when the user chooses a log file from the menu. */
    public TrayApp(EntryDialog entryDialog, Settings settings) {
        this.entryDialog = entryDialog;
        this.settings = settings;
    }

    /** Adds the icon to the tray. Returns false (after logging why) when the system has no tray. */
    public boolean install() {
        log.trace("install(): adding the tray icon");
        if (!SystemTray.isSupported()) {
            log.error("The system tray is not supported here, daylog cannot show its icon");
            return false;
        }
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon icon = new TrayIcon(BookIcon.draw(tray.getTrayIconSize().width), "Daylog", buildMenu());
        icon.setImageAutoSize(true);
        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    SwingUtilities.invokeLater(entryDialog::show);
                }
            }
        });
        try {
            tray.add(icon);
        } catch (AWTException e) {
            log.error("Could not add the tray icon, daylog cannot be used", e);
            return false;
        }
        log.info("Daylog is running in the tray");
        return true;
    }

    /** Builds the right click menu. */
    private PopupMenu buildMenu() {
        PopupMenu menu = new PopupMenu();
        MenuItem logNow = new MenuItem("Log now");
        logNow.addActionListener(event -> SwingUtilities.invokeLater(entryDialog::show));
        MenuItem chooseFile = new MenuItem("Choose log file...");
        chooseFile.addActionListener(event -> SwingUtilities.invokeLater(this::chooseLogFile));
        MenuItem quit = new MenuItem("Quit");
        quit.addActionListener(event -> {
            log.info("Quitting");
            System.exit(0);
        });
        menu.add(logNow);
        menu.add(chooseFile);
        menu.addSeparator();
        MenuItem versionLabel = new MenuItem("Daylog " + AppVersion.current());
        versionLabel.setEnabled(false);
        menu.add(versionLabel);
        menu.add(quit);
        return menu;
    }

    /** Opens the file explorer and stores the choice. */
    private void chooseLogFile() {
        log.trace("chooseLogFile(): menu item clicked");
        Optional<java.nio.file.Path> chosen = LogFileChooser.choose(null, settings.logFile());
        chosen.ifPresent(settings::saveLogFile);
    }
}
