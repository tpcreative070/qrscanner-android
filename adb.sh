adb tcpip 9999
adb -P 9999 start-server

#adb connect <DEVICE_IP_ADDRESS>:9999

#lsof -i :9999
#
#kill -9 <PID>