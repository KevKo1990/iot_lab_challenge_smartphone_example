## Bosch IoT Lab smartphone example for START hack 2019 challenge

We provide you an example smartphone client to access the ALEN car CAN data streams.

Happy coding with the Bosch IoT Lab challenge!

#### Helpful information to get started

##### Config
- In assets/config.json is the IP address of the ALEN box specified

##### REST queries
- REST queries are done with the Volley library: https://developer.android.com/training/volley/
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
            signals = list of signal names
            samplerate = interval in ms
            withtimestamp = boolean if values should have a timestamp when they were last changed
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
            Without timestamp: {"signals": {"speed":{"value":234.32}, "acceleration":{"value": 21.21}, "steering_wheel_angle":{"value":432,26}, ...}}
        </td>
    </tr>
</table>

##### Biometric sensor
- Empatica E4 Connect: https://www.empatica.com/connect

##### Ideas
- Health interventions(a more general approach): https://www.youtube.com/watch?v=79Vx3spRFBk
- The predictive car (an example): https://ieeexplore.ieee.org/abstract/document/7744577
- Driving capability (a more general approach): https://dl.acm.org/citation.cfm?id=3133939

### More information about the event:
- Official START hack website: https://starthack.ch/
- Bosch IoT Lab website: https://www.iot-lab.ch/