package com.bnk.paymentservice.fraud;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Jackson mix-in mapping the Reason XML attribute and text onto the generated model.
 */
public abstract class ReasonXmlMapping {

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    abstract String getCode();

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    abstract void setCode(String code);

    @JacksonXmlText
    abstract String getValue();

    @JacksonXmlText
    abstract void setValue(String value);
}