package com.emr.gds.core.auth;

/**
 * Stores and checks the single local application password.
 */
public interface CredentialRepository {

    boolean hasCredential();

    void setPassword(char[] password);

    boolean verifyPassword(char[] password);
}
