
package com.kemar.olam.delivery_management.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Customer {

    @SerializedName("optionValue")
    @Expose
    private Integer optionValue;
    @SerializedName("optionName")
    @Expose
    private String optionName;
    @SerializedName("optionValueString")
    @Expose
    private String optionValueString;
    @SerializedName("optio 2021-05-16 20:23:29.479 18053-19339/com.kemar.olam D/OkHttp: nValue")
    @Expose
    private Integer optio202105162023294791805319339ComKemarOlamDOkHttpNValue;

    public Integer getOptionValue() {
        return optionValue;
    }

    public void setOptionValue(Integer optionValue) {
        this.optionValue = optionValue;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getOptionValueString() {
        return optionValueString;
    }

    public void setOptionValueString(String optionValueString) {
        this.optionValueString = optionValueString;
    }

    public Integer getOptio202105162023294791805319339ComKemarOlamDOkHttpNValue() {
        return optio202105162023294791805319339ComKemarOlamDOkHttpNValue;
    }

    public void setOptio202105162023294791805319339ComKemarOlamDOkHttpNValue(Integer optio202105162023294791805319339ComKemarOlamDOkHttpNValue) {
        this.optio202105162023294791805319339ComKemarOlamDOkHttpNValue = optio202105162023294791805319339ComKemarOlamDOkHttpNValue;
    }

}
