package com.intuit.developer.sampleapp.ecommerce.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/25/14
 * Time: 8:55 AM
 */
public class MoneyDeserializer extends JsonDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final String jsonStr = jsonParser.getText();
        return Money.of(CurrencyUnit.USD, new BigDecimal(jsonStr));
    }
}
