package org.dockbox.selene.blockregistry.handlers;

import org.dockbox.selene.api.domain.tuple.Tuple;
import org.dockbox.selene.server.minecraft.item.Item;
import org.dockbox.selene.util.SeleneUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MC1_12BlockRegistryHandler implements IBlockRegistryHandler
{
    /**
     * <pre>
     * Extracts the information from a parentID
     *
     * Consider the parentID: conquest:stone_stairs_1:15
     * Group 1 matches - conquest (MODID)
     * Group 2 matches - stone    (BLOCK)
     * Group 3 matches - stairs   (VARIANT)
     * Group 4 matches - 1        (NUMBER)
     * Group 5 matches - 15       (META)
     * </pre>
     */
    private static final Pattern ID_INFORMATION = Pattern.compile("^(\\w*?):(\\w+)_([\\w]*)_([\\d]{0,3}):?([\\d]{0,2})");

    //TODO: Potentially refactor
    private final Map<String, String> parentMappings      = SeleneUtils.emptyMap();
    private final Map<String, List<String>> aliasMappings = SeleneUtils.emptyMap();

    @Override
    public void loadIdRegistry()
    {

    }

    /**
     * Returns the parentID of an alias. E.g: For {@code conquest:stone_plastered_full}, the parentID is
     * {@code conquest:stone_full_1:0}.
     * <p>
     * If there is no parentID for that alias, it just returns the passed in alias.
     *
     * @param alias
     *      The alias to find the parentID of
     *
     * @return The parentID, or the passed in alias if there is no parentID
     */
    @Override
    public String getParent(String alias)
    {
        return this.parentMappings.getOrDefault(alias, alias);
    }

    //TODO: Remove if unused
    @Override
    public String getRootString(String alias)
    {
        String parentID = this.getParent(alias);
        return parentID.replaceFirst(ID_INFORMATION.pattern(),"$1:$2_full_$4:$5");
    }

    /**
     * Returns the {@link Tuple rootID} of the specified alias, containing the {@link String id} and {@link Integer}
     * meta. The rootID is the parentID of the fullblock.
     * <p>
     * E.g: For {@code conquest:stone_stairs_1:0}, the rootID is {@code conquest:stone_full_1:0}.
     *
     * @param alias
     *      The alias to find the rootID of
     *
     * @return The {@link Tuple rootID} of the specified alias
     */
    @Override
    public Tuple<String, Integer> getRoot(String alias)
    {
        String parentID = this.getParent(alias);
        String root = parentID.replaceFirst(ID_INFORMATION.pattern(),"$1:$2_full_$4");

        Matcher matcher = ID_INFORMATION.matcher(parentID);
        if (matcher.matches()) {
            int meta = Integer.parseInt(matcher.group(IdInformation.META.ordinal()));
            return Tuple.of(root, meta);
        }
        return Tuple.of(root, 0);
    }

    @Override
    public List<String> getAliases(Item item)
    {
        return this.getAliases(item.getId());
    }

    @Override
    public List<String> getAliases(String alias)
    {
        return this.aliasMappings.getOrDefault(this.getParent(alias), new ArrayList<>());
    }

    @Override
    public List<String> getAliases(String parentID, int meta)
    {
        return this.aliasMappings.getOrDefault(parentID + ":" + meta, new ArrayList<>());
    }
}
