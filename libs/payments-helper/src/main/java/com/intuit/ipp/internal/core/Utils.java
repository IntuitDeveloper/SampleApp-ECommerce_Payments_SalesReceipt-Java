package com.intuit.ipp.internal.core;

import com.intuit.ipp.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	public static String getAPIBaseURL() {
		return Config.getProperty("baseURL.quickbooks-api");

	}

    public static String getPaymentAPIBaseURL() {
        return Config.getProperty("baseURL.quickbooks-api");

    }


    /**pass a class name without qualification*/
    public static Logger getPrefixedLogger(String classNameWithoutPackageName){
        return LoggerFactory.getLogger("com.intuit.logger." + classNameWithoutPackageName);
    }

}
