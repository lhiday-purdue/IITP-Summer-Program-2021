from epever_modbus import *

def V():
    return [ 'Voltage', 'V' ]
def A():
    return [ 'Ampere', 'A' ]
def AH():
    return [ 'Ampere hours', 'Ah' ]
def W():
    return [ 'Watt', 'W' ]
def C():
    return [ 'degree Celsius', 'C' ] # ℃
def PC():
    return [ 'percentage', '%' ]
def KWH():
    return [ 'kiloWatt/hour', 'kWh' ]
def Ton():
    return [ 'Ton', 't' ]
def MO():
    return [ 'milliohm', 'mOhm' ]
def SEC():
    return [ 'seconds', 'sec' ]
def MIN():
    return [ 'minutes', 'min' ]
def HOUR():
    return [ 'hours', 'h' ]
def DAY():
    return [ 'days', 'd' ]
def N():
    return [ 'none', '' ]

class Register:
    def __init__(self, name, address, unit, times, description, size = 1):
        self.name = name
        self.address = address
        self.unit = unit
        self.times = times
        self.size = size
        self.description = description

    def is_coil(self):
        return self.address < 0x1000

    def is_discrete_input(self):
        return self.address >= 0x1000 and self.address < 0x3000

    def is_input_register(self):
        return self.address >= 0x3000 and self.address < 0x9000

    def is_holding_register(self):
        return self.address >= 0x9000

Rated_Data = [
    Register("PV array rated voltage", RTD_PV_VOLTAGE, V, 100, "PV array rated voltage"),
    Register("PV array rated current", RTD_PV_CURRENT, A, 100, "PV array rated current"),
    Register("PV array rated power", [RTD_PV_POWER_L, RTD_PV_POWER_H] , W, 100, "PV array rated power (low 16 bits)", 2),
    Register("Battery rated voltage", RTD_BT_VOLTAGE, V, 100, "Rated voltage to battery"),
    Register("Battery rated current", RTD_BT_CURRENT, A, 100,"Rated current to battery"),
    Register("Battery rated power", [RTD_BT_POWER_L, RTD_BT_POWER_H], W, 100, "Rated power to battery", 2),
    Register("Charging mode", CHARGING_MODE_, N, 1, "0000H Connect|disconnect, 0001H PWM,0002H MPPT"),
    Register("Rated current of load", RTD_LD_CURRENT, A, 100, "Rated current of load")]

Real_Time_Data = [
    Register("PV array input voltage",PV_VOLTAGE, V, 100, "Solar charge controller--PV array voltage"),
    Register("PV array input current",PV_CURRENT, A, 100, "Solar charge controller--PV array current"),
    Register("PV array input power", [PV_POWER_L, PV_POWER_H], W, 100, "Solar charge controller--PV array power", 2),
    Register("Battery voltage",BT_VOLTAGE, V, 100, "Battery voltage"),
    Register("Battery current",BT_CURRENT, A, 100, "Battery charging current"),
    Register("Battery power", [BT_POWER_L, BT_POWER_H], W, 100, "Battery charging power", 2),
    Register("Load voltage",LD_VOLTAGE, V, 100, "Load voltage"),
    Register("Load current",LD_CURRENT, A, 100, "Load current"),
    Register("Load power", [LD_POWER_L, LD_POWER_H], W, 100, "Load power", 2),
    Register("Battery temperature", BT_CELCIUS, C, 100, "Battery Temperature"),
    Register("Temperature inside equipment", EQ_CELCIUS, C, 100, "Temperature inside case"),
    Register("Power components temperature", PC_CELCIUS, C, 100, "Heat sink surface temperature of equipments power components "),
    Register("Battery SOC", BT_PERCENT, PC, 1, "The percentage of battery's remaining capacity"),
    Register("Remote battery temperature", RMT_BT_TMP, C, 100, "The battery temperature measured by remote temperature sensor"),
    Register("Battery's real rated power", BT_RTD_PWR, V, 100, "Current system rated voltage. 1200, 2400, 3600, 4800 represent 12V，24V，36V，48")
]

