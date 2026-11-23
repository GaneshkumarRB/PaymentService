package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.fraud.api.model.Reason;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * Maps wrapped {@code <Reasons><Reason>...</Reason></Reasons>} onto generated reasons list.
 */
public abstract class ScreeningResultMixin {

    @JacksonXmlElementWrapper(localName = "Reasons")
    @JacksonXmlProperty(localName = "Reason")
    abstract List<Reason> getReasons();

    @JacksonXmlElementWrapper(localName = "Reasons")
    @JacksonXmlProperty(localName = "Reason")
    abstract void setReasons(List<Reason> reasons);
}
