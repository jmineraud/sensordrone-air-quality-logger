package fi.helsinki.cs.sensordrone.air.quality.logger;

import org.apache.commons.cli.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SensorDroneAirQualityLogger {

    public static void main(String[] args) {

        // create Options object
        Options options = new Options();

        // add t option
        options.addRequiredOption("d","delay", true, "The delay for collecting the data from the sensordrone");
        options.addRequiredOption("m","mac", true, "The mac address of the sensordrone");
        options.addRequiredOption("lat","latitude", true, "The latitude of the sensordrone");
        options.addRequiredOption("lon","longitude", true, "The longitude of the sensordrone");
        options.addOption("timeout", true, "The timeout set to collect the data");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            long delay = Long.parseLong(cmd.getOptionValue("d"));
            String macAddress = cmd.getOptionValue("m");
            long timeout = Long.parseLong(cmd.getOptionValue("timeout", "10000")); // default of 10 seconds
            double latitude = Double.parseDouble(cmd.getOptionValue("lat"));
            double longitude = Double.parseDouble(cmd.getOptionValue("lon"));

            final ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(1);

            final SensorDroneDataCollectionTask task = new SensorDroneDataCollectionTask(macAddress, latitude, longitude, timeout);

            final ScheduledFuture<?> sensorDroneCollectionHandler =
                        scheduler.scheduleAtFixedRate(task,0, delay, TimeUnit.MILLISECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> { sensorDroneCollectionHandler.cancel(true); }));
        }
        catch (ParseException exp) {
            // oops, something went wrong
            System.err.println(exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "gradle run", options);
        }

    }
}