Real_Time_Status = [
    Register("Battery status", BT_STATUS_, N, 1, 
    "\
        D3-D0:    01H Overvolt , 00H Normal , 02H Under Volt, 03H Low Volt Disconnect, 04H Fault\
        D7-D4: 00H Normal, 01H Over Temp.(Higher than the warning settings), 02H Low Temp.(Lower than the warning settings)\
        D8: Battery inner resistance abnormal 1,normal 0\
        D15: 1-Wrong identification for rated voltage\
    "),    
    Register("Charing equipment status", CE_STATUS_, N, 1, 
    "\
        D15-D14:  Input volt status. 00 normal, 01 no power connected, 02H Higher volt  input, 03H Input volt error.\
        D13:      Charging MOSFET is short.\
        D12:      Charging or Anti-reverse  MOSFET is short.\
        D11:      Anti-reverse MOSFET is short.\
        D10:      Input is over current.\
        D9:       The load is Over current.\
        D8:       The load is short.\
        D7:       Load MOSFET is short.\
        D4:       PV Input is short.\
        D3-2:     Charging status. 00 No  charging,01 Float,02 Boost, 03 Equalization.\
        D1:       0 Normal, 1 Fault.\
        D0:       1 Running, 0 Standby.\
    "), 
    Register("Discharging equipment status", DE_STATUS_, N, 1, 
    "\
        D15-D14:  00H normal, 01H low, 02H High, 03H no accessInput volt error.\
        D13-D12:  output power:00-light load,01-moderate,02-rated,03-overload\
        D11:      short circuit\
        D10:      unable to discharge\
        D9:       unable to stop discharging\
        D8:       output voltage abnormal\
        D7:       input overpressure\
        D6:       high voltage side short circuit\
        D5:       boost overpressure\
        D4:       output overpressure\
        D1:       0 Normal, 1 Fault.\
        D0:       1 Running, 0 Standby\
    ")
]

Stat_Param = [
    Register("Maximum input volt (PV) today", MAX_PV_VOLTAGE, V, 100, "Maximum PV voltage today, 00: 00 Refresh every day"),
    Register("Minimum input volt (PV) today", MIN_PV_VOLTAGE, V, 100, "Minimum PV voltage today, 00: 00 Refresh every day"),
    Register("Maximum battery volt today ", MAX_BT_VOLTAGE, V, 100, "Maximum battery voltage today, 00: 00 Refresh every day"),
    Register("Minimum battery volt today", MIN_BT_VOLTAGE, V, 100, "Minimum battery voltage today, 00: 00 Refresh every day"),
    Register("Consumed energy today", [CON_ENERGY_D_L, CON_ENERGY_D_H], KWH, 100, "Consumed energy today, 00: 00 Clear every day", 2),
    Register("Consumed energy this month", [CON_ENERGY_M_L, CON_ENERGY_M_H], KWH, 100, "Consumed energy this month, 00: 00 Clear on the first day of month", 2),  
    Register("Consumed energy this year", [CON_ENERGY_Y_L, CON_ENERGY_Y_H], KWH, 100, "Consumed energy this year, 00: 00 Clear on 1, Jan", 2),
    Register("Total consumed energy", [CON_ENERGY_T_L, CON_ENERGY_T_H], KWH, 100, "Total Consumed energy", 2),
    Register("Generated energy today", [GEN_ENERGY_D_L, GEN_ENERGY_D_H], KWH, 100, "Generated energy today, 00: 00 Clear every day.", 2),
    Register("Generated energy this month", [GEN_ENERGY_M_L, GEN_ENERGY_M_H], KWH, 100, "Generated energy this month L, 00: 00 Clear on the first day of month.", 2) ,
    Register("Generated energy this year", [GEN_ENERGY_Y_L, GEN_ENERGY_Y_H], KWH, 100, "Generated energy this year L, 00: 00 Clear on 1, Jan.", 2),
    Register("Total generated energy", [GEN_ENERGY_T_L, GEN_ENERGY_T_H], KWH, 100, "Total generated energy", 2),
    Register("Carbon dioxide reduction", [CO2REDUCTION_L, CO2REDUCTION_H], Ton, 100, "Saving 1 Kilowatt=Reduction 0.997KG\"Carbondioxide \"=Reduction 0.272KG\"Carton\"", 2),
    Register("Battery voltage", BT_VOLTAGE, V, 100 , "Battery voltage"),
    Register("Battery Current", [BT_CURRENT_L, BT_CURRENT_H], A, 100 , "Battery current", 2),
    Register("Battery Temp.", BATTERY___TEMP, C, 100 , "Battery Temp."),
    Register("Ambient Temp.", AMBIENT___TEMP, C, 100 , "Ambient Temp.")
]

