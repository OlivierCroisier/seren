package net.thecodersbreakfast.seren;

/**
 * An exception that signals a problem in the configuration file.
 *
 * @author Olivier Croisier
 */
public class ConfigurationException extends Exception {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
