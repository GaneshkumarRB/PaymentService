package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.fraud.api.model.Reason;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Jackson mix-in mapping wrapped Reason elements onto the generated reasons list.
 */
public abstract class ScreeningResultXmlMapping {

    @JacksonXmlElementWrapper(localName = "Reasons")
    @JacksonXmlProperty(localName = "Reason")
    abstract List<Reason> getReasons();

    @JacksonXmlElementWrapper(localName = "Reasons")
    @JacksonXmlProperty(localName = "Reason")
    abstract void setReasons(List<Reason> reasons);
}