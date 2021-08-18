package org.dockbox.hartshorn.headdatabase;

import com.google.common.collect.Multimap;

import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.ApplicationException;

import java.util.Set;

public interface HeadDatabaseSource {

    Multimap<HeadDatabaseCategory, CustomHead> collect() throws ApplicationException;

    Set<CustomHead> get(HeadDatabaseCategory category);

    Exceptional<CustomHead> first(HeadDatabaseCategory category);

}
