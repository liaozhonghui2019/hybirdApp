/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.egrand.web.qrcode.camera

import android.os.IBinder
import android.util.Log

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * This class is used to activate the weak light on some camera phones (not flash)
 * in order to illuminate surfaces for scanning. There is no official way to do this,
 * but, classes which allow access to this function still exist on some devices.
 * This therefore proceeds through a great deal of reflection.
 *
 *
 * See [
 * http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/](http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/) and
 * [
 * http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java](http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java).
 * Thanks to Ryan Alford for pointing out the availability of this class.
 */
internal object FlashlightManager {

    private val TAG = FlashlightManager::class.java.simpleName

    private val iHardwareService: Any?
    private val setFlashEnabledMethod: Method?

    private val hardwareService: Any?
        get() {
            val serviceManagerClass = maybeForName("android.os.ServiceManager") ?: return null

            val getServiceMethod = maybeGetMethod(serviceManagerClass, "getService", String::class.java)
                ?: return null

            val hardwareService = invoke(getServiceMethod, null, "hardware") ?: return null

            val iHardwareServiceStubClass = maybeForName("android.os.IHardwareService\$Stub") ?: return null

            val asInterfaceMethod = maybeGetMethod(iHardwareServiceStubClass, "asInterface", IBinder::class.java)
                ?: return null

            return invoke(asInterfaceMethod, null, hardwareService)
        }

    init {
        iHardwareService = hardwareService
        setFlashEnabledMethod = getSetFlashEnabledMethod(iHardwareService)
        if (iHardwareService == null) {
            Log.v(TAG, "This device does supports control of a flashlight")
        } else {
            Log.v(TAG, "This device does not support control of a flashlight")
        }
    }

    private fun getSetFlashEnabledMethod(iHardwareService: Any?): Method? {
        if (iHardwareService == null) {
            return null
        }
        val proxyClass = iHardwareService.javaClass
        return maybeGetMethod(proxyClass, "setFlashlightEnabled", Boolean::class.java)
    }

    private fun maybeForName(name: String): Class<*>? {
        try {
            return Class.forName(name)
        } catch (cnfe: ClassNotFoundException) {
            // OK
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while finding class $name", re)
            return null
        }

    }

    private fun maybeGetMethod(clazz: Class<*>, name: String, vararg argClasses: Class<*>): Method? {
        try {
            return clazz.getMethod(name, *argClasses)
        } catch (nsme: NoSuchMethodException) {
            // OK
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while finding method $name", re)
            return null
        }

    }

    private operator fun invoke(method: Method?, instance: Any?, vararg args: Any): Any? {
        try {
            return method!!.invoke(instance, *args)
        } catch (e: IllegalAccessException) {
            Log.w(TAG, "Unexpected error while invoking " + method!!, e)
            return null
        } catch (e: InvocationTargetException) {
            Log.w(TAG, "Unexpected error while invoking " + method!!, e.cause)
            return null
        } catch (re: RuntimeException) {
            Log.w(TAG, "Unexpected error while invoking " + method!!, re)
            return null
        }

    }

    fun enableFlashlight() {
        setFlashlight(true)
    }

    fun disableFlashlight() {
        setFlashlight(false)
    }

    private fun setFlashlight(active: Boolean) {
        if (iHardwareService != null) {
            invoke(setFlashEnabledMethod, iHardwareService, active)
        }
    }

}
