package org.dockbox.hartshorn.blockregistry;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.api.domain.Exceptional;
import org.dockbox.hartshorn.di.annotations.service.Service;
import org.dockbox.hartshorn.persistence.mapping.GenericType;
import org.dockbox.hartshorn.persistence.mapping.ObjectMapper;
import org.dockbox.hartshorn.persistence.properties.PersistenceModifier;
import org.dockbox.hartshorn.persistence.properties.PersistenceProperty;
import org.dockbox.hartshorn.persistence.registry.Registry;
import org.dockbox.hartshorn.server.minecraft.item.Item;
import org.dockbox.hartshorn.server.minecraft.item.ItemContext;
import org.dockbox.hartshorn.util.HartshornUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlockRegistryManager
{

    private final Map<String, Set<String>> rootAliases     = HartshornUtils.emptyMap();
    private final Map<String, String> rootToFamilyMappings = HartshornUtils.emptyMap();

    private final Map<String, Registry<String>> blockRegistry;

    public BlockRegistryManager() {
        this.blockRegistry = this.loadBlockRegistry();
    }

    /**
     * Determines the {@link VariantIdentifier} of the specified {@link Item}.
     *
     * @param item
     *      The {@link Item} to find the variant of
     *
     * @return An {@link Exceptional} containing the {@link VariantIdentifier}
     */
    public Exceptional<VariantIdentifier> variant(Item item) {
        return this.variant(item.id());
    }

    /**
     * Determines the {@link VariantIdentifier} of the specified {@link Item}.
     *
     * @param id
     *      The id of the {@link Item} to find the variant of
     *
     * @return An {@link Exceptional} containing the {@link VariantIdentifier}
     */
    public Exceptional<VariantIdentifier> variant(final String id) {
        return Exceptional.of(this.variants(id)
            .entrySet()
            .stream()
            .filter(e -> e.getValue().contains(id))
            .findFirst()
            .map(e -> VariantIdentifier.valueOf(e.getKey())));
    }

    /**
     * Determines if the specified id is a root id (The id that is assigned by CR, rather than an alias).
     *
     * @param rootId
     *      The id to check
     *
     * @return If the specified id is a root id
     */
    public boolean isRootId(@NotNull String rootId) {
        return this.rootToFamilyMappings.containsKey(rootId);
    }

    /**
     * Determines if the specified id is a family id (The fullblock variant for a block type).
     *
     * @param familyId
     *      The id to check
     *
     * @return If the specified id is a family id
     */
    public boolean isFamilyId(@NotNull String familyId) {
        return this.blockRegistry.containsKey(familyId);
    }

    /**
     * Retrieves the family id of the specified root id, if it exists.
     *
     * @param rootId
     *      The root id to get the family id for
     *
     * @return An {@link Exceptional} containing the family id
     */
    public Exceptional<String> familyId(@NotNull String rootId) {
        if (this.isFamilyId(rootId))
            return Exceptional.of(rootId);

        return this.isRootId(rootId)
            ? Exceptional.of(this.rootToFamilyMappings.get(rootId))
            : Exceptional.empty();
    }

    /**
     * Retrieves the registered aliases for the specified root id (excluding the root id). If there are no aliases
     * for that root id then an empty set will be returned.
     *
     * @param rootId
     *      The root id to find the aliases for
     *
     * @return A {@link Set} containing the registered aliases
     */
    public Set<String> aliases(@NotNull String rootId) {
        return this.rootAliases.getOrDefault(rootId, HartshornUtils.emptySet());
    }

    /**
     * Retrieves a {@link Registry} containing the root ids of the variants of the block family specified by the {@link Item}.
     *
     * @param item
     *      The {@link Item} of any block in the family you want to retrieve the variants for
     *
     * @return A {@link Registry} mapping the root ids of the variants to {@link VariantIdentifier variant identifiers}
     */
    public Registry<String> variants(@NotNull Item item) {
        return this.variants(item.id());
    }

    /**
     * Retrieves a {@link Registry} containing the root ids of the variants of the block family specified by the id
     *
     * @param rootId
     *      The root id of any block in the family you want to retrieve the variants for
     *
     * @return A {@link Registry} mapping the root ids of the variants to {@link VariantIdentifier variant identifiers}
     */
    public Registry<String> variants(@NotNull String rootId) {
        return this.familyId(rootId)
            .map(familyId -> this.blockRegistry.getOrDefault(familyId, new Registry<>()))
            .or(new Registry<>());
    }

    /**
     * Determines if the specified alias has been registered already.
     *
     * @param alias
     *      The alias to check if registered
     *
     * @return If the alias is already registered
     */
    public boolean hasAliasRegistered(@NotNull String alias) {
        return this.rootAliases.values()
            .stream()
            .anyMatch(a -> a.contains(alias));
    }

    /**
     * Maps the alias to the specified root id and automatically registers it within Hartshorn. If the alias already
     * exists then an {@link IllegalArgumentException} will be thrown.
     *
     * @param rootId
     *      The root id to map the alias to
     * @param alias
     *      The alias to add
     */
    public void addAlias(@NotNull @NonNls String rootId, @NotNull String alias) {
        if (this.hasAliasRegistered(alias))
            throw new IllegalArgumentException(
                String.format("The alias %s has already been registered", alias));

        if (this.rootAliases.containsKey(rootId))
            this.rootAliases.get(rootId).add(alias);
        else this.rootAliases.put(rootId, HartshornUtils.asSet(alias));

        Hartshorn.context().first(ItemContext.class)
                .present(context -> context.register(alias, () -> Item.of(rootId)));
    }

    /**
     * Maps the root id to the specified family id. If the root id already exists and is not mapped to the specified
     * family id then an {@link IllegalArgumentException} will be thrown.
     *
     * @param rootId
     *      The root id to add
     * @param familyId
     *      The family id to map the root id to
     */
    public void addRoot(@NotNull String rootId, @NotNull @NonNls String familyId) {
        String currentFamily = this.rootToFamilyMappings.putIfAbsent(rootId, familyId);

        if (null != currentFamily && !currentFamily.equals(familyId))
            throw new IllegalArgumentException(
                String.format("The root %s has already been mapped to the family %s", rootId, currentFamily));
    }

    /**
     * Adds the variant to the block registry.
     *
     * @param familyId
     *      The family id of the variant
     * @param variant
     *      The {@link VariantIdentifier} for the variant
     * @param rootId
     *      The root id of the variant
     */
    public void addVariant(@NotNull String familyId, @NotNull VariantIdentifier variant, @NotNull String rootId) {
        if (!this.blockRegistry.containsKey(familyId))
            this.blockRegistry.put(familyId, new Registry<>());
        if (!this.rootToFamilyMappings.containsKey(rootId))
            this.rootToFamilyMappings.put(rootId, familyId);

        this.blockRegistry.get(familyId).add(variant, rootId);
    }

    /**
     * Loads the serialised block registry from blockregistry.json. If that file doesn't exist or there was an issue
     * loading the block registry, then an empty {@link Registry} is returned instead. All the specified aliases are
     * then automatically registered.
     *
     * @return The loaded block registry
     */
    private Map<String, Registry<String>> loadBlockRegistry() {
        Map<String, Registry<VariantModel>> storedBlockRegistry =
            Hartshorn.context()
                .get(ObjectMapper.class, PersistenceProperty.of(PersistenceModifier.SKIP_EMPTY))
                .read(Path.of("blockregistry.json"), new GenericType<Map<String, Registry<VariantModel>>>() {})
                .or(HartshornUtils.emptyMap());

        //Register aliases and roots
        storedBlockRegistry.forEach(
            (familyId, variantRegistry) -> variantRegistry.forEach(
                (@NonNls var variantKey, var variantColumn) -> variantColumn.forEach(
                    variantModel -> {
                        if (!variantKey.equals(VariantIdentifier.FULL.name())) {
                            this.rootToFamilyMappings.put(variantModel.rootId(), familyId);
                        }
                        if (null != variantModel.aliases())
                            variantModel.aliases()
                                .forEach(alias -> this.addAlias(variantModel.rootId(), alias));
                    }
        )));

        return storedBlockRegistry.entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
                .mapValues(VariantModel::rootId)
        ));
    }

    /**
     * Saves the block registry to blockregistry.json.
     */
    public void saveBlockRegistry() {
        Map<String, Registry<VariantModel>> storedBlockRegistry =
            this.blockRegistry.entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue()
                    .mapValues(rootId -> new VariantModel(rootId,
                        this.rootAliases.getOrDefault(rootId,HartshornUtils.emptySet())))
        ));

        Hartshorn.context()
            .get(ObjectMapper.class, PersistenceProperty.of(PersistenceModifier.SKIP_EMPTY))
            .write(Path.of("blockregistry.json"), storedBlockRegistry);
    }

    @Override
    public String toString() {
        return this.blockRegistry.toString();
    }
}
