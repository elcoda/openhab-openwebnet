/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.ALL_SUPPORTED_THING_TYPES;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetAutomationHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetBridgeHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetLightingHandler;
import org.openhab.binding.openwebnet.handler.OpenWebNetThermoregulationHandler;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Massimo Valla - Initial contribution
 */
public class OpenWebNetHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ALL_SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("==OWN:HandlerFactory== createHandler()");
        if (OpenWebNetBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW BRIDGE Handler");
            OpenWebNetBridgeHandler handler = new OpenWebNetBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (OpenWebNetLightingHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW LIGHTING Handler");
            return new OpenWebNetLightingHandler(thing);
        } else if (OpenWebNetAutomationHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW AUTOMATION Handler");
            return new OpenWebNetAutomationHandler(thing);
        } else if (OpenWebNetThermoregulationHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            logger.debug("==OWN:HandlerFactory== creating NEW THERMO Handler");
            return new OpenWebNetThermoregulationHandler(thing);
        }
        return null;
    }

    private void registerDiscoveryService(OpenWebNetBridgeHandler bridgeHandler) {
        logger.debug("==OWN:HandlerFactory== registerDiscoveryService()");
        OpenWebNetDeviceDiscoveryService deviceDiscoveryService = new OpenWebNetDeviceDiscoveryService(bridgeHandler);
        bridgeHandler.deviceDiscoveryService = deviceDiscoveryService;
        deviceDiscoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext.registerService(
                DiscoveryService.class.getName(), deviceDiscoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof OpenWebNetBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                OpenWebNetDeviceDiscoveryService service = (OpenWebNetDeviceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }
}
