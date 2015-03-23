import ch.qos.logback.classic.selector.servlet.LoggerContextFilter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.servlets.PingServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashCode;
import com.google.common.io.Closeables;
import io.dropwizard.Application;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.eclipse.jetty.server.Server;

import javax.servlet.DispatcherType;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

public class Dropwizard6Service extends Application<Dropwizard6Configuration> {

    @Override
    public void initialize(Bootstrap<Dropwizard6Configuration> bootstrap) {

        bootstrap.getMetricRegistry().register(MetricRegistry.name(Dropwizard6Service.class, "gauge"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return HashCode.fromLong(1L).asInt();
            }
        });

        ObjectMapper objectMapper = bootstrap.getObjectMapper();

    }

    @Override
    public void run(Dropwizard6Configuration configuration, Environment environment)
            throws Exception {

        HttpConnectorFactory httpConnectorFactory = (HttpConnectorFactory) ((DefaultServerFactory) configuration.getServerFactory()).getApplicationConnectors().get(0);
        int applicationPort = httpConnectorFactory.getPort();

        Managed uselessManaged = new Managed() {
            Closeable closeable;

            @Override
            public void start()
                    throws Exception {
               closeable = new BufferedReader(new InputStreamReader(new FileInputStream("useful.file")));
            }

            @Override
            public void stop()
                    throws Exception {
                Closeables.close(closeable, true);
            }
        };

        environment.lifecycle().manage(uselessManaged);

        environment.jersey().register(Dropwizard6Resource.class);

        environment.healthChecks().register("deadlock-healthcheck", new ThreadDeadlockHealthCheck());

        ServerLifecycleListener uselessListener = new ServerLifecycleListener() {
            @Override
            public void serverStarted(Server server) {
                // do something smart here
            }
        };
        environment.lifecycle().addServerLifecycleListener(uselessListener);

        int minPoolSize = 1;
        int maxPoolSize = 5;
        long keepAliveTime = 10;

        ExecutorService service = environment.lifecycle().executorService("worker-%")
                .minThreads(minPoolSize)
                .maxThreads(maxPoolSize)
                .keepAliveTime(Duration.minutes(keepAliveTime))
                .build();

        long shutdownPeriod = 10L;
        String poolname = "pool1";
        ExecutorServiceManager esm = new ExecutorServiceManager(service, Duration.seconds(shutdownPeriod), poolname);

        int corePoolSize = 5;
        environment.lifecycle().scheduledExecutorService("scheduled-worker-%")
                .threads(corePoolSize)
                .build();

        environment.servlets().addFilter("loggedContextFilter", new LoggerContextFilter()).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/logged");

        environment.servlets().addServlet("ping", PingServlet.class).addMapping("/ping");

    }
}
