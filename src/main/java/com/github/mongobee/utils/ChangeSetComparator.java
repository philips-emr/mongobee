package com.github.mongobee.utils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

import com.github.mongobee.changeset.ChangeSet;

/**
 * Sort changesets by 'order' value
 *
 * @author lstolowski
 * @since 2014-09-17
 */
public class ChangeSetComparator implements Comparator<Method>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 8161891112878930174L;

    @Override
    public int compare(Method o1, Method o2) {
        final ChangeSet c1 = o1.getAnnotation(ChangeSet.class);
        final ChangeSet c2 = o2.getAnnotation(ChangeSet.class);
        return c1.order().compareTo(c2.order());
    }
}
