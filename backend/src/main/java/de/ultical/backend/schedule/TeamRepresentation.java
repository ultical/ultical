package de.ultical.backend.schedule;

import de.ultical.backend.model.Team;
import lombok.Data;

@Data
public class TeamRepresentation {
    private Team team;
    private String title;
    private boolean bye = false;
    private boolean noShow = false;

    public TeamRepresentation(TeamRepresentation teamRep) {
        this.team = teamRep.getTeam();
        this.title = teamRep.getTitle();
        this.bye = teamRep.isBye();
    }

    public TeamRepresentation(Team team) {
        this.team = team;
    }

    public TeamRepresentation(String title) {
        this.title = title;
    }

    public TeamRepresentation(boolean isBye) {
        this.setBye(isBye);
    }

    public String getName() {
        if (this.team != null) {
            return this.team.getName();
        } else {
            if (this.isBye()) {
                return "* bye *";
            } else if (this.isNoShow()) {
                return "* no show *";
            } else {
                return this.title != null ? this.title : "";
            }
        }
    }

    @Override
    public boolean equals(Object teamRep) {
        if (!(teamRep instanceof TeamRepresentation)) {
            return false;
        } else {
            return this.equals((TeamRepresentation) teamRep);
        }
    }

    private boolean equals(TeamRepresentation teamRep) {
        boolean sameTitle = (this.title != null && (this.title.equals(teamRep.title)))
                || (this.title == null && teamRep.title == null);
        boolean bothBye = this.isBye() == teamRep.isBye();
        boolean sameTeam = (this.team != null && (this.team.equals(teamRep.team)))
                || (this.team == null && teamRep.team == null);

        return sameTitle && bothBye && sameTeam;
    }

}
