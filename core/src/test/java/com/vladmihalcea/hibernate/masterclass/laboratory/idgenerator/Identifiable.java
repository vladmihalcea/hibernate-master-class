package com.vladmihalcea.hibernate.masterclass.laboratory.idgenerator;

import java.io.Serializable;

/**
 * Identifiable - Identifiable
 *
 * @author Vlad Mihalcea
 */
public interface Identifiable<T extends Serializable> {

    T getId();
}