Setting_Param = [
    Register("Battery Type", BATTERY_TYPE, N, 1 ,"0001H- Sealed , 0002H- GEL, 0003H- Flooded, 0000H- User defined"),
    Register("Battery Capacity", BATTERY_CAPACITY, AH, 1, "Rated capacity of the battery"),
    Register("Temperature compensation coefficient", TEMP_COMPENSATION_COEFFICIENT, N, 1, "Range 0-9"),
    Register("High Volt.disconnect", HIGH_VOLTAGE_DISCONNECT, V, 100, ""),
    Register("Charging limit voltage", CHARGING_LIMIT_VOLTAGE, V, 100, ""),
    Register("Over voltage reconnect", OVER_VOLTAGE_RECONNECT, V, 100, ""),
    Register("Equalization voltage", EQUALIZATION_VOLTAGE, V, 100, ""),
    Register("Boost voltage", BOOST_VOLTAGE, V, 100, ""),
    Register("Float voltage", FLOAT_VOLTAGE, V, 100, ""),
    Register("Boost reconnect voltage", BOOST_RECONNECT_VOLTAGE, V, 100, ""),
    Register("Low voltage reconnect", LOW_VOLTAGE_RECONNECT, V, 100, ""),
    Register("Under voltage recover", UNDER_VOLTAGE_RECONNECT, V, 100, ""),
    Register("Under voltage warning", UNDER_VOLTAGE_WARNING, V, 100, ""),
    Register("Low voltage disconnect", LOW_VOLTAGE_DISCONNECT, V, 100, ""),
    Register("Discharging limit voltage", DISCHARGING_LIMIT_VOLTAGE, V, 100, ""),
    Register("Real time clock", REAL_TIME_CLOCK_SEC_MIN, N, 1, "D7-0 Sec, D15-8 Min.(Year, Month, Day, Min, Sec. should be written simultaneously)"),
    Register("Real time clock", REAL_TIME_CLOCK_HOUR_DAY, N, 1, "D7-0 Hour, D15-8 Day"),
    Register("Real time clock", REAL_TIME_CLOCK_MONTH_YEAR, N, 1, "D7-0 Month, D15-8 Year"),
    Register("Equalization charging cycle", EQUALIZATION_CHARGING_CYCLE, DAY, 1, "Interval days of auto equalization charging in cycle"),
    Register("Battery temperature warning upper limit", BT_TEMP_WARNING_UPPER_LIM, C, 100, ""),
    Register("Battery temperature warning lower limit", BT_TEMP_WARNING_LOWER_LIM, C, 100, ""),
    Register("Controller inner temperature upper limit", CTRLR_INNER_TEMP_UPPER_LIM, C, 100, ""),
    Register("Controller inner temperature upper limit recover", CTRLR_INNER_TEMP_UPPER_LIM_RCVR, C, 100, "After Over Temperature, system recover once it drop to lower than this value"),
    Register("Power component temperature upper limit", PWR_COMP_TEMP_UPPER_LMT, C, 100, "Warning when surface temperature of power components higher than this value, and charging and discharging stop"),
    Register("Power component temperature upper limit recover", PWR_COMP_TEMP_UPPER_LMT_RCVR, C, 100, "Recover once power components temperature lower than this value"),
    Register("Line Impedance", LINE_IMPEDANCE, MO, 100, "The resistance of the connected wires"),
    Register("Night TimeThreshold Volt.(NTTV)", DAY_TIME_THRESHOLD_VOLTAGE, V, 100, "PV lower than this value, controller would detect it as sundown"),
    Register("Light signal startup (night) delay time", LIGHT_SIGNAL_STARTUP_DELAY_TIME, MIN, 1, "PV voltage lower than NTTV, and duration exceeds the Light signal startup (night) delay time, controller would detect it as night time."),
    Register("Day Time Threshold Volt.(DTTV) ", LIGHT_TIME_THRESHOLD_VOLTAGE, V, 100, "PV voltage higher than this value, controller would detect it as sunrise"),
    Register("Light signal close (day) delay time", LIGHT_SIGNAL_CLOSE_DELAY_TIME, MIN, 1, "PV voltage higher than DTTV, and duration exceeds the Light signal close (day) delay time, controller would detect it as day time."),
    Register("Load controlling modes", LOAD_CONTROLLING_MODES, N, 1, "0000H Manual Control 0001H Light ON/OFF 0002H Light ON+ Timer/ 0003H Time Control"),
    Register("Working time length 1", WORKING_TIME_LENGTH_1, N, 1, "The length of load output timer1, D15-D8, hour, D7-D0, minute"),
    Register("Working time length 2", WORKING_TIME_LENGTH_2, N, 1, "The length of load output timer2, D15-D8, hour, D7-D0, minute"),
    Register("Turn on timing 1 ", TURN_ON_TIMING_1_SEC, SEC, N, 1, "Turn on/off timing of load output."),
    Register("Turn on timing 1 ", TURN_ON_TIMING_1_MIN, MIN, N, 1, "Turn on/off timing of load output."),
    Register("Turn on timing 1 ", TURN_ON_TIMING_1_HOUR, HOUR, N, 1, "Turn on/off timing of load output."),
    Register("Turn off timing 1 ", TURN_OFF_TIMING_1_SEC, SEC, N, 1, "Turn on/off timing of load output."),
    Register("Turn off timing 1 ", TURN_OFF_TIMING_1_MIN, MIN, N, 1, "Turn on/off timing of load output."),
    Register("Turn off timing 1 ", TURN_OFF_TIMING_1_HOUR, HOUR, N, 1, "Turn on/off timing of load output."),
    Register("Turn on timing 2", TURN_ON_TIMING_2_SEC, SEC, N, 1, "Turn on/off timing of load output."),
    Register("Turn on timing 2", TURN_ON_TIMING_2_MIN, MIN, N, 1, "Turn on/off timing of load output."),
    Register("Turn on timing 2", TURN_ON_TIMING_2_HOUR, HOUR, N, 1, "Turn on/off timing of load output."),
    Register("Turn off timing 2", TURN_OFF_TIMING_2_SEC, SEC, N, 1, "Turn on/off timing of load output."),
    Register("Turn off timing 2", TURN_OFF_TIMING_2_MIN, MIN, N, 1, "Turn on/off timing of load output."),
    Register("Turn off timing 2", TURN_OFF_TIMING_2_HOUR, HOUR, N, 1, "Turn on/off timing of load output."),
    Register("Backlight time ", BACKLIGHT_TIME, SEC, N, 1, "Close after LCD backlight light setting the number of seconds"),
    Register("Length of night", LENGTH_OF_NIGHT, N, 1, "Set default values of the whole night length of time. D15-D8, hour, D7-D0, minute"),
    Register("Device configure of main power supply", DVC_CONFIG_OF_MAIN_PWR_SUP, N, 1, "0001H Battery is main，0002H AC-DC power mainly"),
    Register("Battery rated voltage code", BATTERY_RATED_VOLTAGE_CODE, N, 1, "0-auto recognize, 1-12V, 2-24V, 3-36V，4-48V，5-60V，6-110V，7-120V，8-220V，9-240V"),
    Register("Default Load On/Off in manual mode", DEFAULT_LOAD_ON_OFF_IN_MANUAL_MODE, N, 1, "0-off, 1-on"),
    Register("Equalize duration", EQUALIZE_DURATION, MIN, 1, "Usually 0-120 minutes"),
    Register("Boost duration", BOOST_DURATION, MIN, 1, "Usually 10-120 minutes"),
    Register("Discharging percentage", DISCHARGING_PERCENTAGE, PC, 100, "Usually 20%-80%. The percentage of battery's remaining capacity when stop charging"),
    Register("Charging percentage", CHARGING_PERCENTAGE, PC, 100, "Depth of charge, 100%"),
    Register("Management modes of battery charging and discharging", BT_CH_DISCH_MNGMT_MODES, N, 1, "Management modes of battery charge and discharge, voltage compensation : 0 and SOC : 1")
]

