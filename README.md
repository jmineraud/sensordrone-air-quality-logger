# sensordrone-air-quality-logger
Periodically reads data from a given sensordrone to the defined output

# Usage

`./gradlew run -PappArgs="['-d', '10000', '-m', 'xx:xx:xx:xx', '-lat', '1.0', '-lon', '2.0']" 2>> error 1>> output`

where -d is the delay between the request for samples
      -m is the mac address of the sensordrone
      -lat/lon are the latitude and longitude of these sensors (float)

# Requirements

libbluetooth-dev on Ubuntu
bluez-libs-devel on Fedora
bluez-devel on openSUSE

Ubuntu 7.04 feisty (bluez 3.8) and later
OpenSUSE 10.2 (bluez-libs-3.7) and later
Fedora Core 6 (bluez-libs-3.7) and later
Debian 4.0r3 ARM on the Linksys NSLU2 (bluez-libs-3.7)

# For the USB
Copy following file to /etc/udev/rules.d/99-sensordrone.rules as root
SUBSYSTEM=="usb",ATTR{idVendor}=="8087",ATTR{idProduct}=="0024",MODE="0660",GROUP="wheel"
