package de.christiankullmann.cktag.util;

import de.christiankullmann.cktag.exception.UpdateException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Created by Ryan McCormick on 3/31/17.
 *
 * Adopted for the CKTag-Service to merge two DropboxTag-Objects
 */
@Service
public class ApiUtils {

  public void merge(Object obj, Object update) {
    if (!obj.getClass().isAssignableFrom(update.getClass())) {
      return;
    }

    Method[] methods = obj.getClass().getMethods();

    for (Method fromMethod : methods) {
      if (fromMethod.getDeclaringClass().equals(obj.getClass())
          && fromMethod.getName().startsWith("get")) {

        String fromName = fromMethod.getName();
        String toName = fromName.replace("get", "set");

        try {
          Method toMetod = obj.getClass().getMethod(toName, fromMethod.getReturnType());
          Object value = fromMethod.invoke(update, (Object[]) null);
          if (value != null) {
            toMetod.invoke(obj, value);
          }
        } catch (Exception e) {
          throw new UpdateException("Error merging Object [" + obj + "] with Object [" + update + "]", e);
        }

      }
    }
  }


}

