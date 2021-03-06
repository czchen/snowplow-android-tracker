/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.snowplowanalytics.snowplow.tracker.android;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class Util extends com.snowplowanalytics.snowplow.tracker.core.Util {

    public static String getAdvertisingID(Context context) {
        return getPlayAdId(context);
    }

    public static String getCarrier(Context context) {
        String carrierName = "";
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            carrierName = telephonyManager.getNetworkOperatorName();
        }
        return carrierName;
    }

    public static Location getLocation(Context context) {
        Location location = null;
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            try {
                location = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException ex) {
                Log.d(Util.class.toString(), "No permission to retrieve location.");
                location = null;
            }
        }
        return location;
    }

    public static String getPlayAdId(Context context) {
        try {
            Object AdvertisingInfoObject = getAdvertisingInfoObject(context);

            String playAdid = (String) invokeInstanceMethod(AdvertisingInfoObject, "getId", null);

            return playAdid;
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static Object getAdvertisingInfoObject(Context context)
            throws Exception {
        return invokeStaticMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient",
                "getAdvertisingIdInfo",
                new Class[] {Context.class} , context
        );
    }

    private static Object invokeStaticMethod(String className, String methodName,
                                             Class[] cArgs, Object... args)
            throws Exception {
        Class classObject = Class.forName(className);

        return invokeMethod(classObject, methodName, null, cArgs, args);
    }

    private static Object invokeInstanceMethod(Object instance, String methodName,
                                               Class[] cArgs, Object... args)
            throws Exception {
        Class classObject = instance.getClass();

        return invokeMethod(classObject, methodName, instance, cArgs, args);
    }

    private static Object invokeMethod(Class classObject, String methodName, Object instance,
                                       Class[] cArgs, Object... args)
            throws Exception {
        Method methodObject = classObject.getMethod(methodName, cArgs);

        Object resultObject = methodObject.invoke(instance, args);

        return resultObject;
    }


}
