package daylog.ui;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This shows the file explorer used to choose the log file, starting in the folder of the previously chosen one. */
public final class LogFileChooser {

    private static final Logger log = LoggerFactory.getLogger(LogFileChooser.class);

    private LogFileChooser() {
    }

    /**
     * Opens the explorer pointing at the folder of the current log file (or the home folder if there is none)
     * and returns the chosen file, or empty if the user cancelled. Choosing a name that does not exist yet is allowed.
     */
    public static Optional<Path> choose(Component parent, Optional<Path> currentLogFile) {
        log.trace("choose(parent, currentLogFile): opening the file explorer, current is {}", currentLogFile);
        File startFolder = currentLogFile.map(Path::toAbsolutePath).map(Path::getParent).map(Path::toFile)
            .orElse(new File(System.getProperty("user.home")));
        JFileChooser chooser = new JFileChooser(startFolder);
        chooser.setDialogTitle("Choose log file");
        currentLogFile.ifPresent(file -> chooser.setSelectedFile(file.toAbsolutePath().toFile()));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            log.debug("File choice cancelled");
            return Optional.empty();
        }
        return Optional.of(chooser.getSelectedFile().toPath());
    }
}
