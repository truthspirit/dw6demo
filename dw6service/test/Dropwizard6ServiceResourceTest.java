import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Rule;

public class Dropwizard6ServiceResourceTest {

    @Rule
    ResourceTestRule resources = setUpResources();

    protected ResourceTestRule setUpResources() {
        return ResourceTestRule.builder()
                .addResource(Dropwizard6Resource.class)
                .addFeature("booleanFeature", false)
                .addProperty("integerProperty", new Integer(1))
                .addProvider(HelpfulServiceProvider.class)
                .build();
    }
}
