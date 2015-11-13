package de.jaberu.test;

import org.junit.runners.model.FrameworkMethod;

/**
 * Here we override the basic junit test model for methods by an own variant.
 *
 *
 * Created by aherr on 12.11.2015.
 */
public class EnvironmentFrameworkMethod extends FrameworkMethod {

    private String stage;
    private String publication;

    public EnvironmentFrameworkMethod(FrameworkMethod method, String stage) {
        this(method, stage, null);
    }

    public EnvironmentFrameworkMethod(FrameworkMethod method, String stage, String publication) {
        super(method.getMethod());
        this.publication = publication;
        this.stage = stage;
    }

    /**
     * That is need since the different junit runner UIs reads the method name from
     * here. However we do not only want the method name, but also the environment
     * information shown in the result lists of the different UIs.
     *
     * @return method name + environment infos
     */
    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder(this.getMethod().getName());
        if (publication != null) {
            builder.append(" for ").append(publication);
        }
        if (stage != null) {
            builder.append(" on ").append(stage);
        }
        return builder.toString();
    }

    public String getPublication() {
        return publication;
    }

    public String getStage() {
        return stage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentFrameworkMethod)) return false;
        if (!super.equals(o)) return false;

        EnvironmentFrameworkMethod that = (EnvironmentFrameworkMethod) o;

        if (getStage() != null ? !getStage().equals(that.getStage()) : that.getStage() != null) return false;
        return !(getPublication() != null ? !getPublication().equals(that.getPublication()) : that.getPublication() != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getStage() != null ? getStage().hashCode() : 0);
        result = 31 * result + (getPublication() != null ? getPublication().hashCode() : 0);
        return result;
    }
}
