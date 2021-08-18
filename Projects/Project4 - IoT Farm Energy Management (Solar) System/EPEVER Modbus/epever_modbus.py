# # # # # # # # # # # # # # # #
# Supported Modbus Functions  #
# # # # # # # # # # # # # # # # 

# # # # # # # # # # # # # # # 
# Read Input Register(0x04) #
# # # # # # # # # # # # # # # 


# Rated Data (Read Only)
# Variable_name                             Address (unit|times)    description
RTD_PV_VOLTAGE                          =   0x3000 #(V|100)         PV array rated voltage
RTD_PV_CURRENT                          =   0x3001 #(A|100)         PV array rated current
RTD_PV_POWER_L                          =   0X3002 #(W|100)         PV array rated power (low 16 bits)
RTD_PV_POWER_H                          =   0x3003 #(W|100)         PV array rated power (high 16 bits)
RTD_BT_VOLTAGE                          =   0x3004 #(V|100)         Rated voltage to battery
RTD_BT_CURRENT                          =   0x3005 #(A|100)         Rated current to battery 
RTD_BT_POWER_L                          =   0x3006 #(W|100)         Rated power to battery
RTD_BT_POWER_H                          =   0x3007 #(W|100)         Rated power to battery
CHARGING_MODE_                          =   0x3008 #                0000H Connect|disconnect, 0001H PWM,0002H MPPT
RTD_LD_CURRENT                          =   0x300E #(W|100)         Rated current of load

# Real-time Data (Read Only)
# Variable_name                             Address (unit|times)    description
PV_VOLTAGE                              =   0x3100 #(V|100)         Solar charge controller--PV array voltage
PV_CURRENT                              =   0X3101 #(A|100)         Solar charge controller--PV array current
PV_POWER_L                              =   0x3102 #(W|100)         Solar charge controller--PV array power
PV_POWER_H                              =   0x3103 #(W|100)         Solar charge controller--PV array power
BT_VOLTAGE                              =   0x3104 #(V|100)         Battery voltage
BT_CURRENT                              =   0x3105 #(A|100)         Battery charging current
BT_POWER_L                              =   0x3106 #(W|100)         Battery charging power
BT_POWER_H                              =   0x3107 #(W|100)         Battery charging power
LD_VOLTAGE                              =   0x310C #(V|100)         Load voltage
LD_CURRENT                              =   0x310D #(A|100)         Load current
LD_POWER_L                              =   0x310E #(W|100)         Load power
LD_POWER_H                              =   0x310F #(W|100)         Load power
BT_CELCIUS                              =   0x3110 #(℃|100)        Battery Temperature
EQ_CELCIUS                              =   0x3111 #(℃|100)        Temperature inside case
PC_CELCIUS                              =   0x3112 #(℃|100)        Heat sink surface temperature of equipments power components 
BT_PERCENT                              =   0x311A #(%|1)           The percentage of battery's remaining capacity
RMT_BT_TMP                              =   0x311B #(℃|100)        The battery temperature measured by remote temperature sensor
BT_RTD_PWR                              =   0x311D #(V|100)         Current system rated voltage. 1200, 2400, 3600, 4800 represent 12V，24V，36V，48 


# Real-time Status (Read Only)
# Variable_name                             Address (unit|times)    
BT_STATUS_                              =   0x3200 
# Battery Status
# D3-D0:    00H Normal, 01H Overvolt, 02H Under Volt, 03H Low Volt Disconnect, 04H Fault
# D7-D4:    00H Normal, 01H Over Temp.(Higher than the warning settings), 02H Low Temp.(Lower than the warning settings)
# D8:       0 normal, 1 Battery inner resistance abnormal
# D15:      1-Wrong identification for rated voltage          

CE_STATUS_                              =   0x3201 
# Charging Equipment Status
# D15-D14:  Input volt status. 00H normal, 01H no power connected, 02H Higher volt input, 03H Input volt error.
# D13:      Charging MOSFET is short.
# D12:      Charging or Anti-reverse  MOSFET is short.
# D11:      Anti-reverse MOSFET is short.
# D10:      Input is over current.
# D9:       The load is Over current.
# D8:       The load is short.
# D7:       Load MOSFET is short.
# D4:       PV Input is short.
# D3-2:     Charging status. 00 No charging, 01 Float Charge, 02 Boost Charge, 03 Equalization.
# D1:       0 Normal, 1 Fault.
# D0:       0 Standby, 1 Running.

