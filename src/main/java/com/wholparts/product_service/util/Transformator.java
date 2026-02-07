package com.wholparts.product_service.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Transformator {

    public <I, O> O transform(I input, Class<O> outputClass) {
        try {
            O output = outputClass.getDeclaredConstructor().newInstance();

            Field[] sourceFields = input.getClass().getDeclaredFields();
            Field[] targetFields = outputClass.getDeclaredFields();

            Arrays.stream(sourceFields).forEach(sourceField -> {
                sourceField.setAccessible(true);
                Arrays.stream(targetFields)
                        .filter(targetField -> isMatching(sourceField, targetField))
                        .forEach(targetField -> {
                            targetField.setAccessible(true);
                            try {
                                targetField.set(output, sourceField.get(input));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
            });

            return output;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Erro na transformação DTO <-> Entity", e);
        }
    }

    private boolean isMatching(Field sourceField, Field targetField) {
        return sourceField.getName().equals(targetField.getName()) &&
                sourceField.getType().equals(targetField.getType());
    }
}
