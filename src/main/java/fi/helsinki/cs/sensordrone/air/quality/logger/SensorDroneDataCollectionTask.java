package fi.helsinki.cs.sensordrone.air.quality.logger;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.java.Drone;

import java.util.Locale;

public class SensorDroneDataCollectionTask implements Runnable {


    private static Drone drone;
    private static String macAddress;

    private final double latitude;
    private final double longitude;
    private final long timeout;

    private boolean batteryMeasured = false;
    private boolean altitudeMeasured = false;
    private boolean capacitanceMeasured = false;
    private boolean humidityMeasured = false;
    private boolean irTemperatureMeasured = false;
    private boolean oxidizingGasMeasured = false;
    private boolean precisionGasMeasured = false;
    private boolean pressureMeasured = false;
    private boolean reducingGasMeasured = false;
    private boolean rgbMeasured = false;
    private boolean temperatureMeasured = false;
    private boolean co2Measured = false;
    private boolean debug = false;

    private static final int TEMPERATURE_SENSOR_ID = 1;
    private static final int COLOR_SENSOR_ID = 2;
    private static final int REDUCING_GAS_SENSOR_ID = 3;
    private static final int PRESSURE_SENSOR_ID = 4;
    private static final int PRECISION_GAS_SENSOR_ID = 5;
    private static final int OXIDIZING_GAS_SENSOR_ID = 6;
    private static final int IR_TEMPERATURE_SENSOR_ID = 7;
    private static final int HUMIDITY_SENSOR_ID = 8;
    private static final int CAPACITANCE_SENSOR_ID = 9;
    private static final int ALTITUDE_SENSOR_ID = 10;
    private static final int BATTERY_VOLTAGE_SENSOR_ID = 11;
    private static final int FILTERED_CO2_SENSOR_ID = 12;
    private static final int UNFILTERED_CO2_SENSOR_ID = 13;

