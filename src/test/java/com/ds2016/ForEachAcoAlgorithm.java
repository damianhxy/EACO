package com.ds2016;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Run the annotated test once for each ant colony algorithm (AntNet and
 * EACO), for behaviour involving ants or pheromone. The test method takes an
 * {@link Algorithm} parameter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest(name = "{0}")
@ExtendWith(NameAlgorithmOnFailure.class)
@EnumSource(value = Algorithm.class, names = {"ANTNET", "EACO"})
public @interface ForEachAcoAlgorithm {
}
