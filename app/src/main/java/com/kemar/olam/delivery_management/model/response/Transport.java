
package com.kemar.olam.delivery_management.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transport {

    @SerializedName("optionValue")
    @Expose
    private Integer optionValue;
    @SerializedName("optionName")
    @Expose
    private String optionName;
    @SerializedName("optionValueString")
    @Expose
    private String optionValueString;

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

}
