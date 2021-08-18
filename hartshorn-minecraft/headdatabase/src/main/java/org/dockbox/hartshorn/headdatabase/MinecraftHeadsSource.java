package org.dockbox.hartshorn.headdatabase;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.api.exceptions.ApplicationException;
import org.dockbox.hartshorn.api.exceptions.Except;
import org.dockbox.hartshorn.di.annotations.inject.Binds;
import org.dockbox.hartshorn.persistence.FileType;
import org.dockbox.hartshorn.persistence.mapping.GenericType;
import org.dockbox.hartshorn.persistence.mapping.ObjectMapper;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Binds(HeadDatabaseSource.class)
public class MinecraftHeadsSource implements HeadDatabaseSource {

    private static final String BASE_URL = "https://minecraft-heads.com/scripts/api.php?tags=true&cat=";

    private final Multimap<HeadDatabaseCategory, CustomHead> heads = ArrayListMultimap.create();

    @Override
    public Multimap<HeadDatabaseCategory, CustomHead> collect() throws ApplicationException {
        this.heads.clear();

        for (final HeadDatabaseCategory category : HeadDatabaseCategory.values()) {
            try {
                final String raw = BASE_URL + category.name().toLowerCase(Locale.ROOT).replaceAll("_", "-");
                final Exceptional<List<CustomHead>> collected = Hartshorn.context().get(ObjectMapper.class).fileType(FileType.JSON).read(
                        new URL(raw),
                        new GenericType<List<CustomHead>>() {
                        });
                collected.present(heads -> this.heads.putAll(category, collected.get()));
            } catch (final MalformedURLException e) {
                throw new ApplicationException(e);
            }
        }

        return this.heads;
    }

    @Override
    public Set<CustomHead> get(final HeadDatabaseCategory category) {
        if (this.heads.isEmpty()) {
            try {
                this.collect();
            }
            catch (final ApplicationException e) {
                Except.handle(e);
                return HartshornUtils.emptySet();
            }
        }
        return HartshornUtils.asUnmodifiableSet(this.heads.get(category));
    }

    @Override
    public Exceptional<CustomHead> first(final HeadDatabaseCategory category) {
        final Set<CustomHead> heads = this.get(category);
        if (!heads.isEmpty()) {
            return Exceptional.of(heads.stream().findFirst());
        }
        return Exceptional.empty();
    }
}
