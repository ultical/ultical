package de.ultical.backend.data;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.mapper.BaseMapper;
import de.ultical.backend.data.mapper.DfvMvNameMapper;
import de.ultical.backend.data.mapper.DfvPlayerMapper;
import de.ultical.backend.data.mapper.DivisionRegistrationMapper;
import de.ultical.backend.data.mapper.PlayerMapper;
import de.ultical.backend.data.mapper.RosterMapper;
import de.ultical.backend.data.mapper.SeasonMapper;
import de.ultical.backend.data.mapper.TeamMapper;
import de.ultical.backend.data.mapper.TeamRegistrationMapper;
import de.ultical.backend.data.mapper.UserMapper;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Identifiable;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.User;

/**
 * the cloud
 *
 * @author bbe
 *
 */
@RequestScoped
public class DataStore {

    @Inject
    SqlSession sqlSession;

    @Inject
    Client client;

    /**
     * set to <code>false</code> if you want to perform more then one dataStore
     * operation. However, if you do so, then you have to manually close the
     * session, once you finished.
     */
    private boolean autoCloseSession = true;

    public DataStore() {

    }

    /**
     * Change this <code>DataStore</code>'s autoClose behavior.
     * <p>
     * If set to <code>false</code> the different operations of this dataStore
     * do not automatically close the <code>DataStore</code>'s
     * {@link SqlSession}. In this case you are responsible for closing the
     * <code>DataStore</code>'s session by invoking {@link #closeSession()}
     * manually.
     * </p>
     * <p>
     * By default the <code>DataStore</code> closes the session automatically.
     * </p>
     *
     * @param newACS
     *            whether or not the auto-close feature should be used.
     */
    public void setAutoCloseSession(boolean newACS) {
        this.autoCloseSession = newACS;
    }

    /**
     * Closes the <code>DataStore</code>'s corresponding {@link SqlSession}.
     * <p>
     * The session will only be closed if it is not <code>null</code> and if the
     * {@link #setAutoCloseSession(boolean) auto-close feature} is set to
     * <code>false</code>. If the session's close method has been invoked, this
     * method returns <code>true</code>, otherwise <code>false</code>.
     * </p>
     *
     * @return <code>true</code> if the session has been closed, otherwise
     *         <code>false</code>.
     */
    public boolean closeSession() {
        boolean result = false;
        if (this.sqlSession != null && !this.autoCloseSession) {
            this.sqlSession.close();
            result = true;
        }
        return result;
    }

