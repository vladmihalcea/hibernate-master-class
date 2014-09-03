@GenericGenerators(
        {
                @GenericGenerator(
                        name = "uuid2",
                        strategy = "uuid2"
                )
        }
)
package com.vladmihalcea.hibernate.masterclass.laboratory.flushing;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.GenericGenerators;