package org.dockbox.hartshorn.blockregistry;

import org.dockbox.hartshorn.api.Hartshorn;
import org.dockbox.hartshorn.blockregistry.init.AirInit;
import org.dockbox.hartshorn.blockregistry.init.ArchesInit;
import org.dockbox.hartshorn.blockregistry.init.BarrelsInit;
import org.dockbox.hartshorn.blockregistry.init.BeamsInit;
import org.dockbox.hartshorn.blockregistry.init.BlocksNoVariantsInit;
import org.dockbox.hartshorn.blockregistry.init.BranchesInit;
import org.dockbox.hartshorn.blockregistry.init.ClothInit;
import org.dockbox.hartshorn.blockregistry.init.ColumnsInit;
import org.dockbox.hartshorn.blockregistry.init.CropsInit;
import org.dockbox.hartshorn.blockregistry.init.DirtInit;
import org.dockbox.hartshorn.blockregistry.init.DoorsInit;
import org.dockbox.hartshorn.blockregistry.init.FloorCeilingPatternInit;
import org.dockbox.hartshorn.blockregistry.init.GlassInit;
import org.dockbox.hartshorn.blockregistry.init.GrassGroundInit;
import org.dockbox.hartshorn.blockregistry.init.IrregularBlocksInit;
import org.dockbox.hartshorn.blockregistry.init.LargeStoneSlabsInit;
import org.dockbox.hartshorn.blockregistry.init.LeavesInit;
import org.dockbox.hartshorn.blockregistry.init.LightsInit;
import org.dockbox.hartshorn.blockregistry.init.LogsInit;
import org.dockbox.hartshorn.blockregistry.init.MetalInit;
import org.dockbox.hartshorn.blockregistry.init.PlanksInit;
import org.dockbox.hartshorn.blockregistry.init.PlantsInit;
import org.dockbox.hartshorn.blockregistry.init.PlasterInit;
import org.dockbox.hartshorn.blockregistry.init.RefinedStoneCobbleBrickInit;
import org.dockbox.hartshorn.blockregistry.init.RoadsInit;
import org.dockbox.hartshorn.blockregistry.init.RoofTilesInit;
import org.dockbox.hartshorn.blockregistry.init.RoughNaturalRockInit;
import org.dockbox.hartshorn.blockregistry.init.SandGravelInit;
import org.dockbox.hartshorn.blockregistry.init.SaplingsInit;
import org.dockbox.hartshorn.blockregistry.init.SmoothNaturalRockInit;
import org.dockbox.hartshorn.blockregistry.init.ThatchInit;
import org.dockbox.hartshorn.blockregistry.init.TudorInit;
import org.dockbox.hartshorn.blockregistry.init.VanillaProps.TypeList;
import org.dockbox.hartshorn.blockregistry.init.WallDesignsInit;
import org.dockbox.hartshorn.blockregistry.init.WallDesignsNoWallInit;
import org.dockbox.hartshorn.blockregistry.init.WasteInit;
import org.dockbox.hartshorn.blockregistry.init.WoolInit;

public final class BlockDataInitialisationManager
{
    public static BlockRegistryManager BlockRegistryManager;