Switch_Value = [
    Register("Charging device on/off", CHARGING_DEVICE_ON_OFF, N, 1, "1 Charging device on | 0 Charging device off"),
    Register("Output control mode manual/automatic", OUTPUT_CONTROL_MODE_MANUAL_AUTOMATIC, N, 1, "1 Output control mode manual | 0 Output control mode automatic"),
    Register("Manual control the load", MANUAL_CONTROL_THE_LOAD, N, 1, "When the load is manual mode，1-manual on | 0-manual off"),
    Register("Default control the load", DEFAULT_CONTROL_THE_LOAD, N, 1, "When the load is default mode，1-manual on | 0-manual off"),
    Register("Enable load test mode", ENABLE_LOAD_TEST_MODE, N, 1, "1 Enable | 0 Disable(normal)"),
    Register("Force the load on/off", FORCE_THE_LOAD_ON_OFF, N, 1, "1 Turn on | 0 Turn off (used for temporary test of the load）"),
    Register("Restore system defaults", RESTORE_SYSTEM_DEFAULTS, N, 1, "1 yes | 0 no"),
    Register("Clear generating electricity statistics", CLEAR_GENERATING_ELECTRICITY_STATISTICS, N, 1, "1 clear. Root privileges to perform")
]

Discrete_Value = [
    Register("Over temperature inside the device", OVER_TEMPERATURE_INSIDE_THE_DEVICE, N, 1, "1-The temperature inside the controller is higher than the over-temperature protection point. 0-Normal"),
    Register("Day/Night", DAY_NIGHT, N, 1, "1-Night, 0-Day")
]