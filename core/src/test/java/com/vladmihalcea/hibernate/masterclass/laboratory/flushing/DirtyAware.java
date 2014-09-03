package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import java.util.Set;

/**
 * DirtyAware - Dirty Aware
 *
 * @author Vlad Mihalcea
 */
public interface DirtyAware {

    Set<String> getDirtyProperties();

    void clearDirtyProperties();
}