    private static final TypeList refinedStoneCobbleBrickShapesVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH,
        VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SPHERE, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.WALL, VariantIdentifier.PILLAR, VariantIdentifier.STAIRS);

    private static final TypeList refinedStoneCobbleBrickShapesVanillaNoWall = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH,
        VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SPHERE, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.PILLAR,
        VariantIdentifier.STAIRS);

    private static final TypeList refinedStoneCobbleBrickShapesTopOverlay = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH,
        VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SPHERE, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.STAIRS, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList refinedStoneCobbleBrickShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_ARCH,
        VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH, VariantIdentifier.TWO_METER_ARCH_HALF,
        VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE,
        VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, 
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.STAIRS, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList roadShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList largeStoneSlabShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_ARCH,
        VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH, VariantIdentifier.TWO_METER_ARCH_HALF,
        VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE,
        VariantIdentifier.CAPITAL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.WALL,
        VariantIdentifier.PILLAR);

    private static final TypeList largeStoneSlabShapesVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH,
        VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList largeStoneSlabShapesVanillaNoStairs = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH,
        VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList wallCarvingsDesignsShapes = TypeList.of(VariantIdentifier.FULL,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL,
        VariantIdentifier.PILLAR);

    private static final TypeList wallCarvingsDesignsNoWallShapes = TypeList.of(VariantIdentifier.FULL,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER);

    private static final TypeList wallCarvingsDesignsNoWallOverlayShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER);

    private static final TypeList wallCarvingsDesignsPillarOverlayShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.PILLAR);

    private static final TypeList tudorShapes = TypeList.of(VariantIdentifier.FULL,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER);

    private static final TypeList tudorSlashShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER);

    private static final TypeList columnShapesVanilla = TypeList.of(VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList columnShapes = TypeList.of(VariantIdentifier.FULL,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList columnShapes2 = TypeList.of(VariantIdentifier.FULL,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList columnShapesLog = TypeList.of(VariantIdentifier.FULL,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList columnDoricCapitalShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.PILLAR, VariantIdentifier.WALL);

    private static final TypeList columnDoricBaseShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.PILLAR, VariantIdentifier.WALL);

    private static final TypeList plasterShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF,
        VariantIdentifier.TWO_METER_ARCH, VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT,
        VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE,
        VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.STAIRS, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList plasterShapesVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SMALL_ARCH, VariantIdentifier.SMALL_ARCH_HALF, VariantIdentifier.TWO_METER_ARCH,
        VariantIdentifier.TWO_METER_ARCH_HALF, VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SPHERE, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS,
        VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList floorCeilingPatternShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList floorCeilingPatternShapesVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList strippedLogShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.WALL, VariantIdentifier.PILLAR,
        VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS,
        VariantIdentifier.BEAM_HORIZONTAL, VariantIdentifier.BEAM_VERTICAL, VariantIdentifier.BEAM_DOOR_FRAME_LINTEL,
        VariantIdentifier.BEAM_DOOR_FRAME_POSTS, VariantIdentifier.BEAM_LINTEL, VariantIdentifier.BEAM_POSTS);

    private static final TypeList strippedLogVanillaShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.WALL,
        VariantIdentifier.PILLAR, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.BEAM_HORIZONTAL,
        VariantIdentifier.BEAM_VERTICAL, VariantIdentifier.BEAM_DOOR_FRAME_LINTEL, VariantIdentifier.BEAM_DOOR_FRAME_POSTS,
        VariantIdentifier.BEAM_LINTEL, VariantIdentifier.BEAM_POSTS);

    private static final TypeList logShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.WALL,
        VariantIdentifier.LARGE_BRANCH, VariantIdentifier.PILLAR, VariantIdentifier.BRANCH,
        VariantIdentifier.SMALL_BRANCH, VariantIdentifier.STUMP, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList logShapesVanilla = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LARGE_BRANCH,
        VariantIdentifier.PILLAR, VariantIdentifier.BRANCH, VariantIdentifier.SMALL_BRANCH, VariantIdentifier.STUMP,
        VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList planksShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE,
        VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList planksVerticalShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE,
        VariantIdentifier.BOARDS, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.WALL,
        VariantIdentifier.PILLAR);

    private static final TypeList planksVerticalCrossFenceShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE,
        VariantIdentifier.BOARDS, VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS,
        VariantIdentifier.WALL, VariantIdentifier.PILLAR, VariantIdentifier.FENCE);

    private static final TypeList planksHorizontalShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SMALL_WINDOW,
        VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE,
        VariantIdentifier.BOARDS, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.WALL,
        VariantIdentifier.PILLAR);

    private static final TypeList thatchShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.STAIRS);

    private static final TypeList woolShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList clothShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList roughNaturalRockShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList roughNaturalRockShapesRocks = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.ROCKS);

    private static final TypeList roughNaturalRockShapesVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList roughNaturalRockShapesRocksVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.SLAB, VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS,
        VariantIdentifier.ROCKS);

    private static final TypeList smoothNaturalRockShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.ARROWSLIT,
        VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE,
        VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.STAIRS, VariantIdentifier.WALL, VariantIdentifier.PILLAR);

    private static final TypeList smoothNaturalRockShapesRocks = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.ARROWSLIT,
        VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF, VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL,
        VariantIdentifier.SPHERE, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB,
        VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS, VariantIdentifier.WALL,
        VariantIdentifier.PILLAR, VariantIdentifier.ROCKS);

    private static final TypeList smoothNaturalRockShapesVanillaNoWall = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF,
        VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.PILLAR, VariantIdentifier.ROCKS);

    private static final TypeList smoothNaturalRockShapesVanilla = TypeList.of(VariantIdentifier.FULL,
        VariantIdentifier.ARROWSLIT, VariantIdentifier.SMALL_WINDOW, VariantIdentifier.SMALL_WINDOW_HALF,
        VariantIdentifier.BALUSTRADE, VariantIdentifier.CAPITAL, VariantIdentifier.SPHERE, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB,  VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR, VariantIdentifier.ROCKS);

    private static final TypeList sandGravelShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER, VariantIdentifier.STAIRS);
    private static final TypeList sandGravelShapesVanilla = TypeList.of(VariantIdentifier.LAYER, VariantIdentifier.STAIRS);
    private static final TypeList sandGravelShapesOverlay = TypeList.of(VariantIdentifier.LAYER, VariantIdentifier.STAIRS);

    private static final TypeList grassGroundShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.LAYER, VariantIdentifier.STAIRS,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER);

    private static final TypeList dirtShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,  VariantIdentifier.VERTICAL_SLAB,
        VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList dirtShapesVanilla = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.STAIRS);

    private static final TypeList roofTileShapes = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,  VariantIdentifier.QUARTER_SLAB,
        VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB, VariantIdentifier.VERTICAL_CORNER_SLAB,
        VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER, VariantIdentifier.VERTICAL_QUARTER,
        VariantIdentifier.WALL, VariantIdentifier.PILLAR, VariantIdentifier.CAPITAL, VariantIdentifier.STAIRS);

    private static final TypeList roofTileShapesVanilla = TypeList.of(VariantIdentifier.FULL, VariantIdentifier.SLAB,
        VariantIdentifier.QUARTER_SLAB, VariantIdentifier.CORNER_SLAB, VariantIdentifier.EIGHTH_SLAB,
        VariantIdentifier.VERTICAL_CORNER_SLAB, VariantIdentifier.VERTICAL_SLAB, VariantIdentifier.VERTICAL_CORNER,
        VariantIdentifier.VERTICAL_QUARTER, VariantIdentifier.WALL, VariantIdentifier.PILLAR,
        VariantIdentifier.CAPITAL, VariantIdentifier.STAIRS);

    private BlockDataInitialisationManager() {}

    public static void constructBlockRegistry()
    {
        BlockRegistryManager = Hartshorn.context().get(BlockRegistryManager.class);

        RefinedStoneCobbleBrickInit.init(refinedStoneCobbleBrickShapes, refinedStoneCobbleBrickShapesVanilla, refinedStoneCobbleBrickShapesVanillaNoWall, refinedStoneCobbleBrickShapesTopOverlay);
        LargeStoneSlabsInit.init(largeStoneSlabShapes, largeStoneSlabShapesVanilla, largeStoneSlabShapesVanillaNoStairs);
        RoadsInit.init(roadShapes);
        RoofTilesInit.init(roofTileShapes, roofTileShapesVanilla);
        WallDesignsInit.init(wallCarvingsDesignsShapes);
        WallDesignsNoWallInit.init(wallCarvingsDesignsNoWallShapes, wallCarvingsDesignsNoWallOverlayShapes, wallCarvingsDesignsPillarOverlayShapes);
        TudorInit.init(tudorShapes, tudorSlashShapes);
        ColumnsInit.init(columnShapes, columnDoricCapitalShapes, columnDoricBaseShapes, columnShapes2, columnShapesVanilla, columnShapesLog);
        PlasterInit.init(plasterShapes, plasterShapesVanilla);
        FloorCeilingPatternInit.init(floorCeilingPatternShapes, floorCeilingPatternShapesVanilla);
        BeamsInit.init(strippedLogShapes, strippedLogVanillaShapes);
        PlanksInit.init(planksShapes, planksHorizontalShapes, planksVerticalShapes, planksVerticalCrossFenceShapes);
        LogsInit.init(logShapes, logShapesVanilla);
        BranchesInit.init();
        ThatchInit.init(thatchShapes);
        WoolInit.init(woolShapes);
        ClothInit.init(clothShapes);
        MetalInit.init();
        RoughNaturalRockInit.init(roughNaturalRockShapes, roughNaturalRockShapesRocks, roughNaturalRockShapesVanilla, roughNaturalRockShapesRocksVanilla);
        SmoothNaturalRockInit.init(smoothNaturalRockShapes, smoothNaturalRockShapesRocks, smoothNaturalRockShapesVanillaNoWall, smoothNaturalRockShapesVanilla);
        SandGravelInit.init(sandGravelShapes, sandGravelShapesVanilla, sandGravelShapesOverlay);
        GrassGroundInit.init(grassGroundShapes);
        DirtInit.init(dirtShapes, dirtShapesVanilla);
        IrregularBlocksInit.init();
        BlocksNoVariantsInit.init();
        DoorsInit.init();
        GlassInit.init();
        BarrelsInit.init();
        LightsInit.init();
        LeavesInit.init();
        SaplingsInit.init();
        PlantsInit.init();
        CropsInit.init();
        WasteInit.init();
        AirInit.init();
        ArchesInit.init();

        BlockRegistryManager.saveBlockRegistry();
    }
}
