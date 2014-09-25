package com.intuit.ipp.internal.core;

import com.intuit.ipp.exception.SerializationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.slf4j.Logger;

public class JsonUtil {
    private static final Logger logger = Utils.getPrefixedLogger(JsonUtil.class.getSimpleName());
    public static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
        DateFormat dateFormat = new SimpleDateFormat(Constants.DATETIMEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Constants.TIMEZONE_UTC));
        mapper.setDateFormat(dateFormat);
        mapper.disable(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
   }

    public static String serialize(Object obj) throws SerializationException {
        try {
            if (obj != null)
                return mapper.writeValueAsString(obj);
            else
                return null;
        } catch (Exception e) {
            throw new SerializationException(e.getMessage());
        }
    }

    public static Object deserialize(String json, TypeReference<?> typeReference) throws SerializationException {
        try {
            logger.debug("Json string to deserialize {} ", json);
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            SerializationException serializationException = new SerializationException(e);
            throw serializationException;
        }
    }



 }

