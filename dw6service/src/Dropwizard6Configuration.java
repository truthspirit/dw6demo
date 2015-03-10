import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

/**
 * Created by timothy.maxwell on 3/9/15.
 */
public class Dropwizard6Configuration extends Configuration {

    @JsonProperty
    private String property;

    public String getProperty() {
        return property;
    }
}
