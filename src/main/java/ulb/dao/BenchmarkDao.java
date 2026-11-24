package ulb.dao;

import java.util.List;

import ulb.exception.DaoException;
import ulb.object_db_model.Player;

public interface BenchmarkDao extends AutoCloseable {
    void init() throws DaoException;

    void clearDatabase() throws DaoException;

    void insertPlayers(List<Player> players) throws DaoException;

    int getPlayerWithRelations(String playername) throws DaoException;

    void updateInventoryAmount(String playername, String itemName, int amount) throws DaoException;

    List<String> searchLegendaryOwners(String requester) throws DaoException;

    List<String> searchActiveLegendaryOfType(String requester, String type) throws DaoException;

    default void refreshStatistics() throws DaoException {
    }

    @Override
    void close() throws Exception;
}
