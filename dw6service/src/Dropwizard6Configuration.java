import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;


public class Dropwizard6Configuration extends Configuration {

    @JsonProperty
    private String property;

    public String getProperty() {
        return property;
    }
}
