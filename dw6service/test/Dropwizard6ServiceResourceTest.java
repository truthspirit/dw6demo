import com.yammer.dropwizard.testing.ResourceTest;

public class Dropwizard6ServiceResourceTest extends ResourceTest {
    @Override
    protected void setUpResources()
            throws Exception {
        addResource(Dropwizard6Resource.class);
        addFeature("booleanFeature", false);
        addProperty("integerProperty", new Integer(1));
        addProvider(HelpfulServiceProvider.class);
    }
}
