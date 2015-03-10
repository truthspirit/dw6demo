import ch.qos.logback.classic.selector.servlet.LoggerContextFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashCodes;
import com.google.common.io.Closeables;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.HttpConfiguration;
import com.yammer.dropwizard.lifecycle.ExecutorServiceManager;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.reporting.PingServlet;
import com.yammer.metrics.util.DeadlockHealthCheck;
import org.eclipse.jetty.server.Server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by timothy.maxwell on 3/9/15.
 */
public class Dropwizard6Service extends Service<Dropwizard6Configuration> {

    @Override
    public void initialize(Bootstrap<Dropwizard6Configuration> bootstrap) {
        bootstrap.setName("dropwizard-6-service");
        Metrics.defaultRegistry().newGauge(new MetricName(Dropwizard6Service.class, "gauge"), new Gauge<Integer>() {
            @Override
            public Integer value() {
                return HashCodes.fromLong(1L).asInt();
            }
        });

        ObjectMapper objectMapper = bootstrap.getObjectMapperFactory().build();

    }

    @Override
    public void run(Dropwizard6Configuration configuration, Environment environment)
            throws Exception {

        HttpConfiguration httpConfiguration = configuration.getHttpConfiguration();
        int applicationPort = httpConfiguration.getPort();

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
                Closeables.closeQuietly(closeable);
            }
        };

        environment.manage(uselessManaged);

        environment.addResource(Dropwizard6Resource.class);

        environment.addHealthCheck(new DeadlockHealthCheck());

        ServerLifecycleListener uselessListener = new ServerLifecycleListener() {
            @Override
            public void serverStarted(Server server) {
                // do something smart here
            }
        };
        environment.addServerLifecycleListener(uselessListener);

        int minPoolSize = 1;
        int maxPoolSize = 5;
        long keepAliveTime = 10;
        TimeUnit duration = TimeUnit.MINUTES;

        ExecutorService service = environment.managedExecutorService("worker-%", minPoolSize, maxPoolSize, keepAliveTime, duration);

        long shutdownPeriod = 10L;
        TimeUnit unit = TimeUnit.SECONDS;
        String poolname = "pool1";
        ExecutorServiceManager esm = new ExecutorServiceManager(service, shutdownPeriod, unit, poolname);

        int corePoolSize = 5;
        environment.managedScheduledExecutorService("scheduled-worker-%", corePoolSize);

        environment.addFilter(new LoggerContextFilter(), "/loggedpath");

        environment.addServlet(PingServlet.class, "/ping");

    }
}
