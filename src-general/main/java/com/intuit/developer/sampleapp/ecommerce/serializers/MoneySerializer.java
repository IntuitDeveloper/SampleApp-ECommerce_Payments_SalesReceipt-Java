package com.intuit.developer.sampleapp.ecommerce.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.joda.money.Money;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/25/14
 * Time: 8:55 AM
 */
public class MoneySerializer extends com.fasterxml.jackson.databind.JsonSerializer<Object> {
    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        Money money = (Money) o;
        jsonGenerator.writeNumber(money.getAmount());
    }
}
