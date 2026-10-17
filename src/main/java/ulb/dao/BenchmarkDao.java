package ulb.dao;

import java.util.List;

import ulb.object_db_model.Player;

public interface BenchmarkDao extends AutoCloseable {
    void init() throws Exception;

    void clearDatabase() throws Exception;

    void insertPlayers(List<Player> players) throws Exception;

    @Override
    void close() throws Exception;
}
