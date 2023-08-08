## QA test cases 

#### Bluetooth permissions 
- [ ] Demo devices shown before permissions accepted. 
- [ ] After accept permissions, immediately see app populate with devices list and start to populate the battery levels. 
- [ ] Go into OS settings, deny permissions, go back into app. Should see cached data for devices and battery levels. 

#### Manually add devices 
- [ ] Add device button not visible when showing demo devices.
- [ ] Help message shows when press help button on add devices screen. 
- [ ] Try adding a device with invalid hardware address. See error message. 
- [ ] Try adding same device hardware address multiple times. Should not see duplicate or replaced device. Should ignore request. 
- [ ] After adding a device, should see app try and get the battery level immediately after adding. 
- [ ] Add `ff:ff:ff:ff:ff:ff` as a device. I guess it's an invalid address according to the Android OS. So, Android `getRemoteDevice` will throw an exception. Verify that the app does not throw an exception. 

#### Devices list 
- [ ] When app in foreground, should see app check battery level periodically. 
- [ ] When app is background (not killed, just background), app should not check battery level periodically. 

### Broadcast receiver 
- [ ] When a device connects to OS, the battery level should be checked immediately. 