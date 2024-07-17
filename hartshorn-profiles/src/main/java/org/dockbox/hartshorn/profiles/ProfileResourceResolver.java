package org.dockbox.hartshorn.profiles;

import java.net.URI;
import java.util.Set;

public interface ProfileResourceResolver {

    Set<URI> resolve(String profileName);

}
