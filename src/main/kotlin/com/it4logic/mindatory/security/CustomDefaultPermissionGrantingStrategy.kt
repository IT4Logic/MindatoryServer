/*
    Copyright (c) 2019, IT4Logic.

    This file is part of Mindatory project by IT4Logic.

    Mindatory is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mindatory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <https://www.gnu.org/licenses/>.

 */

package com.it4logic.mindatory.security

import org.springframework.security.acls.domain.AuditLogger
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy
import org.springframework.security.acls.model.Acl
import org.springframework.security.acls.model.Permission
import org.springframework.security.acls.model.Sid

/**
 * Customizing the default permission granting strategy
 */
class CustomDefaultPermissionGrantingStrategy (private var auditLogger: AuditLogger)
    : DefaultPermissionGrantingStrategy(auditLogger) {

    /**
     * Makes the owner of the object has all permissions
     */
    override fun isGranted(acl: Acl, permission: List<Permission>, sids: List<Sid>, administrativeMode: Boolean): Boolean {
        for (sid in sids) {
            if(sid == acl.owner)
                return true
        }
        return super.isGranted(acl, permission, sids, administrativeMode)
    }
}