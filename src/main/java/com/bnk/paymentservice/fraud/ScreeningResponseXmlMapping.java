package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.fraud.api.model.Error;
import com.bnk.paymentservice.fraud.api.model.ScreeningHeader;
import com.bnk.paymentservice.fraud.api.model.ScreeningResult;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Jackson mix-in mapping nested Header, Result and Error elements onto the generated model.
 */
public abstract class ScreeningResponseXmlMapping {

    @JacksonXmlProperty(localName = "Header")
    abstract ScreeningHeader getHeader();

    @JacksonXmlProperty(localName = "Header")
    abstract void setHeader(ScreeningHeader header);

    @JacksonXmlProperty(localName = "Result")
    abstract ScreeningResult getResult();

    @JacksonXmlProperty(localName = "Result")
    abstract void setResult(ScreeningResult result);

    @JacksonXmlProperty(localName = "Error")
    abstract Error getError();

    @JacksonXmlProperty(localName = "Error")
    abstract void setError(Error error);
}