    public <T extends Identifiable> List<T> getAll(Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(instance.getMapper());
            return mapper.getAll();
        } catch (IllegalAccessException | InstantiationException iae) {
            throw new PersistenceException(iae);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public <T extends Identifiable> T addNew(T newInstance) {
        try {
            BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(newInstance.getMapper());
            mapper.insert(newInstance);
            this.sqlSession.commit();
            return newInstance;
        } catch (PersistenceException pe) {
            this.sqlSession.rollback();
            throw pe;
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public <T extends Identifiable> boolean update(T updatedInstance) {
        try {
            BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(updatedInstance.getMapper());
            Integer updateCount = mapper.update(updatedInstance);
            this.sqlSession.commit();
            return updateCount == 1;
        } catch (PersistenceException pe) {
            this.sqlSession.rollback();
            throw pe;
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public <T extends Identifiable> T get(Integer id, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(instance.getMapper());
            return mapper.get(id);
        } catch (InstantiationException | IllegalAccessException e) {
            this.sqlSession.rollback();
            throw new PersistenceException(e);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public <T extends Identifiable> void remove(Integer id, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            BaseMapper<T> mapper = (BaseMapper<T>) this.sqlSession.getMapper(instance.getMapper());
            mapper.delete(id);
            this.sqlSession.commit();
        } catch (Exception e) {
            this.sqlSession.rollback();
            throw new PersistenceException(e);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public DivisionRegistration addDivisionToEdition(final TournamentEdition edition,
            final DivisionRegistration division) {
        Objects.requireNonNull(division);
        Objects.requireNonNull(edition);
        try {
            DivisionRegistrationMapper drm = this.sqlSession.getMapper(DivisionRegistrationMapper.class);
            drm.insert(division, edition);
            return division;
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public Roster getRosterOfPlayerSeason(int playerId, int seasonId, String divisionAge, String divisionType) {
        try {
            RosterMapper rosterMapper = this.sqlSession.getMapper(RosterMapper.class);
            return rosterMapper.getByPlayerSeasonDivision(playerId, seasonId, divisionAge, divisionType);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void removePlayerFromRoster(int playerId, int rosterId) {
        try {
            RosterMapper rosterMapper = this.sqlSession.getMapper(RosterMapper.class);
            rosterMapper.deletePlayer(playerId, rosterId);
            this.sqlSession.commit();
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public Player getPlayerByDfvNumber(int dfvNumber) {
        try {
            DfvPlayerMapper dfvPlayerMapper = this.sqlSession.getMapper(DfvPlayerMapper.class);
            return dfvPlayerMapper.getByDfvNumber(dfvNumber);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void addPlayerToRoster(Roster roster, Player player) {
        try {
            RosterMapper rosterMapper = this.sqlSession.getMapper(RosterMapper.class);
            rosterMapper.addPlayer(roster, player);
            this.sqlSession.commit();
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    /*
     * clear and refill the DfvMvName table
     */
    public void refreshDfvNames(List<DfvMvName> dfvNames) {
        try {
            DfvMvNameMapper nameMapper = this.sqlSession.getMapper(DfvMvNameMapper.class);
            nameMapper.deleteAll();
            for (DfvMvName name : dfvNames) {
                nameMapper.insert(name);
            }
            this.sqlSession.commit();
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public List<DfvMvName> getDfvNames(String firstname, String lastname) {
        try {
            DfvMvNameMapper nameMapper = this.sqlSession.getMapper(DfvMvNameMapper.class);
            return nameMapper.getByName(firstname, lastname);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public List<Season> getAllSeasons() {
        try {
            SeasonMapper sm = this.sqlSession.getMapper(SeasonMapper.class);
            return sm.getAll();
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public Season getSeason(final int id) {
        try {
            SeasonMapper sm = this.sqlSession.getMapper(SeasonMapper.class);
            return sm.get(id);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public Season addSeason(final Season newSeason) {
        Season checkedSeason = Objects.requireNonNull(newSeason);
        try {
            SeasonMapper mapper = this.sqlSession.getMapper(checkedSeason.getMapper());
            mapper.insert(checkedSeason);
            this.sqlSession.commit();
            return checkedSeason;
        } catch (PersistenceException pe) {
            this.sqlSession.rollback();
            throw pe;
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public boolean updateSeason(final Season updSeason) {
        boolean result = false;
        Objects.requireNonNull(updSeason);
        int updateCount = 0;
        try {
            SeasonMapper mapper = this.sqlSession.getMapper(updSeason.getMapper());
            updateCount = mapper.update(updSeason);
            this.sqlSession.commit();
        } catch (PersistenceException pe) {
            this.sqlSession.rollback();
            throw pe;
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
        result = updateCount == 1;
        return result;
    }

    public List<Team> getTeamsByUser(int userId) {
        TeamMapper teamMapper = this.sqlSession.getMapper(TeamMapper.class);
        return teamMapper.getByUser(userId);
    }

    public Team getTeamByName(String teamName) {
        TeamMapper teamMapper = this.sqlSession.getMapper(TeamMapper.class);
        return teamMapper.getByName(teamName);
    }

    public void updateDfvPlayer(DfvPlayer dfvPlayer) {
        /**
         * A DfvPlayer has to be stored in two steps First Player (superclass)
         * then DfvPlayer (subclass)
         */
        boolean orgCloseSession = this.autoCloseSession;

        // only close session at the end
        this.setAutoCloseSession(false);

        // insert Player with corresponding mapper
        PlayerMapper playerMapper = this.sqlSession.getMapper(PlayerMapper.class);
        playerMapper.update(dfvPlayer);

        // insert DfvPlayer
        this.update(dfvPlayer);

        // set autoclose to original value
        this.setAutoCloseSession(orgCloseSession);

        if (this.sqlSession != null && this.autoCloseSession) {
            this.sqlSession.close();
        }
    }

    public void storeDfvPlayer(DfvPlayer dfvPlayer) {
        /**
         * A DfvPlayer has to be stored in two steps First Player (superclass)
         * then DfvPlayer (subclass)
         */
        boolean orgCloseSession = this.autoCloseSession;

        // only close session at the end
        this.setAutoCloseSession(false);

        // insert Player with corresponding mapper
        PlayerMapper playerMapper = this.sqlSession.getMapper(PlayerMapper.class);
        playerMapper.insert(dfvPlayer);

        // request id to store it in dfvPlayer object
        dfvPlayer.getId();

        // insert DfvPlayer
        this.addNew(dfvPlayer);

        // set autoclose to original value
        this.setAutoCloseSession(orgCloseSession);

        if (this.sqlSession != null && this.autoCloseSession) {
            this.sqlSession.close();
        }
    }

    public void storeUser(User user, boolean playerNewlyCreated) {
        /**
         * A user has to be stored in two steps First DfvPlayer, then User
         */
        boolean orgCloseSession = this.autoCloseSession;

        // only close session at the end
        this.setAutoCloseSession(false);

        if (playerNewlyCreated) {
            this.storeDfvPlayer(user.getDfvPlayer());
        } else {
            this.updateDfvPlayer(user.getDfvPlayer());
        }

        // insert User
        this.addNew(user);

        // set autoclose to original value
        this.setAutoCloseSession(orgCloseSession);

        if (this.sqlSession != null && this.autoCloseSession) {
            this.sqlSession.close();
        }
    }

    public User getUserByDfvNr(int dfvNumber) {

        final boolean orgCloseSession = this.autoCloseSession;

        // only close session at the end
        this.setAutoCloseSession(false);

        DfvPlayer dfvPlayer = this.getDfvPlayerByDfvNumber(dfvNumber);

        if (dfvPlayer == null) {
            return null;
        }

        UserMapper userMapper = this.sqlSession.getMapper(UserMapper.class);
        User user = userMapper.getByDfvPlayer(dfvPlayer.getId());

        if (user == null) {
            return null;
        }

        user.setDfvPlayer(dfvPlayer);

        // set autoclose to original value
        this.setAutoCloseSession(orgCloseSession);

        if (this.sqlSession != null && this.autoCloseSession) {
            this.sqlSession.close();
        }

        return user;
    }

    public DfvPlayer getDfvPlayerByDfvNumber(int dfvNumber) {
        DfvPlayerMapper dfvPlayerMapper = this.sqlSession.getMapper(DfvPlayerMapper.class);
        return dfvPlayerMapper.getByDfvNumber(dfvNumber);
    }

    public User getUserByEmail(String email) {
        final boolean orgCloseSession = this.autoCloseSession;

        // only close session at the end
        this.setAutoCloseSession(false);

        UserMapper userMapper = this.sqlSession.getMapper(UserMapper.class);
        User user = userMapper.getByEmail(email);

        // set autoclose to original value
        this.setAutoCloseSession(orgCloseSession);

        if (this.sqlSession != null && this.autoCloseSession) {
            this.sqlSession.close();
        }

        return user;
    }

    public List<User> findUser(String searchString) {
        try {
            UserMapper userMapper = this.sqlSession.getMapper(UserMapper.class);
            return userMapper.find(searchString);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public List<DfvMvName> findDfvMvName(String searchString) {
        try {
            DfvMvNameMapper nameMapper = this.sqlSession.getMapper(DfvMvNameMapper.class);
            return nameMapper.find(searchString);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void deleteDivision(final DivisionRegistration reg) {
        try {
            DivisionRegistrationMapper mapper = this.sqlSession.getMapper(DivisionRegistrationMapper.class);
            mapper.delete(reg);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void registerTeamForDivision(DivisionRegistrationTeams div, TeamRegistration teamReg) {
        try {
            final TeamRegistrationMapper mapper = this.sqlSession.getMapper(TeamRegistrationMapper.class);
            mapper.insertAtEnd(div, teamReg);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void unregisterTeamFromDivision(DivisionRegistrationTeams div, Team t) {
        try {
            final TeamRegistrationMapper mapper = this.sqlSession.getMapper(TeamRegistrationMapper.class);
            mapper.delete(div, t);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void addAdminToTeam(Team team, User admin) {
        this.modifyTeamAdmin(team, admin, (t, a) -> {
            final TeamMapper mapper = this.sqlSession.getMapper(t.getMapper());
            mapper.addAdmin(t, a);
        });
    }

    public void removeAdminFromTeam(Team team, User admin) {
        this.modifyTeamAdmin(team, admin, (t, a) -> {
            final TeamMapper mapper = this.sqlSession.getMapper(t.getMapper());
            mapper.removeAdmin(t, a);
        });
    }

    public void removeAllAdminsFromTeam(Team team) {
        TeamMapper teamMapper = this.sqlSession.getMapper(TeamMapper.class);
        teamMapper.removeAllAdmins(team);
        this.sqlSession.commit();
    }

    private void modifyTeamAdmin(Team team, User admin, BiConsumer<Team, User> dbAction) {
        Objects.requireNonNull(team);
        Objects.requireNonNull(admin);
        try {
            dbAction.accept(team, admin);
            this.sqlSession.commit();
        } catch (PersistenceException pe) {
            this.sqlSession.rollback();
            throw pe;
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }
}
