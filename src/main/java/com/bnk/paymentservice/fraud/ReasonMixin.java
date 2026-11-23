package com.bnk.paymentservice.fraud;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

/**
 * Maps {@code <Reason code="...">text</Reason>} attribute + element text onto generated Reason.
 */
public abstract class ReasonMixin {

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    abstract String getCode();

    @JacksonXmlProperty(isAttribute = true, localName = "code")
    abstract void setCode(String code);

    @JacksonXmlText
    abstract String getValue();

    @JacksonXmlText
    abstract void setValue(String value);
}
