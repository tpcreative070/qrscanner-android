adb tcpip 9999
adb -P 9999 start-server

#adb connect <DEVICE_IP_ADDRESS>:9999


# The issue could not connect to the devices try to do some steps below
#Issue on >= Android 10(Turn off Wifi debug on all devices Android 11 later, Our purpose on Android >= Android)
# reset wifi
# Close IDE and Open
# adb kill-server
#lsof -i :9999 and 5555
#kill -9 <PID>
# Start port and server again
#adb tcpip 9999
#adb -P 9999 start-server

#nano ~/.zshrc
#export PATH=$PATH:/Volumes/Data/Android/sdk/platform-tools
#export ANDROID_HOME=/Volumes/Data/Android/sdk
#export PATH="$HOME/.bin:$PATH"
#export PATH="/Volumes/Data/Android/sdk/platform-tools":$PATH
#export PATH=${PATH}:$HOME/Volumes/Data/Android/sdk/platform-tools/


#Install apk

#adb -s 192.168.1.5:9999 install app-qrscanner-freerelease.apk
#adb -s adb-R58W20L08TZ-xyrRgB._adb-tls-connect._tcp. install app-qrscanner-freerelease.apk
#adb -s adb-YPQSDEE6SSO7EI9S-NvPcm9._adb-tls-connect._tcp. install app-qrscanner-freerelease.apk