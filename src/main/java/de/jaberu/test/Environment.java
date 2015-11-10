package de.jaberu.test;

/**
 * Here we define the different fixed parameters for choosing the publication and also the target system.
 * Those parameters can be given by command line (system property) in order to control environment
 * specific behaviour in tests.
 *
 * Created by aherr on 10.11.2015.
 */
public final class Environment {

    public static final String PUBLICATION = "publication";
    public static final String STAGE = "stage";

    private Environment() {}
}
