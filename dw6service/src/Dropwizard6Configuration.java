import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;


public class Dropwizard6Configuration extends Configuration {

    @JsonProperty
    private String property;

    public String getProperty() {
        return property;
    }
}
