package de.ultical.backend.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.glassfish.jersey.process.internal.RequestScoped;

import de.ultical.backend.api.transferClasses.DfvMvName;
import de.ultical.backend.data.mapper.AssociationMapper;
import de.ultical.backend.data.mapper.BaseMapper;
import de.ultical.backend.data.mapper.ClubMapper;
import de.ultical.backend.data.mapper.DfvMvNameMapper;
import de.ultical.backend.data.mapper.DfvPlayerMapper;
import de.ultical.backend.data.mapper.DivisionRegistrationMapper;
import de.ultical.backend.data.mapper.EventMapper;
import de.ultical.backend.data.mapper.MailCodeMapper;
import de.ultical.backend.data.mapper.PlayerMapper;
import de.ultical.backend.data.mapper.RosterMapper;
import de.ultical.backend.data.mapper.RosterPlayerMapper;
import de.ultical.backend.data.mapper.SeasonMapper;
import de.ultical.backend.data.mapper.TeamMapper;
import de.ultical.backend.data.mapper.TeamRegistrationMapper;
import de.ultical.backend.data.mapper.TournamentFormatMapper;
import de.ultical.backend.data.mapper.UserMapper;
import de.ultical.backend.model.Association;
import de.ultical.backend.model.Club;
import de.ultical.backend.model.DfvPlayer;
import de.ultical.backend.model.DivisionRegistration;
import de.ultical.backend.model.DivisionRegistrationTeams;
import de.ultical.backend.model.Event;
import de.ultical.backend.model.Identifiable;
import de.ultical.backend.model.MailCode;
import de.ultical.backend.model.Player;
import de.ultical.backend.model.Roster;
import de.ultical.backend.model.Season;
import de.ultical.backend.model.Team;
import de.ultical.backend.model.TeamRegistration;
import de.ultical.backend.model.TournamentEdition;
import de.ultical.backend.model.TournamentFormat;
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
     * Use this method to get access to an <code>AutoCloseable</code> instance
     * that could be used, to close the <code>DataStore</code>'s internal
     * {@link SqlSession}.
     * <p>
     * The feature provided by this method is useful if you want to do more than
     * one database access at once and therefore have to avoid that the session
     * to the databse is closed automatically after the first access. In order
     * to avoid any problems due to not closed resources you are strongly
     * encouraged to use this method in conjunction with Java's
     * try-with-resources feature.
     *
     * @return an instance of <code>AutoCloseable</code> that could be used to
     *         close the DataStore Sql-Connection within a try block.
     */
    public AutoCloseable getClosable() {
        this.autoCloseSession = false;
        return new AutoCloseable() {

            @Override
            public void close() {
                DataStore.this.closeSession();

            }
        };
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
    private void setAutoCloseSession(boolean newACS) {
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
    private boolean closeSession() {
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

    public List<LocalDate> getRosterBlockingDates(int rosterId) {
        try {
            RosterMapper rosterMapper = this.sqlSession.getMapper(RosterMapper.class);
            return rosterMapper.getBlockingDate(rosterId);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public List<TeamRegistration> getTeamRegistrationOfPlayerSeason(int playerId, int seasonId, String divisionAge,
            String divisionType) {
        try {
            RosterMapper rosterMapper = this.sqlSession.getMapper(RosterMapper.class);
            return rosterMapper.getTrByPlayerSeasonDivisionQualified(playerId, seasonId, divisionAge, divisionType);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public Roster getRosterOfTeamSeason(int teamId, int seasonId, String divisionAge, String divisionType) {
        try {
            RosterMapper rosterMapper = this.sqlSession.getMapper(RosterMapper.class);
            return rosterMapper.getByTeamSeasonDivision(teamId, seasonId, divisionAge, divisionType);
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void removePlayerFromRoster(int playerId, int rosterId) {
        try {
            RosterPlayerMapper rosterPlayerMapper = this.sqlSession.getMapper(RosterPlayerMapper.class);
            rosterPlayerMapper.deletePlayer(playerId, rosterId);
            this.sqlSession.commit();
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public DfvPlayer getPlayerByDfvNumber(int dfvNumber) {
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

    public void updateUserWithoutPassword(User user) {
        try {
            UserMapper userMapper = this.sqlSession.getMapper(UserMapper.class);
            userMapper.updateWithoutPassword(user);
            this.sqlSession.commit();
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public Club getClub(int clubId) {
        try {
            ClubMapper clubMapper = this.sqlSession.getMapper(ClubMapper.class);
            return clubMapper.get(clubId);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public List<Club> getAllClubs() {
        try {
            ClubMapper clubMapper = this.sqlSession.getMapper(ClubMapper.class);
            return clubMapper.getAll();
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    /*
     * update the dfv association table
     */
    public void refreshAssociations(List<Association> retrievedAssociations) {
        try {
            AssociationMapper associationMapper = this.sqlSession.getMapper(AssociationMapper.class);
            Set<Integer> existingAssociations = associationMapper.getAllIds();

            for (Association association : retrievedAssociations) {
                if (existingAssociations.contains(association.getId())) {
                    associationMapper.updateBasics(association);
                } else {
                    associationMapper.insert(association);
                }
            }
            this.sqlSession.commit();
        } finally {
            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    /*
     * update the dfvMvName table
     */
    public void refreshClubs(List<Club> retrievedClubs) {
        try {
            ClubMapper clubMapper = this.sqlSession.getMapper(ClubMapper.class);
            Set<Integer> existingClubIds = clubMapper.getAllIds();

            for (Club club : retrievedClubs) {
                if (existingClubIds.contains(club.getId())) {
                    clubMapper.update(club);
                } else {
                    clubMapper.insert(club);
                }
            }
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
        try {
            // only close session at the end
            this.setAutoCloseSession(false);

            // insert Player with corresponding mapper
            PlayerMapper playerMapper = this.sqlSession.getMapper(PlayerMapper.class);
            playerMapper.update(dfvPlayer);

            // insert DfvPlayer
            this.update(dfvPlayer);
        } finally {
            // set autoclose to original value
            this.setAutoCloseSession(orgCloseSession);

            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void storeDfvPlayer(DfvPlayer dfvPlayer) {
        /**
         * A DfvPlayer has to be stored in two steps First Player (superclass)
         * then DfvPlayer (subclass)
         */
        boolean orgCloseSession = this.autoCloseSession;
        try {
            // only close session at the end
            this.setAutoCloseSession(false);

            // insert Player with corresponding mapper
            PlayerMapper playerMapper = this.sqlSession.getMapper(PlayerMapper.class);
            playerMapper.insertPlayer(dfvPlayer, dfvPlayer instanceof DfvPlayer);

            // insert DfvPlayer
            this.addNew(dfvPlayer);
        } finally {
            // set autoclose to original value
            this.setAutoCloseSession(orgCloseSession);

            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void storeUser(User user, boolean playerNewlyCreated) {
        /**
         * A user has to be stored in two steps First DfvPlayer, then User
         */
        boolean orgCloseSession = this.autoCloseSession;
        try {
            // only close session at the end
            this.setAutoCloseSession(false);

            if (playerNewlyCreated) {
                this.storeDfvPlayer(user.getDfvPlayer());
            } else {
                this.updateDfvPlayer(user.getDfvPlayer());
            }

            // insert User
            this.addNew(user);
        } finally {
            // set autoclose to original value
            this.setAutoCloseSession(orgCloseSession);

            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public User getUserByDfvNr(int dfvNumber) {

        final boolean orgCloseSession = this.autoCloseSession;
        User user;
        try {
            // only close session at the end
            this.setAutoCloseSession(false);

            DfvPlayer dfvPlayer = this.getDfvPlayerByDfvNumber(dfvNumber);

            if (dfvPlayer == null) {
                return null;
            }

            UserMapper userMapper = this.sqlSession.getMapper(UserMapper.class);
            user = userMapper.getByDfvPlayer(dfvPlayer.getId());

            if (user == null) {
                return null;
            }

            user.setDfvPlayer(dfvPlayer);
        } finally {
            // set autoclose to original value
            this.setAutoCloseSession(orgCloseSession);

            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }

        return user;
    }

    public DfvPlayer getDfvPlayerByDfvNumber(int dfvNumber) {
        try {
            DfvPlayerMapper dfvPlayerMapper = this.sqlSession.getMapper(DfvPlayerMapper.class);
            return dfvPlayerMapper.getByDfvNumber(dfvNumber);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public MailCode getMailCode(String code) {
        try {
            MailCodeMapper mcMapper = this.sqlSession.getMapper(MailCodeMapper.class);
            return mcMapper.get(code);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public void deleteMailCode(String code) {
        try {
            MailCodeMapper mcMapper = this.sqlSession.getMapper(MailCodeMapper.class);
            mcMapper.delete(code);
            this.sqlSession.commit();
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public boolean saveMailCode(MailCode mailCode) {
        try {
            MailCodeMapper mcMapper = this.sqlSession.getMapper(MailCodeMapper.class);
            mcMapper.deletePreviousEntries(mailCode);
            int insertedRows = mcMapper.insert(mailCode);
            this.sqlSession.commit();
            return insertedRows == 1;
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public User getUserByEmail(String email) {
        final boolean orgCloseSession = this.autoCloseSession;
        try {
            // only close session at the end
            this.setAutoCloseSession(false);

            UserMapper userMapper = this.sqlSession.getMapper(UserMapper.class);
            User user = userMapper.getByEmail(email);
            return user;
        } finally {
            // set autoclose to original value
            this.setAutoCloseSession(orgCloseSession);

            if (this.sqlSession != null && this.autoCloseSession) {
                this.sqlSession.close();
            }
        }

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

    public Event getEventByDivision(int divisionId) {
        try {
            EventMapper em = this.sqlSession.getMapper(EventMapper.class);
            return em.getByDivisionRegistration(divisionId);
        } finally {
            if (this.autoCloseSession) {
                this.sqlSession.close();
            }
        }
    }

    public TournamentFormat getFormatByEvent(int eventId) {
        try {
            TournamentFormatMapper tfMapper = this.sqlSession.getMapper(TournamentFormatMapper.class);
            return tfMapper.getByEvent(eventId);
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

    public TeamRegistration registerTeamForEdition(int divisionRegistrationId, TeamRegistration teamReg) {
        try {
            final TeamRegistrationMapper mapper = this.sqlSession.getMapper(TeamRegistrationMapper.class);
            mapper.insert(divisionRegistrationId, teamReg);
            this.sqlSession.commit();
            return teamReg;
            // mapper.insertAtEnd(div, teamReg);
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
        // try-finally block is inside modifyTeamAdmin
        this.modifyTeamAdmin(team, admin, (t, a) -> {
            final TeamMapper mapper = this.sqlSession.getMapper(t.getMapper());
            mapper.addAdmin(t, a);
        });

    }

    public void removeAdminFromTeam(Team team, User admin) {
        // try - finally block is inside modifyTeamAdmin method
        this.modifyTeamAdmin(team, admin, (t, a) -> {
            final TeamMapper mapper = this.sqlSession.getMapper(t.getMapper());
            mapper.removeAdmin(t, a);
        });

    }

    public void removeAllAdminsFromTeam(Team team) {
        // try-finally block is inside modifyTeamAdmin
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
