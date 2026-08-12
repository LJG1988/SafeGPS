package com.secure.gps;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class GPSInjector {
    
    public static boolean injectLocation(LocationManager lm, double lat, double lon) {
        try {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(lat);
            location.setLongitude(lon);
            location.setAccuracy(3.0f);
            location.setTime(System.currentTimeMillis());
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            location.setSpeed(0.5f);
            location.setBearing((float) (Math.random() * 360));
            clearMockFlag(location);
            return setTestProviderLocation(lm, location);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static void clearMockFlag(Location location) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Method setMock = Location.class.getDeclaredMethod("setMock", boolean.class);
                setMock.setAccessible(true);
                setMock.invoke(location, false);
            } else {
                Method setIsMock = Location.class.getDeclaredMethod("setIsFromMockProvider", boolean.class);
                setIsMock.setAccessible(true);
                setIsMock.invoke(location, false);
            }
        } catch (Exception e) {
            try {
                Field field = Location.class.getDeclaredField("mIsFromMockProvider");
                field.setAccessible(true);
                field.setBoolean(location, false);
            } catch (Exception ignored) {}
        }
    }
    
    private static boolean setTestProviderLocation(LocationManager lm, Location location) {
        try {
            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
            return true;
        } catch (SecurityException e) {
            try {
                Method method = LocationManager.class.getDeclaredMethod(
                    "setTestProviderLocation", String.class, Location.class);
                method.setAccessible(true);
                method.invoke(lm, LocationManager.GPS_PROVIDER, location);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }
    
    public static void initTestProvider(LocationManager lm) {
        try {
            if (lm.getProvider(LocationManager.GPS_PROVIDER) != null) return;
            lm.addTestProvider(LocationManager.GPS_PROVIDER,
                false, false, false, false, true, true, true,
                android.location.Criteria.POWER_HIGH,
                android.location.Criteria.ACCURACY_FINE);
            lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static double[] addWander(double lat, double lon) {
        double latChange = (Math.random() - 0.5) * 0.00003;
        double lonChange = (Math.random() - 0.5) * 0.00003;
        return new double[]{lat + latChange, lon + lonChange};
    }
}