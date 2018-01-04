package com.intuit.ipp.data.payment;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.math.BigDecimal;
import com.intuit.ipp.data.payment.DeviceInfo;
public class PaymentContext {
  
    private BigDecimal tax = null;
    private DeviceInfo deviceInfo = null;
    private Boolean recurring = null;
    private String mobile = null;
    private String isEcommerce = null;
    private String ecommerce = null;
    /**
     * Sales Tax - required for commercial card processing
     *
     * @return Sales Tax - required for commercial card processing
     */
    public BigDecimal getTax() {
        return tax;
    }

    /**
     * Sales Tax - required for commercial card processing
     *
     * @param tax Sales Tax - required for commercial card processing
     */
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    /**
     * This boolean value should be set to true if the charge is recurring. This value is not applicable for capture or refund request and it won't be used.
     *
     * @return This boolean value should be set to true if the charge is recurring. This value is not applicable for capture or refund request and it won't be used.
     */
    public Boolean getRecurring() {
        return recurring;
    }

    /**
     * This boolean value should be set to true if the charge is recurring. This value is not applicable for capture or refund request and it won't be used.
     *
     * @param recurring This boolean value should be set to true if the charge is recurring. This value is not applicable for capture or refund request and it won't be used.
     */
    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getIsEcommerce() {
		return isEcommerce;
	}

	public void setIsEcommerce(String isEcommerce) {
		this.isEcommerce = isEcommerce;
	}
	
	public String getEcommerce() {
		return ecommerce;
	}

	public void setEcommerce(String ecommerce) {
		this.ecommerce = ecommerce;
	}

	@Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

