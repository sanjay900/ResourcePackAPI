package net.tangentmc.resourcepackapi.exceptions;

public class NoEnabledPacksException extends Exception {
    public NoEnabledPacksException() {
        super("No resource packs enabled");
    }
}
