package com.sensorcon.sensordrone.java;

import com.sensorcon.sensordrone.CoreDrone;

import javax.usb.*;
import javax.usb.util.UsbUtil;
import java.util.List;


public class UsbDrone extends CoreDrone {

    public boolean connect() {
        return false;
    }


    @Override
    protected void closeSocket() {

    }

    private static UsbDevice findDevice(UsbHub hub, short vendorId, short productId)  {
        //noinspection unchecked
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices())  {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) return device;
            if (device.isUsbHub())
            {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) return device;
            }
        }
        return null;
    }

    public static final byte GET_REPORT_DESCRIPTOR_REQUESTTYPE =
            UsbConst.REQUESTTYPE_DIRECTION_IN | UsbConst.REQUESTTYPE_TYPE_STANDARD | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE;
    public static final byte GET_REPORT_DESCRIPTOR_REQUEST = UsbConst.REQUEST_GET_DESCRIPTOR;
    public static final byte HID_DESCRIPTOR_TYPE_REPORT = 0x22;
    public static final short GET_REPORT_DESCRIPTOR_VALUE = HID_DESCRIPTOR_TYPE_REPORT << 8;


    // http://javax-usb.cvs.sourceforge.net/viewvc/javax-usb/javax-usb-example/src/MouseDriver.java?revision=1.1&view=markup
    @SuppressWarnings("unchecked")
    private static UsbInterface findSensordroneInterface(UsbHub hub, short vendorId, short productId)  {
        UsbDevice usbDevice = findDevice(hub, vendorId, productId);
        if (usbDevice == null) {
            return null;
        }
        // Process all configurations
        for (UsbConfiguration configuration: (List<UsbConfiguration>) usbDevice.getUsbConfigurations()) {
            for (UsbInterface usbInterface : (List<UsbInterface>) configuration.getUsbInterfaces()) {
                try {
                    usbInterface.claim();
                } catch (UsbException ue) {
                    // If claiming the interface fails, we will still try to check the usage.
                    // It may or may not work depending on how things are implemented lower down.
                }
                UsbDevice dev = usbInterface.getUsbConfiguration().getUsbDevice();
                /* These fields perform a get-descriptor request for a HID Report-type descriptor. */
                byte bmRequestType = GET_REPORT_DESCRIPTOR_REQUESTTYPE;
                byte bRequest = GET_REPORT_DESCRIPTOR_REQUEST;
                short wValue = GET_REPORT_DESCRIPTOR_VALUE;
                short wIndex = UsbUtil.unsignedShort( usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber() );
                UsbControlIrp getUsageIrp = usbDevice.createUsbControlIrp(bmRequestType, bRequest, wValue, wIndex);

                /* This is the buffer to place the descriptor in. */
                byte[] data = new byte[256];
                getUsageIrp.setData(data);

                try {
                    // This gets the Report-type descriptor (for this interface) from the device.
                    // This may throw a UsbException.
                    usbDevice.syncSubmit(getUsageIrp);

                    // The usage is the first 4 bytes, so if we didn't get at least 4 bytes back,
                    // something is wrong.
                    if (4 > getUsageIrp.getActualLength()) {
                        return null;
                    }

//                    if (UsbUtil.toInt(data[0], data[1], data[2], data[3]) == UsbUtil.toInt(usagePage, usageID)) {
//                        return usbInterface;
//                    }

                    // The usage didn't match.
                    return null;
                }
                catch (UsbException ue) {
                    return null;
                }
                finally {
                    // Try to release the interface
                    try { usbInterface.release(); }
                    catch ( UsbException uE ) {}
                }

            }
        }
        return null;
    }


    /* Dumps the specified USB device to stdout.
    *
            * @param device
    *            The USB device to dump.
    */
    private static void dumpDevice(final UsbDevice device)
    {
        // Dump information about the device itself
        System.out.println(device);
        final UsbPort port = device.getParentUsbPort();
        if (port != null)
        {
            System.out.println("Connected to port: " + port.getPortNumber());
            System.out.println("Parent: " + port.getUsbHub());
        }

        // Dump device descriptor
        System.out.println(device.getUsbDeviceDescriptor());

        // Process all configurations
        for (UsbConfiguration configuration: (List<UsbConfiguration>) device
                .getUsbConfigurations())
        {
            // Dump configuration descriptor
            System.out.println(configuration.getUsbConfigurationDescriptor());

            // Process all interfaces
            for (UsbInterface iface: (List<UsbInterface>) configuration
                    .getUsbInterfaces())
            {
                // Dump the interface descriptor
                System.out.println(iface.getUsbInterfaceDescriptor());

                // Process all endpoints
                for (UsbEndpoint endpoint: (List<UsbEndpoint>) iface
                        .getUsbEndpoints())
                {
                    // Dump the endpoint descriptor
                    System.out.println(endpoint.getUsbEndpointDescriptor());
                }
            }
        }

        System.out.println();

        // Dump child devices if device is a hub
        if (device.isUsbHub())
        {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child: (List<UsbDevice>) hub.getAttachedUsbDevices())
            {
                dumpDevice(child);
            }
        }
    }

    /**
     * Sends a message to the missile launcher.
     *
     * @param device
     *            The USB device handle.
     * @param message
     *            The message to send.
     * @throws UsbException
     *             When sending the message failed.
     */
    public static void sendMessage(UsbDevice device, byte[] message)
            throws UsbException
    {
        UsbControlIrp irp = device.createUsbControlIrp(
                (byte) (UsbConst.REQUESTTYPE_TYPE_CLASS |
                        UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE), (byte) 0x09,
                (short) 2, (short) 1);
        irp.setData(message);
        device.syncSubmit(irp);
    }

    public static void sendMsg(UsbDevice device) throws UsbException {

        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        for (UsbInterface usbInterface : (List<UsbInterface>)configuration.getUsbInterfaces()) {
            System.out.println("UsbIntegravce " + usbInterface.getSetting((byte) 0));
        }
        UsbInterface iface = configuration.getUsbInterface((byte) 0);
        iface.claim(usbInterface -> true);

        UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x81);
        UsbPipe pipe = endpoint.getUsbPipe();
        pipe.open();
        try {
            // Get Hardware / Firmware #
            byte[] readHWFW = {0x50, 0x02, 0x33, 0x00};
            int sent = pipe.syncSubmit(readHWFW);
            System.out.println(sent + " bytes sent");
        }
        finally
        {
            pipe.close();
        }

        iface.release();
    }



    public static void oldMain(String[] args) throws UsbException {
        // Get the USB services and dump information about them
        final UsbServices services = UsbHostManager.getUsbServices();
        System.out.println("USB Service Implementation: "
                + services.getImpDescription());
        System.out.println("Implementation version: "
                + services.getImpVersion());
        System.out.println("Service API version: " + services.getApiVersion());

        UsbDevice usbDevice = findDevice(services.getRootUsbHub(), (short) 0x10c4, (short) 0xea60);
        if (usbDevice == null) {
            System.out.println("No device found");
        }
        else {
            dumpDevice(usbDevice);
            sendMsg(usbDevice);
        }
    }

    /**
     * Get the virtual root UsbHub.
     * @return The virtual root UsbHub
     */


    public static UsbHub getVirtualRootUsbHub() {
        UsbServices services = null;
        UsbHub virtualRootUsbHub = null;

        /* First we need to get the UsbServices.
	     * This might throw either an UsbException or SecurityException.
	     * A SecurityException means we're not allowed to access the USB bus,
	     * while a UsbException indicates there is a problem either in
	     * the javax.usb implementation or the OS USB support.
	     */
        try {
            services = UsbHostManager.getUsbServices();
        } catch ( UsbException | SecurityException uE ) {
            throw new RuntimeException("Error : " + uE.getMessage());
        }

        /* Now we need to get the virtual root UsbHub,
	     * everything is connected to it.  The Virtual Root UsbHub
	     * doesn't actually correspond to any physical device, it's
	     * strictly virtual.  Each of the devices connected to one of its
	     * ports corresponds to a physical host controller located in
	     * the system.  Those host controllers are (usually) located inside
	     * the computer, e.g. as a PCI board, or a chip on the mainboard,
	     * or a PCMCIA card.  The virtual root UsbHub aggregates all these
	     * host controllers.
	     *
	     * This also may throw an UsbException or SecurityException.
	     */
        try {
            virtualRootUsbHub = services.getRootUsbHub();
        } catch ( UsbException | SecurityException uE ) {
            throw new RuntimeException("Error : " + uE.getMessage());
        }

        return virtualRootUsbHub;
    }

    public static final byte HID_CLASS = 0x03;




    public static void main(String[] args) throws UsbException {

        UsbHub virtualRootUsbHub = getVirtualRootUsbHub();



        //List usbInterfaces = FindUsbInterface.getUsbInterfacesWithInterfaceClass(virtualRootUsbHub, HID_CLASS);

    }
}
