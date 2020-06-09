package com.darwinreforged.server.modules.player.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;

@DatabaseTable(tableName = "plot")
public class PlotStorageModel {

    @DatabaseField(dataType = DataType.INTEGER, canBeNull = false, columnName = "id")
    private int id;

    @DatabaseField(dataType = DataType.INTEGER, canBeNull = false, columnName = "plot_id_x")
    private int plotIdX;

    @DatabaseField(dataType = DataType.INTEGER, canBeNull = false, columnName = "plot_id_z")
    private int plotIdZ;

    @DatabaseField(dataType = DataType.UUID, canBeNull = false, columnName = "owner")
    private UUID owner;

    @DatabaseField(dataType = DataType.STRING, canBeNull = false, columnName = "world")
    private String world;

    @DatabaseField(dataType = DataType.DATE, canBeNull = false, columnName = "timestamp")
    private Date timestamp;

    public int getId() {
        return id;
    }

    public int getPlotIdX() {
        return plotIdX;
    }

    public int getPlotIdZ() {
        return plotIdZ;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorld() {
        return world;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
