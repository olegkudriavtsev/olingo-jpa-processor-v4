package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

public class IntermediateOperationHelper {

  private IntermediateOperationHelper() {
// Must not create instances
  }

  static Constructor<?> determineConstructor(Method javaFunction) throws ODataJPAModelException {
    Constructor<?> result = null;
    Constructor<?>[] constructors = javaFunction.getDeclaringClass().getConstructors();
    for (Constructor<?> constructor : Arrays.asList(constructors)) {
      Parameter[] parameters = constructor.getParameters();
      if (parameters.length == 0)
        result = constructor;
      else if (parameters.length == 1 && parameters[0].getType() == EntityManager.class) {
        result = constructor;
        break;
      }
    }
    if (result == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_CONSTRUCTOR_MISSING, javaFunction
          .getClass().getName());
    return result;
  }

  static boolean isCollection(Class<?> declairedReturnType) {
    for (Class<?> inter : Arrays.asList(declairedReturnType.getInterfaces())) {
      if (inter == Collection.class)
        return true;
    }
    return false;
  }

  static FullQualifiedName determineReturnType(final ReturnType definedReturnType, final Class<?> declairedReturnType,
      final IntermediateSchema schema, final String operationName) throws ODataJPAModelException {

    IntermediateStructuredType structuredType = schema.getStructuredType(declairedReturnType);
    if (structuredType != null)
      return structuredType.getExternalFQN();
    else {
      final IntermediateEnumerationType enumType = schema.getEnumerationType(declairedReturnType);
      if (enumType != null) {
        return enumType.getExternalFQN();
      } else {
        final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedReturnType);
        if (edmType == null)
          throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
              declairedReturnType.getName(), operationName);
        return edmType.getFullQualifiedName();
      }
    }
  }
}
