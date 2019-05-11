# FaceRecognitionAttendance(FRA)-Client-App
<p align="left">
	<img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/icon.png" alt="Sample"  width="100" height="100"></p>

This client application can help you streamline your employee sign-in process by simply running it on an Android device that supports front-facing cameras.

## Interface

After opening this application, you will see the following interfaces:

### Administrator login

<p align="left">
	<img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot0.jpg" alt="Sample"  width="360" height="640"></p>

### Attendance

<p align="left">
	<img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot1.jpg" alt="Sample"  width="360" height="640">
</p>

### Employee information registration

<p align="left">
	<img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot2.jpg" alt="Sample"  width="360" height="640">
  <img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot3.jpg" alt="Sample"  width="360" height="640">
</p>

### Settings


<p align="left">
	<img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot7.jpg" alt="Sample"  width="360" height="640">
</p>

### Shortcuts

<p align="left">
	<img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot8.jpg" alt="Sample"  width="360" height="640">
</p>

**In addition, if you can find the egg of this application, you will get employee information management functions additionally:**

### Management

<table>
  <tr>
    <td>Employee information details display:</td>
    <td>Delete retired employee information<br>(can be withdrawn):</td>
    <td>Modify employee information:</td>
  </tr>
  <tr>
    <td><img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot4.jpg" alt="Sample"  width="240" height="427"></td>
    <td><img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot5.jpg" alt="Sample"  width="240" height="427"></td>
    <td><img src="https://github.com/nbcsproject/FaceRecognitionAttendance-FRA/blob/master/FRA_Client-Server/App/Screenshots/Screenshot6.jpg" alt="Sample"  width="240" height="427"></td>
  </tr>
</table>

## Application function

### This application relies on a remote server. After the server is turned on, you will be able to use the following features:

- Employee attendance based on face detection
- Information registration and face entry for new employees
- Set the attendance time interval
- Verify the fingerprint to ensure the process is self-operating
- Automatically exclude the same employee from checking in repeatedly
- Multiple administrator account switch
- Thorough Simplified Chinese and English support
- Quick entry(App shortcuts)
- More……

### After opening the application egg, you will get the following additional features:

- View all employee information
- Delete employee information in a single/bulk
- Deleted employee information can be withdrawn in a short time
- Modify the employee information that has been entered

## Precautions

- All major functions can be switched by swiping the pop-up side-slip menu to the right.
- This app will only apply for **Camera** permissions on your device. Individual ROMs will prompt the app to request additional permissions. You can choose to ignore or deny permissions.
- This application is completely dependent on the remote server. Please keep the network connection during the whole process.
- The default operation after fingerprint verification is the user's own operation, so please clear the activity of **FaceAttendance** in the background after exiting.
- Some feature entries will not appear until the app is opened, because we recommend you to use the more powerful web page management interface.

## Change logs

### 1.0.0

- Fix popup prompt when not connected to the server at attendance
- Open multi-language switching
- Set interface UI logic adjustment
- Fix known issues

### 0.9.3

- Fix the problem that the application control is incomplete under the device landscape
- Fix known issues

### 0.9.0

- Fix multi-user login data conflicts
- Add a loading progress bar to the web interface
- The web interface supports pressing the back button to return to the previous web interface
- Automatically handle the same user repeated check-in
- Fix known issues

### 0.8.1

- About the application interface, you can view all the open source frameworks used by this application, thanks to the open source spirit of the framework authors
- Interface operation animation adjustment
- Individual interface increases right slide exit gesture
- Faster information upload speed

### 0.7.1

- Fix an issue where the application failed to connect to the server
- Add employee attendance time display
- Remove the administrator registration function (please go to the web-side management interface to register)
- Fix known issues

### 0.6.1

- Open fingerprint authentication for all users (please ensure that your device's system version is above Android 6.0 and supports fingerprint module)
- Strengthen the logical judgment when setting the attendance time interval

### 0.5.0

- Great UI adjustments give you a different new experience
- Attendance interface pop-up window support automatically disappears
- Allow multiple administrator account login switch
- Improve the accuracy of the face recognition algorithm
- Add a custom check-in time interval in the settings
- Add fingerprint authentication function in settings (only partially open)
- Remove management features (you can still open it by opening the application egg)
- Multi-language switching function (test phase)
- Add feature quick entry(App shortcuts)
- Fix known issues

### 0.3.2

- Add settings interface
- Management interface increases operation withdrawal function in a short time (test phase)
- Management interface supports batch deletion of retired employee information (test phase)
- Modify the interface support to modify based on the original information (test phase)

### 0.3.0

- Add management interface (test phase)
- Fix known issues

### 0.2.0

- Add employee information registration interface
- Adjust some UI logic
- Fix the problem of camera preview interface distortion

### 0.1.2

- Fixed the problem that the application could not call the front camera on Huawei, LG, LeTV and other devices

## Acknowledgement

This application uses several open source frameworks in the development process, which can be viewed in the application via `Settings`-`Application version`-`Open source related`, thanks to the open source spirit of these framework authors.