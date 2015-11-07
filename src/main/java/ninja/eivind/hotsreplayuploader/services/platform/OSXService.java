package ninja.eivind.hotsreplayuploader.services.platform;

import java.awt.Desktop;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class OSXService implements PlatformService {
    private static final Logger LOG = LoggerFactory.getLogger(OSXService.class);
    private final String libraryPath = "/Library/Application Support";
    private Desktop desktop;

    public OSXService() {
        desktop = Desktop.getDesktop();
    }

    @Override
    public File getApplicationHome() {
        return new File(USER_HOME + "/" + libraryPath + "/" + APPLICATION_DIRECTORY_NAME);
    }

    @Override
    public File getHotSHome() {
        return new File(USER_HOME + "/" + libraryPath + "/" + "Blizzard/Heroes of the Storm/Accounts/");
    }

    @Override
    public void browse(final URI uri) throws IOException {
        desktop.browse(uri);
    }

    @Override
    public URL getLogoUrl() {
        String logoVariant = isMacMenuBarDarkMode() ? "" : "-black";
        return ClassLoader.getSystemClassLoader().getResource(
                "images/logo-desktop" + logoVariant + ".png");
    }

    @Override
    public TrayIcon getTrayIcon(final Stage primaryStage) throws PlatformNotSupportedException {
        URL imageURL = getLogoUrl();
        EventType<KeyEvent> keyPressed = KeyEvent.KEY_PRESSED;
        primaryStage.addEventHandler(keyPressed, event -> {
            if (event.getCode() == KeyCode.Q && event.isMetaDown()) {
                LOG.info("Exiting application due to keyboard shortcut.");
                System.exit(0);
            }
        });
        return buildTrayIcon(imageURL, primaryStage);
    }

    /**
     * Checks, if the OS X dark mode is used by querying the defaults CLI.<br>
     * This is needed for adding the correct tray icon once at the startup.
     * @return true if <code>defaults read -g AppleInterfaceStyle</code>
     * has an exit status of <code>0</code> (i.e. _not_ returning "key not found").
     */
    private boolean isMacMenuBarDarkMode() {
        try {
            /* check for exit status only.
             * Once there are more modes than "dark" and "default",
             *  we might need to analyze string contents...
             */
            final Process proc = Runtime.getRuntime().exec(
                    new String[] {"defaults", "read", "-g", "AppleInterfaceStyle"});
            proc.waitFor(100, TimeUnit.MILLISECONDS);
            return proc.exitValue() == 0;
        } catch (IOException | InterruptedException | IllegalThreadStateException ex) {
            //process didn't terminate properly
            LOG.warn("Could not determine, whether 'dark mode' is being used. "
                    + "Falling back to default (light) mode.");
            return false;
        }
    }

}