DE_STATUS_                              =   0x3202
# Discharging Equipment Status
# D15-D14:  00H normal, 01H low, 02H High, 03H no access Input volt error.
# D13-D12:  output power:00-light load,01-moderate,02-rated,03-overload
# D11:      short circuit
# D10:      unable to discharge
# D9:       unable to stop discharging
# D8:       output voltage abnormal
# D7:       input overpressure
# D6:       high voltage side short circuit
# D5:       boost overpressure
# D4:       output overpressure
# D1:       0 Normal, 1 Fault.
# D0:       0 Standby, 1 Running.

# Statistical Parameters (Read Only)
#Variable_name                              Address (unit|times)    description
MAX_PV_VOLTAGE                          =   0x3300 #(V|100)         Maximum PV voltage today, 00: 00 Refresh every day    
MIN_PV_VOLTAGE                          =   0x3301 #(V|100)         Minimum PV voltage today, 00: 00 Refresh every day
MAX_BT_VOLTAGE                          =   0x3302 #(V|100)         Maximum battery voltage today, 00: 00 Refresh every day 
MIN_BT_VOLTAGE                          =   0x3303 #(V|100)         Minimum battery voltage today, 00: 00 Refresh every day 
CON_ENERGY_D_L                          =   0x3304 #(kWh|100)       Consumed energy today L, 00: 00 Clear every day   
CON_ENERGY_D_H                          =   0x3305 #(kWh|100)       Consumed energy today H, 00: 00 Clear every day   
CON_ENERGY_M_L                          =   0x3306 #(kWh|100)       Consumed energy this month L, 00: 00 Clear on the first day of month
CON_ENERGY_M_H                          =   0x3307 #(kWh|100)       Consumed energy this month H, 00: 00 Clear on the first day of month   
CON_ENERGY_Y_L                          =   0x3308 #(kWh|100)       Consumed energy this year L, 00: 00 Clear on 1, Jan  
CON_ENERGY_Y_H                          =   0x3309 #(kWh|100)       Consumed energy this year H, 00: 00 Clear on 1, Jan
CON_ENERGY_T_L                          =   0x330A #(kWh|100)       Total Consumed energy L  
CON_ENERGY_T_H                          =   0x330B #(kWh|100)       Total Consumed energy H   
GEN_ENERGY_D_L                          =   0x330C #(kWh|100)       Generated energy today L, 00: 00 Clear every day.  
GEN_ENERGY_D_H                          =   0x330D #(kWh|100)       Generated energy today H, 00: 00 Clear every day.  
GEN_ENERGY_M_L                          =   0x330E #(kWh|100)       Generated energy this month L, 00: 00 Clear on the first day of month. 
GEN_ENERGY_M_H                          =   0x330F #(kWh|100)       Generated energy this month H, 00: 00 Clear on the first day of month.  
GEN_ENERGY_Y_L                          =   0x3310 #(kWh|100)       Generated energy this year L, 00: 00 Clear on 1, Jan.  
GEN_ENERGY_Y_H                          =   0x3311 #(kWh|100)       Generated energy this year H, 00: 00 Clear on 1, Jan.  
GEN_ENERGY_T_L                          =   0x3312 #(kWh|100)       Total generated energy L 
GEN_ENERGY_T_H                          =   0x3313 #(kWh|100)       Total generated energy H 
CO2REDUCTION_L                          =   0x3314 #(Ton|100)       Saving 1 Kilowatt=Reduction 0.997KG"Carbondioxide "=Reduction 0.272KG"Carton''
CO2REDUCTION_H                          =   0x3315 #(Ton|100)       Saving 1 Kilowatt=Reduction 0.997KG"Carbondioxide "=Reduction 0.272KG"Carton''
BT_VOLTAGE                              =   0x331A #(V|100)         Battery voltage 
BT_CURRENT_L                            =   0x331B #(A|100)         Battery current L 
BT_CURRENT_H                            =   0x331C #(A|100)         Battery current H
BATTERY___TEMP                          =   0x331D #(℃|100)        Battery Temp.
AMBIENT___TEMP                          =   0x331E #(℃|100)        Ambient Temp.


# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 
# Read Holding Register(0x03) & Write Multiple Holding Register (0x10)  #
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# Setting Parameter (Read and Write)
#Variable_name                              Address (unit|times)    description
BATTERY_TYPE                            =   0x9000 #                0001H- Sealed , 0002H- GEL, 0003H- Flooded, 0000H- User defined
BATTERY_CAPACITY                        =   0x9001 #(Ah)            Rated capacity of the battery
TEMP_COMPENSATION_COEFFICIENT           =   0x9002 #(mV/℃/V)       Range 0-9
HIGH_VOLTAGE_DISCONNECT                 =   0x9003 #(V|100)
CHARGING_LIMIT_VOLTAGE                  =   0x9004 #(V|100)
OVER_VOLTAGE_RECONNECT                  =   0x9005 #(V|100)
EQUALIZATION_VOLTAGE                    =   0x9006 #(V|100)
BOOST_VOLTAGE                           =   0x9007 #(V|100)
FLOAT_VOLTAGE                           =   0x9008 #(V|100)
BOOST_RECONNECT_VOLTAGE                 =   0x9009 #(V|100)
LOW_VOLTAGE_RECONNECT                   =   0x900A #(V|100)
UNDER_VOLTAGE_RECONNECT                 =   0x900B #(V|100)
UNDER_VOLTAGE_WARNING                   =   0x900C #(V|100)
LOW_VOLTAGE_DISCONNECT                  =   0x900D #(V|100)
DISCHARGING_LIMIT_VOLTAGE               =   0x900E #(V|100)
REAL_TIME_CLOCK_SEC_MIN                 =   0x9013 #                D7-0 Sec, D15-8 Min.(Year, Month, Day, Min, Sec. should be written simultaneously)
REAL_TIME_CLOCK_HOUR_DAY                =   0x9014 #                D7-0 Hour, D15-8 Day
REAL_TIME_CLOCK_MONTH_YEAR              =   0x9015 #                D7-0 Month, D15-8 Year
EQUALIZATION_CHARGING_CYCLE             =   0x9016 #(Day)           Interval days of auto equalization charging in cycle
BT_TEMP_WARNING_UPPER_LIM               =   0x9017 #(℃|100)
BT_TEMP_WARNING_LOWER_LIM               =   0x9018 #(℃|100)
CTRLR_INNER_TEMP_UPPER_LIM              =   0x9019 #(℃|100)
CTRLR_INNER_TEMP_UPPER_LIM_RCVR         =   0x901A #(℃|100)        After Over Temperature, system recover once it drop to lower than this value
PWR_COMP_TEMP_UPPER_LMT                 =   0x901B #(℃|100)        Warning when surface temperature of power components higher than this value, and charging and discharging stop 
PWR_COMP_TEMP_UPPER_LMT_RCVR            =   0x901C #(℃|100)        Recover once power components temperature lower than this value         
LINE_IMPEDANCE                          =   0x901D #(mOhm|100)      The resistance of the connected wires
DAY_TIME_THRESHOLD_VOLTAGE              =   0x901E #(V|100)         PV lower than this value, controller would detect it as sundown
LIGHT_SIGNAL_STARTUP_DELAY_TIME         =   0x901F #(Min)           PV voltage lower than NTTV, and duration exceeds the Light signal startup (night) delay time, controller would detect it as night time.
LIGHT_TIME_THRESHOLD_VOLTAGE            =   0x9020 #(V|100)         PV voltage higher than this value, controller would detect it as sunrise
LIGHT_SIGNAL_CLOSE_DELAY_TIME           =   0x9021 #(Min)           PV voltage higher than DTTV, and duration exceeds the Light signal close (day) delay time, controller would detect it as day time.
LOAD_CONTROLLING_MODES                  =   0x903D #                0000H Manual Control 0001H Light ON/OFF 0002H Light ON+ Timer/ 0003H Time Control
WORKING_TIME_LENGTH_1                   =   0x903E #                The length of load output timer1, D15-D8, hour, D7-D0, minute
WORKING_TIME_LENGTH_2                   =   0x903F #                The length of load output timer2, D15-D8, hour, D7-D0, minute
TURN_ON_TIMING_1_SEC                    =   0x9042 #(Sec)           Turn on/off timing of load output.
TURN_ON_TIMING_1_MIN                    =   0x9043 #(Min)           Turn on/off timing of load output.
TURN_ON_TIMING_1_HOUR                   =   0x9044 #(Hour)          Turn on/off timing of load output.
TURN_OFF_TIMING_1_SEC                   =   0x9045 #(Sec)           Turn on/off timing of load output.
TURN_OFF_TIMING_1_MIN                   =   0x9046 #(Min)           Turn on/off timing of load output.
TURN_OFF_TIMING_1_HOUR                  =   0x9047 #(Hour)          Turn on/off timing of load output.
TURN_ON_TIMING_2_SEC                    =   0x9048 #(Sec)           Turn on/off timing of load output.
TURN_ON_TIMING_2_MIN                    =   0x9049 #(Min)           Turn on/off timing of load output.
TURN_ON_TIMING_2_HOUR                   =   0x904A #(Hour)          Turn on/off timing of load output.
TURN_OFF_TIMING_2_SEC                   =   0x904B #(Sec)           Turn on/off timing of load output.
TURN_OFF_TIMING_2_MIN                   =   0x904C #(Min)           Turn on/off timing of load output.
TURN_OFF_TIMING_2_HOUR                  =   0x904D #(Hour)          Turn on/off timing of load output.
BACKLIGHT_TIME                          =   0x9063 #(Sec)           Close after LCD backlight light setting the number of seconds
LENGTH_OF_NIGHT                         =   0x9065 #                Set default values of the whole night length of time. D15-D8, hour, D7-D0, minute
DVC_CONFIG_OF_MAIN_PWR_SUP              =   0x9066 #                0001H Battery is main，0002H AC-DC power mainly
BATTERY_RATED_VOLTAGE_CODE              =   0x9067 #                0-auto recognize, 1-12V, 2-24V, 3-36V，4-48V，5-60V，6-110V，7-120V，8-220V，9-240V
DEFAULT_LOAD_ON_OFF_IN_MANUAL_MODE      =   0x906A #                0-off, 1-on
EQUALIZE_DURATION                       =   0x906B #(Min)           Usually 0-120 minutes
BOOST_DURATION                          =   0x906C #(Min)           Usually 10-120 minutes
DISCHARGING_PERCENTAGE                  =   0x906D #(%|100)         Usually 20%-80%. The percentage of battery's remaining capacity when stop charging
CHARGING_PERCENTAGE                     =   0x906E #(%|100)         Depth of charge, 100%
BT_CH_DISCH_MNGMT_MODES                 =   0x9070 #                Management modes of battery charge and discharge, voltage compensation : 0 and SOC : 1


