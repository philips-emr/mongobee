package com.github.mongobee.utils;

import static java.util.Arrays.asList;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.github.mongobee.changeset.ChangeEntry;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.github.mongobee.exception.MongobeeChangeSetException;

/**
 * Utilities to deal with reflections and annotations
 *
 * @author lstolowski
 * @since 27/07/2014
 */
public class ChangeService {
    private static final String DEFAULT_PROFILE = "default";

    private final String changeLogsBasePackage;
    private final List<String> activeProfiles;


    public ChangeService(String changeLogsBasePackage) {
        this.changeLogsBasePackage = changeLogsBasePackage;
        this.activeProfiles = asList(DEFAULT_PROFILE);
    }

    public List<Class<?>> fetchChangeLogs(){
        Reflections reflections = new Reflections(changeLogsBasePackage);
        Set<Class<?>> changeLogs = reflections.getTypesAnnotatedWith(ChangeLog.class);
        List<Class<?>> filteredChangeLogs = (List<Class<?>>) filterByActiveProfiles(changeLogs);

        Collections.sort(filteredChangeLogs, new ChangeLogComparator());

        return filteredChangeLogs;
    }

    public List<Method> fetchChangeSets(final Class<?> type) throws MongobeeChangeSetException {
        final List<Method> changeSets = filterChangeSetAnnotation(asList(type.getDeclaredMethods()));
        final List<Method> filteredChangeSets = (List<Method>) filterByActiveProfiles(changeSets);

        Collections.sort(filteredChangeSets, new ChangeSetComparator());

        return filteredChangeSets;
    }

    public boolean isRunAlwaysChangeSet(Method changesetMethod){
        if (changesetMethod.isAnnotationPresent(ChangeSet.class)){
            ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);
            return annotation.runAlways();
        } else {
            return false;
        }
    }

    public ChangeEntry createChangeEntry(Method changesetMethod){
        if (changesetMethod.isAnnotationPresent(ChangeSet.class)){
            ChangeSet annotation = changesetMethod.getAnnotation(ChangeSet.class);

            return new ChangeEntry(
                    annotation.id(),
                    annotation.author(),
                    new Date(),
                    changesetMethod.getDeclaringClass().getName(),
                    changesetMethod.getName());
        } else {
            return null;
        }
    }

    private boolean matchesActiveSpringProfile(AnnotatedElement element) {
        if (!ClassUtils.isPresent("org.springframework.context.annotation.Profile", null)) {
            return true;
        }

        return false;
    }

    private List<?> filterByActiveProfiles(Collection<? extends AnnotatedElement> annotated) {
        List<AnnotatedElement> filtered = new ArrayList<>();
        for (AnnotatedElement element : annotated) {
            filtered.add( element);
        }
        return filtered;
    }

    private List<Method> filterChangeSetAnnotation(List<Method> allMethods) throws MongobeeChangeSetException {
        final Set<String> changeSetIds = new HashSet<>();
        final List<Method> changesetMethods = new ArrayList<>();
        for (final Method method : allMethods) {
            if (method.isAnnotationPresent(ChangeSet.class)) {
                String id = method.getAnnotation(ChangeSet.class).id();
                if (changeSetIds.contains(id)) {
                    throw new MongobeeChangeSetException(String.format("Duplicated changeset id found: '%s'", id));
                }
                changeSetIds.add(id);
                changesetMethods.add(method);
            }
        }
        return changesetMethods;
    }

}
