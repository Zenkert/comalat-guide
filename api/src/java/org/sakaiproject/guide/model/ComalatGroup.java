package org.sakaiproject.guide.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The Model for the COMALAT Group
 *
 * @author Johannes Zenkert (johannes.zenkert@uni-siegen.de)
 */

public class ComalatGroup implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long comalatGroupId; // the comalat group id
    private String comalatGroupIdentifier; // the name of the group
    private Set<ComalatUser> comalatUsers =
            new HashSet<>(0);

    public ComalatGroup() {
    }

    public ComalatGroup(Long comalatGroupId) {
        this.comalatGroupId = comalatGroupId;
    }

    public Set<ComalatUser> getComalatUsers() {
        return comalatUsers;
    }

    public void setComalatUsers(Set<ComalatUser> comalatUsers) {
        this.comalatUsers = comalatUsers;
    }

    public String getComalatGroupIdentifier() {
        return comalatGroupIdentifier;
    }

    public void setComalatGroupIdentifier(String comalatGroupIdentifier) {
        this.comalatGroupIdentifier = comalatGroupIdentifier;
    }

    public Long getComalatGroupId() {
        return comalatGroupId;
    }

    public void setComalatGroupId(Long comalatGroupId) {
        this.comalatGroupId = comalatGroupId;
    }

}