# # # # # # # # # # # # # # # # # # # # # # # # 
# Read Coils(0x01) & Write Single Coil(0x05)  #
# # # # # # # # # # # # # # # # # # # # # # # # 

# Switch Value (Read and Write)
#Variable_name                              Address description
CHARGING_DEVICE_ON_OFF                  =   0x000   # 1 Charging device on | 0 Charging device off
OUTPUT_CONTROL_MODE_MANUAL_AUTOMATIC    =   0x001   # 1 Output control mode manual | 0 Output control mode automatic
MANUAL_CONTROL_THE_LOAD                 =   0x002   # When the load is manual mode，1-manual on | 0-manual off
DEFAULT_CONTROL_THE_LOAD                =   0x003   # When the load is default mode，1-manual on | 0-manual off
ENABLE_LOAD_TEST_MODE                   =   0x005   # 1 Enable | 0 Disable(normal)
FORCE_THE_LOAD_ON_OFF                   =   0x006   # 1 Turn on | 0 Turn off (used for temporary test of the load）
RESTORE_SYSTEM_DEFAULTS                 =   0x013   # 1 yes | 0 no
CLEAR_GENERATING_ELECTRICITY_STATISTICS =   0x014   # 1 clear. Root privileges to perform

# # # # # # # # # # # # # # # # #
#  Read Discrete Inputs (0x02)  #
# # # # # # # # # # # # # # # # #

# Discrete Value (Read Only)
#Variable_name                              Address description
OVER_TEMPERATURE_INSIDE_THE_DEVICE      =   0x2000  # 1-The temperature inside the controller is higher than the over-temperature protection point. 0-Normal
DAY_NIGHT                               =   0x200C  # 1-Night, 0-Day