/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link OpenWebNetBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Massimo Valla - Initial contribution
 */

public class OpenWebNetBindingConstants {

    private static final String BINDING_ID = "openwebnet";

    public static final int THING_STATE_REQ_TIMEOUT = 5; // seconds

    // #LIST OF Thing Type UIDs
    // bridges
    public static final ThingTypeUID THING_TYPE_DONGLE = new ThingTypeUID(BINDING_ID, "dongle");
    public static final String THING_LABEL_DONGLE = "ZigBee USB Dongle";
    public static final ThingTypeUID THING_TYPE_BUS_GATEWAY = new ThingTypeUID(BINDING_ID, "bus_gateway");
    public static final String THING_LABEL_BUS_GATEWAY = "BUS Gateway";
    // generic (unknown) device
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final String THING_LABEL_DEVICE = "GENERIC Device";
    // other thing types
    // BUS
    public static final ThingTypeUID THING_TYPE_BUS_ON_OFF_SWITCH = new ThingTypeUID(BINDING_ID, "bus_on_off_switch");
    public static final String THING_LABEL_BUS_ON_OFF_SWITCH = "BUS Switch";
    public static final ThingTypeUID THING_TYPE_BUS_DIMMER = new ThingTypeUID(BINDING_ID, "bus_dimmer");
    public static final String THING_LABEL_BUS_DIMMER = "BUS Dimmer";
    public static final ThingTypeUID THING_TYPE_BUS_AUTOMATION = new ThingTypeUID(BINDING_ID, "bus_automation");
    public static final String THING_LABEL_BUS_AUTOMATION = "BUS Automation";
    public static final ThingTypeUID THING_TYPE_BUS_TEMP_SENSOR = new ThingTypeUID(BINDING_ID, "bus_temp_sensor");
    public static final String THING_LABEL_BUS_TEMP_SENSOR = "BUS Temperature Sensor";
    public static final ThingTypeUID THING_TYPE_BUS_THERMOSTAT = new ThingTypeUID(BINDING_ID, "bus_thermostat");
    public static final String THING_LABEL_BUS_THERMOSTAT = "BUS Thermostat";
    public static final ThingTypeUID THING_TYPE_BUS_THERMO_CENTRAL_UNIT = new ThingTypeUID(BINDING_ID,
            "bus_thermo_central_unit");
    public static final String THING_LABEL_BUS_THERMO_CENTRAL_UNIT = "BUS Thermo Central Unit";
    // ZIGBEE
    public static final ThingTypeUID THING_TYPE_ON_OFF_SWITCH = new ThingTypeUID(BINDING_ID, "on_off_switch");
    public static final String THING_LABEL_ON_OFF_SWITCH = "ZigBee Switch";
    public static final ThingTypeUID THING_TYPE_ON_OFF_SWITCH_2UNITS = new ThingTypeUID(BINDING_ID, "on_off_switch2u");
    public static final String THING_LABEL_ON_OFF_SWITCH_2UNITS = "ZigBee 2-units Switch";
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final String THING_LABEL_DIMMER = "ZigBee Dimmer";
    public static final ThingTypeUID THING_TYPE_AUTOMATION = new ThingTypeUID(BINDING_ID, "automation");
    public static final String THING_LABEL_AUTOMATION = "ZigBee Automation";
    // public static final ThingTypeUID THING_TYPE_TEMP_SENSOR = new ThingTypeUID(BINDING_ID, "tempsensor");
    // public static final String THING_LABEL_TEMP_SENSOR = "Temperature Sensor";
    // public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    // public static final String THING_LABEL_THERMOSTAT = "Thermostat";
    // TODO transform these constants in enum+hashmaps

    // #SUPPORTED THINGS SETS
    // ## Lighting
    public static final Set<ThingTypeUID> LIGHTING_SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_ON_OFF_SWITCH,
            THING_TYPE_ON_OFF_SWITCH_2UNITS, THING_TYPE_DIMMER, THING_TYPE_BUS_ON_OFF_SWITCH, THING_TYPE_BUS_DIMMER);

    // ## Automation
    public static final Set<ThingTypeUID> AUTOMATION_SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_AUTOMATION,
            THING_TYPE_BUS_AUTOMATION);

    // ## Thermoregulation
    public static final Set<ThingTypeUID> THERMOREGULATION_SUPPORTED_THING_TYPES = Sets
            .newHashSet(THING_TYPE_BUS_TEMP_SENSOR, THING_TYPE_BUS_THERMOSTAT, THING_TYPE_BUS_THERMO_CENTRAL_UNIT);

    // ## Groups
    public static final Set<ThingTypeUID> DEVICE_SUPPORTED_THING_TYPES = Sets.union(
            Sets.union(LIGHTING_SUPPORTED_THING_TYPES,
                    Sets.union(AUTOMATION_SUPPORTED_THING_TYPES, THERMOREGULATION_SUPPORTED_THING_TYPES)),
            Sets.newHashSet(THING_TYPE_DEVICE));

    public static final Set<ThingTypeUID> BRIDGE_SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_DONGLE,
            THING_TYPE_BUS_GATEWAY);

    public static final Set<ThingTypeUID> ALL_SUPPORTED_THING_TYPES = Sets.union(DEVICE_SUPPORTED_THING_TYPES,
            BRIDGE_SUPPORTED_THING_TYPES);

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SWITCH_01 = "switch_01";
    public static final String CHANNEL_SWITCH_02 = "switch_02";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_SHUTTER = "shutter";
    // thermo
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMP_TARGET = "targetTemperature";
    public static final String CHANNEL_THERMO_FUNCTION = "thermoFunction";
    public static final String CHANNEL_HEATING_COOLING_MODE = "heatingCoolingMode";
    public static final String CHANNEL_HEATING = "heating";
    public static final String CHANNEL_COOLING = "cooling";
    public static final String CHANNEL_ACTIVE_MODE = "activeMode";
    public static final String CHANNEL_LOCAL_MODE = "localMode";
    public static final String CHANNEL_TEMP_SETPOINT = "setpointTemperature";
    public static final String CHANNEL_SET_MODE = "setMode";

    public static final String CHANNEL_ALL_TEMP_SETPOINT = "allSetpointTemperature";
    public static final String CHANNEL_ALL_SET_MODE = "allSetMode";
    public static final String CHANNEL_ALL_THERMO_FUNCTION = "allThermoFunction";

    // config properties
    public static final String CONFIG_PROPERTY_SHUTTER_RUN = "shutterRun";

    public static final String CONFIG_PROPERTY_SERIAL_PORT = "serialPort";

    public static final String CONFIG_PROPERTY_WHERE = "where";
    public static final String CONFIG_PROPERTY_HOST = "host";
    public static final String CONFIG_PROPERTY_PORT = "port";
    public static final String CONFIG_PROPERTY_PASSWD = "passwd";

    public static final String CONFIG_PROPERTY_FIRMWARE = "firmwareVersion";
    public static final String CONFIG_PROPERTY_MODEL = "model";

}
