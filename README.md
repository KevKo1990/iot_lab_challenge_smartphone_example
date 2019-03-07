## Bosch IoT Lab smartphone example for START hack 2019 challenge

We provide you an example smartphone client to access the ALEN car CAN data streams.

Happy coding with the Bosch IoT Lab challenge!

#### Helpful information to get started

##### Available documentation
- Pitch presentation: https://www.iot-lab.ch/wp-content/uploads/2019/03/START-Hack-pitch.pdf
- Workshop presentation: https://www.iot-lab.ch/wp-content/uploads/2019/03/START-Hack-workshop.pdf
- Documentation for all signals: https://www.iot-lab.ch/wp-content/uploads/2019/03/START-Hack-available-signals.pdf

##### Config
- In assets/config.json is the IP address of the ALEN box specified

##### Technical information
- IP address of webserver in all provided networks: 192.168.8.10
- WLAN Car: SSID HUAWEI-2D17 Password: Wa1z3gouvASQ5!
- WLAN Car simulation / CANalyzer: SSID FRITZ!Box 7490 Bosch IoT Password: 98871595452296495993
- FTP storage: ask our team!
- Empatica E4 connect portal: ask our team!

##### REST queries
- REST queries are done with the Google Volley library: https://developer.android.com/training/volley/
- Example queries are in MainActivity, GraphFragment, CANWebsocketClient, and SensorNameDropdownFragment

###### REST queries
<table>
    <th>
        Query
    </th>
    <th>
        Description
    </th>
    <th>
        Examples
    </th>
    <tr>
        <td>
            /list
        </td>
        <td>
            Lists all available signals
        </td>
        <td>
            {"gateway":{"signals":["speed", "acceleration", "steering_wheel_angle",...]}}
        </td>
    </tr>
    <tr>
        <td>
            /signal/'signal_name'
        </td>
        <td>
            Provides detailed information (e.g. description and last value) on a single signal.<br \>
            <br \>
            signal_name = a signal name from signal list
        </td>
        <td>
            {"signal":{"definition":{"name":"speed","unit":"Unit_PerCent","description":"in km/h"},"measurement":{"value":83.13725490196077,"utc":1550761385086735000}}
        </td>
    </tr>
    <tr>
        <td>
            /signal/'signal_name'/value
        </td>
        <td>
            Provides last value on a single signal.<br \>
            <br \>
            signal_name = a signal name from signal list
        </td>
        <td>
            {"measurement":{"value":83.13725490196077,"utc":1550761385086735000}}
        </td>
    </tr>
</table>

##### Websocket
- Websocket connection is established with the Java WebSocket library: https://github.com/TooTallNate/Java-WebSocket
- Example implementations are the CANWebsocketClient and in LogFragment the CANDataClient class.

###### Websocket events
<table>
    <th>
        Event
    </th>
    <th>
        Description
    </th>
    <th>
        Examples
    </th>
    <tr>
        <td>
            onOpen
        </td>
        <td>
            send on onOpen event a message on which signals and on which interval the websocket should receive messages<br \>
            <br \>
            signals = list of signal names <br \>
            <br \>
            samplerate = interval in ms <br \>
            <br \>
            withtimestamp = boolean if values should have a timestamp when the signal was last changed
        </td>
        <td>
            {"signals":["speed", "acceleration", "steering_wheel_angle",...], "samplerate":250, "withtimestamp":true}
        </td>
    </tr>
    <tr>
        <td>
            onMessage(String)
        </td>
        <td>
            The event that receives incoming messages
        </td>
        <td>
            Without timestamp: {"signals": {"speed":{"value":234.32, "utc":0}, "acceleration":{"value": 21.21, "utc":0}, "steering_wheel_angle":{"value":432,26, "utc":0}, ...}}<br \>
            <br \>               
            With timestamp: {"signals": {"speed":{"value":234.32, "utc":1551865379474022000}, "acceleration":{"value": 21.21, "utc":1551865379535070000}, "steering_wheel_angle":{"value":432,26, "utc":1551865379535070000}, ...}}
        </td>
    </tr>
</table>

##### Biometric sensors
Ask our team to borrow the devices!
###### Empatica E4
- Empatica E4 Connect (online platform for accessing data): https://www.empatica.com/connect
- Streaming app from phone to E4 Connect (Android): https://play.google.com/store/apps/details?id=com.empatica.e4realtime
- Streaming app from phone to E4 Connect (iOS): https://itunes.apple.com/us/app/empatica-e4-realtime/id702791633
- Signals in detail: http://box.empatica.com/documentation/20141119_E4_TechSpecs.pdf
###### Firstbeat
- PC client to read the data from device: https://we.tl/t-cXHvADb4fk
- Signals in detail: https://www.iot-lab.ch/wp-content/uploads/2019/03/Firstbeat-SPORTS-Physiological-and-GPS-variables.pdf
##### Ideas
- The car interior of the future: https://www.youtube.com/watch?v=iSVt5Ja0ZCk
###### "The observing car"
- Volkswagen Connect: https://www.vwconnect.com/driving-style-analysis/
- Mercedes InScore (only in German): https://www.mercedes-benz-bank.de/content/mbbank/de/produkte/versicherung/telematik-autoversicherung.html
- A more general approach: https://dl.acm.org/citation.cfm?id=3133939
###### "The emphatic car"
- Apple Watch EKG monitor: https://www.youtube.com/watch?v=3cE5kA6XDSY
- Empatica Embrace 2: https://www.empatica.com/
- A stress related approach: https://ieeexplore.ieee.org/abstract/document/7744577
###### "The caretaking car"
- Tesla Santa Mode as general example: https://www.youtube.com/watch?v=79Vx3spRFBk
- Mercedes Vivoactive: https://www.garmin.com/en-US/blog/general/garmin-collaborates-with-daimler-to-bring-connected-features-to-mercedes-benz-vehicles-with-the-vivoactive-3-gps-smartwatch/
- Audi Fit Driver: https://www.quattroworld.com/audi-news/audi-fit-driver/

### More information about the event:
- Official START hack website: https://starthack.ch/
- Bosch IoT Lab website: https://www.iot-lab.ch/