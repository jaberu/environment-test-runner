package de.jaberu.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Used to load the properties from classpath.
 *
 * The loaded properties are cached basing on the bundle
 *
 * Created by aherr on 13.11.2015.
 */
public class PropertyCache {

    private static Map<CacheKey, Properties> cache = new HashMap<CacheKey, Properties>();

    /**
     * Gets the properties instance for the given stage and publication.
     *
     * Before loading we check if we already loaded that bundle and serve it from cache in that case.
     *
     * @param loader class what is used to load the properties
     * @param bundle the name of the bundle to load
     * @param stage the stage, can be null
     * @param publication the publication, can be null
     * @return properties including all stage and publication specific overrides
     * @throws IOException if loading fails
     */
    public static Properties getProperties(Class<?> loader, String bundle, String stage, String publication) throws IOException {
        CacheKey key = new CacheKey(bundle, stage, publication);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Properties properties = createResourceBundle(loader, bundle, stage, publication);
        cache.put(key, properties);
        return properties;
    }


    /**
     * Here we add a bundle by first loading the base bundle. Afterwards we check if an environment-specific bundle
     * exists. If existing we override the already loaded values from the base bundle.
     * <p>
     * Since we assume that the resource is accessible by the same classloader like the test class, we use the
     * test class for loading the resource.
     *
     * @param loader   the test class to load the resource with
     * @param resource the bundles name
     * @param stage the stage to look for, can be null
     * @param publication the publication to look for, can be null
     * @return properties instance
     * @throws IOException from {@link Properties#load(InputStream)}
     */
    private static Properties createResourceBundle(Class<?> loader, String resource, String stage, String publication) throws IOException {
        Properties properties = new Properties();
        // loads the base resource
        StringBuilder builder = new StringBuilder(SLASH);
        builder.append(resource);
        builder.append(PROPERTIES);
        properties.load(loader.getResourceAsStream(builder.toString()));
        // check environment specific overrides
        if (stage != null) {
            builder = new StringBuilder(SLASH);
            builder.append(stage.toLowerCase());
            builder.append(SLASH);
            builder.append(resource);
            builder.append(PROPERTIES);
            InputStream publicationSpecific = loader.getResourceAsStream(builder.toString());
            if (publicationSpecific != null) {
                // might be null if nothing specific for the environment exists
                properties.load(publicationSpecific);
            }
            if (publication != null) {
                builder = new StringBuilder(SLASH);
                builder.append(stage.toLowerCase());
                builder.append(SLASH);
                builder.append(publication.toLowerCase());
                builder.append(SLASH);
                builder.append(resource);
                builder.append(PROPERTIES);
                InputStream stageSpecific = loader.getResourceAsStream(builder.toString());
                if (stageSpecific != null) {
                    // might be null if nothing specific for the environment exists
                    properties.load(stageSpecific);
                }
            }
        }
        return properties;
    }

    private static class CacheKey {
        private String bundle;
        private String stage;
        private String publication;

        public CacheKey(String bundle, String stage, String publication) {
            this.bundle = bundle;
            this.stage = stage;
            this.publication = publication;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (bundle != null ? !bundle.equals(cacheKey.bundle) : cacheKey.bundle != null) return false;
            if (stage != null ? !stage.equals(cacheKey.stage) : cacheKey.stage != null) return false;
            return !(publication != null ? !publication.equals(cacheKey.publication) : cacheKey.publication != null);

        }

        @Override
        public int hashCode() {
            int result = bundle != null ? bundle.hashCode() : 0;
            result = 31 * result + (stage != null ? stage.hashCode() : 0);
            result = 31 * result + (publication != null ? publication.hashCode() : 0);
            return result;
        }
    }

    private static final String SLASH = "/";
    private static final String PROPERTIES = ".properties";
}
