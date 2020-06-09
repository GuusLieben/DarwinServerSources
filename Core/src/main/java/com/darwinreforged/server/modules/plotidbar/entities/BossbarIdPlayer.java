package com.darwinreforged.server.modules.plotidbar.entities;

import com.darwinreforged.server.core.DarwinServer;
import com.darwinreforged.server.core.chat.Text;
import com.darwinreforged.server.core.player.DarwinPlayer;
import com.darwinreforged.server.core.types.virtual.Bossbar;
import com.darwinreforged.server.core.util.CommonUtils;

public class BossbarIdPlayer extends DarwinPlayer {

    private Bossbar plotIdBar;
    private Bossbar memberBar;

    private BossbarIdPlayer(DarwinPlayer player) {
        super(player.getUniqueId(), player.getName());
    }

    public void setPlotIdBarTitle(Text title) {
        plotIdBar.setTitle(title);
    }

    public Bossbar getPlotIdBar() {
        return plotIdBar;
    }

    public Bossbar getMemberBar() {
        return memberBar;
    }

    private void updatePlotIdBar() {
        DarwinServer.getUtilMan().get(CommonUtils.class).toggleBossbar(plotIdBar, this);
    }
}
