/**

Copyright (C) SYSTAP, LLC 2006-2010.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Jun 2, 2010
 */

package com.bigdata.quorum;

/**
 * A type-safe enumeration for informational quorum events.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public enum QuorumEventEnum {

    /**
     * Event generated when a member service is added to a quorum.
     */
    MEMBER_ADDED,
    /**
     * Event generated when a member service is removed form a quorum.
     */
    MEMBER_REMOVED,
    /**
     * Event generated when a service is added to the write pipeline.
     */
    PIPELINE_ADDED,
    /**
     * Event generated when a member service is removed from the write pipeline.
     */
    PIPELINE_REMOVED,
    /**
     * Vote cast by a service for some lastCommitTime.
     */
    VOTE_CAST,
    /**
     * Vote for some lastCommitTime was withdrawn by a service.
     */
    VOTE_WITHDRAWN,
	/**
	 * A consensus has been achieved with <code>(k+1)/2</code> services voting
	 * for some lastCommitTime. This event will typically be associated with an
	 * invalid quorum token since the quorum token is assigned when the leader
	 * is elected and this event generally becomes visible before the
	 * {@link #LEADER_ELECTED} event.
	 */
    CONSENSUS,
    /**
     * Event generated when a service joins a quorum.
     */
    SERVICE_JOINED,
    /**
     * Event generated when a service leaves a quorum.
     */
    SERVICE_LEFT,
    /**
     * Event generated when a new leader is elected, including when a quorum
     * meets.
     */
    LEADER_ELECTED,
    /**
     * Event generated when a service joins a quorum as a follower.
     */
    FOLLOWER_ELECTED,
//    /**
//     * Event generated when the leader leaves a quorum.
//     */
//    LEADER_LEFT,
    /**
     * Event generated when the last valid token is set.
     */
    SET_LAST_VALID_TOKEN,
    /**
     * Event generated when a quorum meets.
     */
    QUORUM_MEET,
    /**
     * Event generated when a quorum breaks due to a leader leave or a service
     * leave which brings the #of joined services to <code>((k+1)/2)-1</code>.
     */
    QUORUM_BROKE;

}
