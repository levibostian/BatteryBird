## QA test cases 

#### Bluetooth permissions 
- [ ] Demo devices shown before permissions accepted. 
- [ ] After accept permissions, immediately see app populate with devices list and start to populate the battery levels. 
- [ ] Go into OS settings, deny permissions, go back into app. Should see cached data for devices and battery levels. 

#### Manually add devices 
- [ ] Help message shows when press help button on add devices screen. 
- [ ] Try adding a device with invalid hardware address. See error message. 
- [ ] Try adding same device hardware address multiple times. Should not see duplicate or replaced device. Should ignore request. 
- [ ] After adding a device, should see app try and get the battery level immediately after adding. 
- [ ] Add `ff:ff:ff:ff:ff:ff` as a device. I guess it's an invalid address according to the Android OS. So, Android `getRemoteDevice` will throw an exception. Verify that the app does not throw an exception. 

#### Devices list 
- [ ] App should update battery level immediately when app opens. 

### Broadcast receiver 
- [ ] When a device connects to OS, the battery level should be checked immediately. Test when app is killed. 

### Periodic battery level updates 
- [ ] When app is killed, battery level should be updated every 15 minutes. 

### Weird behaviors for some devices 
- [ ] Test app with Sony WH-1000XM5 headphones. This device when trying to use GATT causes the headphones to turn off. App should be able to get the bluetooth connection status and battery level when the headphones are connected and disconnected. 
- [ ] Test app with Coros Pace 2 watch. This device only works by manually adding the hardware address and using GATT to get battery level. 