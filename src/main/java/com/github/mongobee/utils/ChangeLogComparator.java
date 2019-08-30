package com.github.mongobee.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.github.mongobee.changeset.ChangeLog;

/**
 * Sort ChangeLogs by 'order' value or class name (if no 'order' is set)
 *
 * @author lstolowski
 * @since 2014-09-17
 */
public class ChangeLogComparator implements Comparator<Class<?>>, Serializable {
  @Override
  public int compare(Class<?> o1, Class<?> o2) {
    ChangeLog c1 = o1.getAnnotation(ChangeLog.class);
    ChangeLog c2 = o2.getAnnotation(ChangeLog.class);

    String val1 = o1.getCanonicalName().contains(c1.order()) ? o1.getCanonicalName() : c1.order();
    String val2 =  o2.getCanonicalName().contains(c2.order()) ? o2.getCanonicalName() : c2.order();

    if (val1 == null && val2 == null){
      return 0;
    } else if (val1 == null) {
      return -1;
    } else if (val2 == null) {
      return 1;
    }

    return val1.compareTo(val2);
  }
}
