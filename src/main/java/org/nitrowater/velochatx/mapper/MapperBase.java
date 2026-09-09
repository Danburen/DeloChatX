package org.nitrowater.velochatx.mapper;

import org.nitrowater.velochatx.VeloChatX;
import org.nitrowater.waterapi.domain.infra.io.database.H2DataBase;
import org.waterwood.io.database.H2DataBase;
import org.waterwood.plugin.WaterPlugin;

public abstract class MapperBase extends H2DataBase {
    private static final String dbFilePath = VeloChatX.getInstance().getDefaultFilePath("data").replace("/","\\");

    public MapperBase() {
        super(dbFilePath);
        if(connection == null) {
            VeloChatX.getInstance().getLogger().warning(WaterPlugin.getPluginMessage("error-connect-database-message").formatted(dbFilePath));
        }else{
            VeloChatX.getInstance().getLogger().info(WaterPlugin.getPluginMessage("success-connect-database-message"));
            createTable();
        }
    }

    protected abstract void createTable();
}
