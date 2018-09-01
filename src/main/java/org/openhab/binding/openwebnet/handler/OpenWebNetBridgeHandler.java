/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.handler;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.openwebnet.OpenError;
import org.openwebnet.OpenGateway;
import org.openwebnet.OpenGatewayBus;
import org.openwebnet.OpenGatewayZigBee;
import org.openwebnet.OpenListener;
import org.openwebnet.OpenNewDeviceListener;
import org.openwebnet.OpenWebNet;
import org.openwebnet.message.Automation;
import org.openwebnet.message.BaseOpenMessage;
import org.openwebnet.message.GatewayManagement;
import org.openwebnet.message.Lighting;
import org.openwebnet.message.OpenMessage;
import org.openwebnet.message.OpenMessageFactory;
import org.openwebnet.message.Thermoregulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetBridgeHandler} is responsible for handling communication with gateways and handling events.
 *
 * @author Massimo Valla - Initial contribution
 */
public class OpenWebNetBridgeHandler extends ConfigStatusBridgeHandler implements OpenListener {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeHandler.class);

    private static final int GATEWAY_ONLINE_TIMEOUT = 20; // (sec) Time to wait for the gateway to become connected
    private static final int CONFIG_GATEWAY_DEFAULT_PORT = 20000;
    private static final String CONFIG_GATEWAY_DEFAULT_PASSWD = "12345";
    // private static final String CONFIG_GATEWAY_DEFAULT_HOST = "127.0.0.1";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.BRIDGE_SUPPORTED_THING_TYPES;

    private Map<String, ThingUID> registeredDevices = new ConcurrentHashMap<>();

    protected OpenGateway gateway;
    private boolean isBusGateway = false;

    private boolean isGatewayConnected = false;

    public OpenWebNetDeviceDiscoveryService deviceDiscoveryService;

    public OpenWebNetBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public OpenGateway getGateway() {
        return gateway;
    }

    public boolean isBusGateway() {
        return isBusGateway;
    }

    @Override
    public void initialize() {
        logger.debug("==OWN== BridgeHandler.initialize() ");
        ThingTypeUID thingType = getThing().getThingTypeUID();
        logger.debug("==OWN== type = {}", thingType);

        if (thingType.equals(THING_TYPE_DONGLE)) {
            initZigBeeGateway();
        } else {
            initBusGateway();
            isBusGateway = true;
        }
        gateway.subscribe(this);
        if (gateway.isConnected()) { // gateway is already connected, device can go ONLINE
            isGatewayConnected = true;
            logger.info("==OWN== ------------------- ALREADY CONNECTED -> setting status to ONLINE");
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            logger.debug("==OWN== Trying to connect gateway...");
            gateway.connect();
            scheduler.schedule(() -> {
                // if state is still UNKNOWN after timer ends, set the device as OFFLINE
                if (thing.getStatus().equals(ThingStatus.UNKNOWN)) {
                    logger.info("==OWN== BridgeHandler status still UNKNOWN. Setting device={} to OFFLINE",
                            thing.getUID());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                }
            }, GATEWAY_ONLINE_TIMEOUT, TimeUnit.SECONDS);
        }

        // TODO
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /**
     * Init a ZigBee gateway based on config properties
     *
     */
    private void initZigBeeGateway() {
        String serialPort = (String) (getConfig().get(CONFIG_PROPERTY_SERIAL_PORT));
        if (serialPort == null) {
            logger.warn(
                    "==OWN== BridgeHandler ZigBee gateway port config is <null>, will try to find a gateway on serial ports");
            gateway = OpenWebNet.gatewayZigBeeAsSingleton(); // TODO do not use singleton
        } else {
            // TODO connect to serial port specified using config params
            gateway = OpenWebNet.gatewayZigBeeAsSingleton();
        }
    }

    /**
     * Init a BUS/SCS gateway based on config properties
     *
     */
    private void initBusGateway() {
        if (getConfig().get(CONFIG_PROPERTY_HOST) != null) {
            String host = (String) (getConfig().get(CONFIG_PROPERTY_HOST));
            int port = CONFIG_GATEWAY_DEFAULT_PORT;
            Object portConfig = getConfig().get(CONFIG_PROPERTY_PORT);
            if (portConfig != null) {
                port = ((BigDecimal) portConfig).intValue();
            }
            String passwd = (String) (getConfig().get(CONFIG_PROPERTY_PASSWD));
            if (passwd == null) {
                passwd = CONFIG_GATEWAY_DEFAULT_PASSWD;
            }
            String passwdMasked;
            if (passwd.length() >= 4) {
                passwdMasked = "******" + passwd.substring(passwd.length() - 3, passwd.length() - 1);
            } else {
                passwdMasked = "******";
            }
            logger.debug("==OWN== Creating new BUS gateway with config properties: {}:{}, pwd={}", host, port,
                    passwdMasked);
            gateway = OpenWebNet.gatewayBus(host, port, passwd);
        } else {
            logger.warn(
                    "==OWN== BridgeHandler Cannot connect to gateway. No IP/host has been provided in Bridge configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("==OWN== BridgeHandler handleCommand (command={} - channel={})", command, channelUID);
        if (!gateway.isConnected()) {
            logger.warn("==OWN== BridgeHandler gateway is NOT connected, skipping command");
            return;
        } else {
            logger.warn("==OWN== BridgeHandler Channel not supported: channel={}", channelUID);
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        logger.debug("==OWN== BridgeHandler.getConfigStatus() ");
        Collection<ConfigStatusMessage> configStatusMessages;

        configStatusMessages = Collections.emptyList();

        return configStatusMessages;
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        logger.info("==OWN== Bridge configuration updated.");
        // for (Thing t : getThing().getThings()) {
        // final ThingHandler thingHandler = t.getHandler();
        // if (thingHandler != null) {
        // thingHandler.thingUpdated(t);
        // }
        // }
    }

    @Override
    public void handleRemoval() {
        logger.debug("==OWN== BridgeHandler.handleRemoval() ");
        if (gateway != null) {
            gateway.closeConnection();
            gateway.unsubscribe(this);
            logger.debug("==OWN== Connection closed and unsubscribed.");
        }
        logger.debug("==OWN== now calling super.handleRemoval()");
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        logger.debug("==OWN== BridgeHandler.dispose() ");
        if (gateway != null) {
            gateway.closeConnection();
            gateway.unsubscribe(this);
            logger.debug("==OWN== Connection closed and unsubscribed.");
        }
        logger.debug("==OWN== now calling super.dispose()");
        super.dispose();
    }

    /**
     * Search for devices connected to this bridge handler's gateway
     *
     * @param listener to receive device found notifications
     */
    public void searchDevices(OpenNewDeviceListener listener) {
        logger.debug("==OWN==  BridgeHandler.searchDevices()");
        if (!gateway.isConnected()) {
            logger.warn("==OWN==  gateway is NOT connected, cannot search for devices!");
            return;
        }
        gateway.discoverDevices(listener);
    }

    /**
     * Register a device thing to this bridge handler based on its id
     *
     * @param ownId    device OpenWebNet id
     * @param thingUID ThingUID
     */
    protected void registerDevice(String ownId, ThingUID thingUID) {
        logger.debug("==OWN==  BridgeHandler.registerDevice() ");
        if (thingUID == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null 'ThingUID'.");
        }
        if (ownId == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null 'ownId'.");
        }
        registeredDevices.put(ownId, thingUID);
    }

    /**
     * Un-register a device from this bridge handler
     *
     * @param ownId device OpenWebNet id
     */
    protected void unregisterDevice(String ownId) {
        logger.debug("==OWN==  BridgeHandler.UNregisterDevice() ");
        if (ownId == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null 'ownId'.");
        }
        registeredDevices.remove(ownId);
    }

    @Override
    public void onMessage(OpenMessage msg) {
        logger.trace("==OWN==  RECEIVED <<<<< {}", msg);
        // TODO provide direct methods msg.isACK() and msg.isNACK()
        if (OpenMessage.ACK.equals(msg.getValue()) || OpenMessage.NACK.equals(msg.getValue())) {
            return;// ignore
        }
        // GATEWAY MANAGEMENT
        if (msg instanceof GatewayManagement) {
            GatewayManagement gwMgmtMsg = (GatewayManagement) msg;
            logger.debug("==OWN==  GatewayManagement WHAT = {}", gwMgmtMsg.getWhat());
            return;
        }
        BaseOpenMessage baseMsg = (BaseOpenMessage) msg;
        // let's try to get the thing associated with this message...
        if (baseMsg instanceof Lighting || baseMsg instanceof Automation || baseMsg instanceof Thermoregulation) {
            String ownId = ownIdFromWhere(baseMsg.getWhere());
            logger.trace("==OWN==  ownId = {}", ownId);
            ThingUID thingUID = registeredDevices.get(ownId);
            Thing device = getThingByUID(thingUID);
            if (device == null) {
                logger.debug("==OWN==  ownId={} has NO THING associated, ignoring it", ownId);
            } else {
                OpenWebNetThingHandler deviceHandler = (OpenWebNetThingHandler) device.getHandler();
                if (deviceHandler != null) {
                    deviceHandler.handleMessage(baseMsg);
                } else {
                    logger.debug("==OWN==  ownId={} has NO HANDLER associated, ignoring it", ownId);
                }
            }
        } else { // WHO not supported by the binding
            logger.debug(
                    "==OWN==  BridgeHandler ignoring frame {}. This message type (WHO={}) is not supported by the binding",
                    baseMsg, baseMsg.getWho());
        }

    }

    @Override
    public void onConnected() {
        isGatewayConnected = true;
        if (gateway instanceof OpenGatewayZigBee) {
            logger.info("==OWN== ------------------- CONNECTED to ZigBee gateway - USB port: {}",
                    ((OpenGatewayZigBee) gateway).getConnectedPort());
        } else {
            logger.info("==OWN== ------------------- CONNECTED to BUS gateway - {}:{}",
                    ((OpenGatewayBus) gateway).getHost(), ((OpenGatewayBus) gateway).getPort());
            // update gw model
            updateProperty(CONFIG_PROPERTY_MODEL, ((OpenGatewayBus) gateway).getModelName());
            /*
             * Map<String, String> properties = editProperties();
             * properties.put(CONFIG_PROPERTY_MODEL, ((OpenGatewayBus) gateway).getModelName());
             * updateProperties(properties);
             */
        }
        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void onConnectionError(OpenError error) {
        // logger.debug("==OWN== onConnectionError()");
        String cause;
        switch (error) {
            case DISCONNECTED:
            case LIB_LINKAGE_ERROR:
                cause = "Please check that the ZigBee dongle is correctly plugged-in, and the driver installed and loaded";
                break;
            case NO_SERIAL_PORTS_ERROR:
                cause = "No serial ports found";
                break;
            case JVM_ERROR:
                cause = "Make sure you have a working Java Runtime Environment installed in 32 bit version";
                break;
            case IO_EXCEPTION_ERROR:
                cause = "Connection error (IOException). Check network and gateway thing Configuration Parameters";
                break;
            case OTHER_ERROR:
            default:
                cause = "==ERROR NOT RECOGNIZED==";
                break;
        }
        logger.warn("==OWN==  CONNECTION ERROR: {}", cause);
        // if (isGatewayConnected) {
        isGatewayConnected = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, cause);
        // logger.debug("==OWN== Bridge status set to OFFLINE");
        // }
    }

    @Override
    public void onConnectionClosed() {
        isGatewayConnected = false;
        logger.debug("==OWN==  onConnectionClosed() - isGatewayConnected={}", isGatewayConnected);
        // cannot change to OFFLINE here because we are already in REMOVING state
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "The CONNECTION to the gateway HAS BEEN CLOSED");
    }

    @Override
    public void onDisconnected() {
        isGatewayConnected = false;
        logger.warn("==OWN== ---------- DISCONNECTED from the gateway");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                "The gateway HAS BEEN DISCONNECTED");
        logger.debug("==OWN==  Bridge status set to OFFLINE");
        // TODO start here a re-connect cycle??
    }

    @Override
    public void onReconnected() {
        logger.info("==OWN== ------------------- RE-CONNECTED to gateway!");
        updateStatus(ThingStatus.ONLINE);
        logger.debug("==OWN== Bridge status set to ONLINE");
        // TODO refresh devices' status?

    }

    /**
     * Transform a WHERE string address into a ownId (id for thing) based on bridge type (BUS/ZigBee)
     *
     * @param where OWN WHERE string address
     * @return ownId
     */
    public String ownIdFromWhere(String where) {
        String str = "";
        if (isBusGateway) {
            if (where.indexOf('#') < 0) { // no hash present
                str = where;
            } else if (where.indexOf("#4#") > 0) { // local bus: APL#4#bus
                str = where;
            } else if (where.indexOf('#') == 0) { // thermo zone via central unit: #0 or #Z (Z=[1-99]) --> Z
                str = where.substring(1);
            } else if (where.indexOf('#') > 0) { // thermo zone and actuator N: Z#N (Z=[1-99], N=[1-9]) -- > Z
                str = where.substring(0, where.indexOf('#'));
            } else {
                logger.warn("==OWN== ownIdFromWhere() unexpected WHERE: {}", where);
                str = where;
            }
            return str;
        } else {
            return OpenMessageFactory.getAddrFromWhere(where);
        }
    }

}
