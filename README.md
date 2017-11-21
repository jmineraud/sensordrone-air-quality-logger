# sensordrone-air-quality-logger
Periodically reads data from a given sensordrone to the defined output

# Usage

`gradle run -d 30000 -m xx:xx:xx:xx -lat LT -lon LN 2>> error 1>> output`

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