    SensorDroneDataCollectionTask(String macAddress, double latitude, double longitude, long timeout, boolean debug) {
        if (SensorDroneDataCollectionTask.macAddress == null) {
            SensorDroneDataCollectionTask.drone = new Drone();
            SensorDroneDataCollectionTask.macAddress = macAddress;

            DroneEventHandler mDroneEventHandler = new DroneEventHandler() {
                @Override
                public void parseEvent(DroneEventObject event) {
                    if (event.matches(DroneEventObject.droneEventType.CONNECTED)) {
                        drone.setLEDs(0, 0, 126); // Set LED blue when connected
                        drone.enableADC();
                        drone.enableAltitude();
                        drone.enableCapacitance();
                        drone.enableHumidity();
                        drone.enableIRTemperature();
                        drone.enableOxidizingGas();
                        drone.enablePrecisionGas();
                        drone.enablePressure();
                        drone.enableReducingGas();
                        drone.enableRGBC();
                        drone.enableTemperature();
                        drone.measureBatteryVoltage();
                        measureCO2();
                        // Enabled
                    } else if (event.matches(DroneEventObject.droneEventType.ALTITUDE_ENABLED)) {
                        drone.measureAltitude();
                    } else if (event.matches(DroneEventObject.droneEventType.CAPACITANCE_ENABLED)) {
                        drone.measureCapacitance();
                    } else if (event.matches(DroneEventObject.droneEventType.HUMIDITY_ENABLED)) {
                        drone.measureHumidity();
                    } else if (event.matches(DroneEventObject.droneEventType.IR_TEMPERATURE_ENABLED)) {
                        drone.measureIRTemperature();
                    } else if (event.matches(DroneEventObject.droneEventType.OXIDIZING_GAS_ENABLED)) {
                        drone.measureOxidizingGas();
                    } else if (event.matches(DroneEventObject.droneEventType.PRECISION_GAS_ENABLED)) {
                        drone.measurePrecisionGas();
                    } else if (event.matches(DroneEventObject.droneEventType.PRESSURE_ENABLED)) {
                        drone.measurePressure();
                    } else if (event.matches(DroneEventObject.droneEventType.REDUCING_GAS_ENABLED)) {
                        drone.measureReducingGas();
                    } else if (event.matches(DroneEventObject.droneEventType.RGBC_ENABLED)) {
                        drone.measureRGBC();
                    } else if (event.matches(DroneEventObject.droneEventType.TEMPERATURE_ENABLED)) {
                        drone.measureTemperature();
                        // Measured
                    } else if (event.matches(DroneEventObject.droneEventType.BATTERY_VOLTAGE_MEASURED)) {
                        logSample(BATTERY_VOLTAGE_SENSOR_ID, drone.batteryVoltage_Volts);
                        batteryMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.ALTITUDE_MEASURED)) {
                        // drone.altitude_Meters, drone.altitude_Feet
                        logSample(ALTITUDE_SENSOR_ID, drone.altitude_Meters);
                        altitudeMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.CAPACITANCE_MEASURED)) {
                        logSample(CAPACITANCE_SENSOR_ID, drone.capacitance_femtoFarad);
                        capacitanceMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.HUMIDITY_MEASURED)) {
                        logSample(HUMIDITY_SENSOR_ID, drone.humidity_Percent);
                        humidityMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.IR_TEMPERATURE_MEASURED)) {
                        // drone.irTemperature_Celsius, drone.irTemperature_Fahrenheit, drone.irTemperature_Kelvin
                        logSample(IR_TEMPERATURE_SENSOR_ID, drone.irTemperature_Celsius);
                        irTemperatureMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.OXIDIZING_GAS_MEASURED)) {
                        logSample(OXIDIZING_GAS_SENSOR_ID, drone.oxidizingGas_Ohm);
                        oxidizingGasMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.PRECISION_GAS_MEASURED)) {
                        logSample(PRECISION_GAS_SENSOR_ID, drone.precisionGas_ppmCarbonMonoxide);
                        precisionGasMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.PRESSURE_MEASURED)) {
                        // drone.pressure_Pascals, drone.pressure_Atmospheres, drone.pressure_Torr
                        logSample(PRESSURE_SENSOR_ID, drone.pressure_Pascals);
                        pressureMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.REDUCING_GAS_MEASURED)) {
                        logSample(REDUCING_GAS_SENSOR_ID, drone.reducingGas_Ohm);
                        reducingGasMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.RGBC_MEASURED)) {
                        // drone.rgbcLux, drone.rgbcColorTemperature, drone.rgbcClearChannel, rgbcBlueChannel, rgbcGreenChannel, rgbcRedChannel
                        logSample(COLOR_SENSOR_ID, drone.rgbcLux, drone.rgbcColorTemperature, drone.rgbcClearChannel, drone.rgbcBlueChannel, drone.rgbcGreenChannel, drone.rgbcRedChannel);
                        rgbMeasured = true;
                    } else if (event.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED)) {
                        // drone.temperature_Celsius, drone.temperature_Fahrenheit, drone.temperature_Kelvin
                        logSample(TEMPERATURE_SENSOR_ID, drone.temperature_Celsius);
                        temperatureMeasured = true;
                    }
                }
            };
            SensorDroneDataCollectionTask.drone.registerDroneListener(mDroneEventHandler);
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeout = timeout;
        this.debug = debug;
    }

    private boolean allDataCollected() {
        return batteryMeasured && altitudeMeasured && capacitanceMeasured && humidityMeasured && irTemperatureMeasured && oxidizingGasMeasured &&
                precisionGasMeasured && pressureMeasured && reducingGasMeasured && rgbMeasured && temperatureMeasured && co2Measured;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void measureCO2() {
        // CO2 Sensor http://www.mb-systemtechnik.de/produkte_co2_messung_co2_sensor_modul_mit_ausgang.htm
        // Set baudrate
        //drone.setBaudRate_38400();
        drone.setBaudRate_9600(); // As defined in http://www.airtest.com/support/datasheet/COZIRSerialInterface.pdf
        // Command
        // byte[] getStatusCommand = { 0x23, 0x31, 0x30, 0x0D }; // 10 (Read status)
        byte[] setPollingMode = "K 2\\r\\n".getBytes();
        drone.uartWrite(setPollingMode);
        // Now poll the co2
        byte[] getCO2 = "Z\\r\\n".getBytes();

        boolean isResponseValid = false;
        int nbTries = 5;
        while (!isResponseValid && nbTries > 0) {
            byte[] response = drone.uartWriteForRead(getCO2, 5);
            // We are looking for the start space Z space some values
            if (response[0] == 32 && response[1] == 90 && response[2] == 32 && response[4] != 0) {
                byte[] values = new byte[5];
                System.arraycopy(response, 4, values, 0, values.length);
                logSample(FILTERED_CO2_SENSOR_ID, Double.parseDouble(new String(values)));
                isResponseValid = true;
                // Now processing the last unfiltered reading
                if (response[8] == 32 && response[9] == 122 && response[10] == 32 && response[11] != 0) {
                    System.arraycopy(response, 11, values, 0, values.length);
                    logSample(UNFILTERED_CO2_SENSOR_ID, Double.parseDouble(new String(values)));
                }
            }
            nbTries--;
        }
        co2Measured = true;
    }

    private void logSample(int sensorId, double... sensorValues) {
        StringBuilder sampleSb = new StringBuilder(String.format(Locale.ENGLISH, "%d;%s;%f;%f;%d",
                System.currentTimeMillis(), macAddress, latitude, longitude, sensorId));
        for (double v : sensorValues) {
            sampleSb.append(String.format(Locale.ENGLISH, ";%f", v));
        }
        System.out.println(sampleSb.toString());
    }

    @Override
    public void run() {

        if (!drone.isConnected) {
            drone.btConnect(macAddress, debug);
        }

        if (!drone.isConnected) {
            System.err.println("Connection Failed to drone " + macAddress + " !");
            return;
        }

        long startTime = System.currentTimeMillis();
        while (drone.isConnected && !allDataCollected() && (System.currentTimeMillis() - startTime < timeout)) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        drone.disconnect(debug);
    }

}
